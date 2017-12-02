package com.austinv11.aspects.inject;

import com.austinv11.aspects.annotation.*;
import com.austinv11.aspects.bridge.ExecutionSignal;
import com.austinv11.aspects.hook.*;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.scanner.ClassInfo;
import io.github.lukehutch.fastclasspathscanner.scanner.FieldInfo;
import io.github.lukehutch.fastclasspathscanner.scanner.MethodInfo;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.*;
import net.bytebuddy.matcher.ElementMatchers;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Executable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.*;

/**
 * This class wires all aspects and performs injections.
 */
public class AspectInjector {

    private AgentBuilder buddy;
    private final Instrumentation instrumentation;
    private final List<ClasspathSniffer> sniffers = Collections.synchronizedList(new ArrayList<>());
    private final boolean classpathScanningEnabled;

    /**
     * Constructs a new injector.
     *
     * @param instrumentation The instrumentation instance used to perform the bytecode injection with.
     * @param classpathScanningEnabled This is used to toggle classpath scanning because it will add some overhead on
     *                                 injection.
     */
    public AspectInjector(Instrumentation instrumentation, boolean classpathScanningEnabled) {
        this.instrumentation = instrumentation;
        this.buddy = new AgentBuilder.Default().with(AgentBuilder.LambdaInstrumentationStrategy.ENABLED);
        this.classpathScanningEnabled = classpathScanningEnabled;
    }

    /**
     * Constructs a new injector.
     *
     * @param instrumentation The instrumentation instance used to perform the bytecode injection with.
     */
    public AspectInjector(Instrumentation instrumentation) {
        this(instrumentation, true);
    }

    /**
     * Constructs a new injector.
     *
     * @param classpathScanningEnabled This is used to toggle classpath scanning because it will add some overhead on
     *                                 injection.
     */
    public AspectInjector(boolean classpathScanningEnabled) {
        this(null, classpathScanningEnabled);
    }

    /**
     * Constructs a new injector.
     */
    public AspectInjector() {
        this(null);
    }

    /**
     * Performs injection with all the aspects currently configured.
     *
     * @throws IOException
     */
    public void inject() throws IOException {
        if (instrumentation != null)
            buddy.enableBootstrapInjection(instrumentation, File.createTempFile("bootstrap", "temp")).installOn(instrumentation);
        else
            buddy.enableUnsafeBootstrapInjection().installOnByteBuddyAgent();

        if (classpathScanningEnabled)
            runScan();
    }

    private void runScan() {
        ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
            Thread thread = Executors.defaultThreadFactory().newThread(r);
            thread.setDaemon(true);
            thread.setName("SimpleAspects Classpath Scanner");
            return thread;
        });

        executor.execute(() -> {
            FastClasspathScanner scanner = new FastClasspathScanner()
                    .enableFieldInfo()
                    .enableFieldAnnotationIndexing()
                    .enableMethodInfo()
                    .enableMethodAnnotationIndexing()
                    .ignoreFieldVisibility()
                    .ignoreMethodVisibility();

            Map<Class<? extends Annotation>, List<ClasspathSniffer>> classifiedSniffers = new HashMap<>();

            for (ClasspathSniffer sniffer : sniffers) {
                classifiedSniffers.putIfAbsent(sniffer.getDiscoveryAnnotationType(), new ArrayList<>());
                classifiedSniffers.get(sniffer.getDiscoveryAnnotationType()).add(sniffer);
            }

            ScanResult results = scanner.scan();
            Map<String, ClassInfo> scanned = results.getClassNameToClassInfo();

            classifiedSniffers.forEach((annotation, sniffers) -> {
                String annotationName = annotation.getCanonicalName();

                scanned.forEach((name, info) -> {
                    if (info.hasAnnotation(annotationName)) {
                        sniffers.forEach(sniffer -> sniffer.sniff(name, info, null, null));
                    }
                    if (info.hasMethodWithAnnotation(annotationName)) {
                       info.getMethodInfo()
                               .stream()
                               .filter(methodInfo -> methodInfo.getAnnotationNames().contains(annotationName))
                               .forEach(methodInfo -> sniffers.forEach(sniffer -> sniffer.sniff(name, info, methodInfo, null)));
                    }
                    if (info.hasFieldWithAnnotation(annotationName)) {
                        info.getFieldInfo()
                                .stream()
                                .filter(fieldInfo -> fieldInfo.getAnnotationNames().contains(annotationName))
                                .forEach(fieldInfo -> sniffers.forEach(sniffer -> sniffer.sniff(name, info, null, fieldInfo)));
                    }
                });
            });

            executor.shutdown();
        });
    }

    /**
     * Gets all the annotation types this annotation represents by checking for the hierarchy defined by the
     * {@link Aspect} meta annotation.
     *
     * @param progress A mutable list to track scanning progress, this ensures that infinite recursion doesn't occur.
     * @param annotation The annotation to get the inheritance of.
     * @return The inheritance list (including the original passed in annotation).
     */
    private List<Class<? extends Annotation>> getInheritanceList(List<Class<? extends Annotation>> progress, Class<? extends Annotation> annotation) {
        if (progress == null) {
            Annotation[] annotations = annotation.getAnnotations();
            List<Class<? extends Annotation>> inherited = new ArrayList<>();
            for (Annotation annotation1 : annotations) {
                List<Class<? extends Annotation>> inheritance = getInheritanceList(new ArrayList<>(), annotation1.annotationType());
                if (!inheritance.isEmpty()) {
                    inherited.add(annotation1.annotationType());
                    inherited.addAll(inheritance);
                }
            }

            inherited.add(annotation);

            return inherited.stream().distinct().collect(Collectors.toList());
        }

        List<Class<? extends Annotation>> annotations = new ArrayList<>();
        for (Annotation annotation1 : annotation.getAnnotations()) {
            if (!annotation1.annotationType().equals(annotation) && !(progress.contains(annotation1.annotationType()))) {
                progress.add(annotation1.annotationType());
                List<Class<? extends Annotation>> found = getInheritanceList(progress, annotation1.annotationType());

                if (found.isEmpty())
                    continue;

                annotations.addAll(found);
            }
        }

        if (annotation.isAnnotationPresent(Aspect.class))
            annotations.add(annotation);
        return annotations.stream().distinct().collect(Collectors.toList());
    }

    private AspectInjector connectMethod(Class<? extends Annotation> clazz, Interceptor interceptor) {
        buddy = buddy.type(ElementMatchers.isAnnotatedWith(clazz))
                .transform((builder, typeDescription, classLoader, module) -> builder.method(ElementMatchers.any()).intercept(MethodDelegation.to(interceptor)))
                .type(ElementMatchers.declaresMethod(ElementMatchers.isAnnotatedWith(clazz)))
                .transform((builder, typeDescription, classLoader, module) -> builder.method(ElementMatchers.isAnnotatedWith(clazz)).intercept(MethodDelegation.to(interceptor)))
                .asDecorator();
        return this;
    }
    
//    private AspectInjector connectParam(Class<? extends Annotation> clazz, Interceptor interceptor) {
//        buddy = buddy.type(ElementMatchers.declaresMethod(ElementMatchers.isAnnotatedWith(clazz)))
//                .transform(((builder, typeDescription, classLoader, module) -> builder.method(ElementMatchers.isAnnotatedWith(clazz)).intercept(MethodDelegation.to(interceptor))))
//                .type(ElementMatchers.declaresMethod(ElementMatchers.any()))
//                .transform(((builder, typeDescription, classLoader, module) -> builder.constructor(ElementMatchers.hasParameters(ElementMatchers.whereAny(ElementMatchers.isAnnotatedWith(clazz))))
//                    .intercept(MethodDelegation.to(interceptor))
//                    .method(ElementMatchers.hasParameters(ElementMatchers.whereAny(ElementMatchers.isAnnotatedWith(clazz))).and(ElementMatchers.not(ElementMatchers.isConstructor())))
//                    .intercept(MethodDelegation.to(interceptor))))
//                .asDecorator();
//        return this;
//    }

    private void precheckAspectInfo(Class<? extends Annotation> clazz, Class<? extends Annotation> expectedAspect) {
        List<Class<? extends Annotation>> inherited = getInheritanceList(null, clazz);

        if (inherited.isEmpty())
            throw new RuntimeException("Invalid aspect inheritance!");

        if (expectedAspect != null && !inherited.contains(expectedAspect))
            throw new RuntimeException("Expected an aspect of type " + expectedAspect);
    }

    /**
     * Register a class discovery hook.
     *
     * @param clazz The annotation class to limit search by.
     * @param hook The hook to call on discovery.
     * @return The current injector instance to allow for chaining.
     */
    public AspectInjector discoverClasses(Class<? extends Annotation> clazz, ClassDiscoveryHook hook) {
        precheckAspectInfo(clazz, Discover.class);

        sniffers.add(ClasspathSniffer.wrap(hook, clazz));

        return this;
    }

    /**
     * Register a method discovery hook.
     *
     * @param clazz The annotation class to limit search by.
     * @param hook The hook to call on discovery.
     * @return The current injector instance to allow for chaining.
     */
    public AspectInjector discoverMethods(Class<? extends Annotation> clazz, MethodDiscoveryHook hook) {
        precheckAspectInfo(clazz, Discover.class);

        sniffers.add(ClasspathSniffer.wrap(hook, clazz));

        return this;
    }

    /**
     * Register a field discovery hook.
     *
     * @param clazz The annotation class to limit search by.
     * @param hook The hook to call on discovery.
     * @return The current injector instance to allow for chaining.
     */
    public AspectInjector discoverFields(Class<? extends Annotation> clazz, FieldDiscoveryHook hook) {
        precheckAspectInfo(clazz, Discover.class);

        sniffers.add(ClasspathSniffer.wrap(hook, clazz));

        return this;
    }
    
    private AspectInjector connect(Class<? extends Annotation> clazz, InvocationHook hook, Class<? extends Annotation> expectedAspect) {
        precheckAspectInfo(clazz, expectedAspect);

        connectMethod(clazz, Interceptor.wrap(hook, clazz));

        return this;
    }

    /**
     * Wires an annotation extending {@link Before} to a corresponding hook.
     *
     * @param clazz The annotation to wire to.
     * @param hook The hook to wire with.
     * @return The current injector instance to allow for chaining.
     */
    public AspectInjector connectBefore(Class<? extends Annotation> clazz, BeforeHook hook) {
        return connect(clazz, hook, Before.class);
    }

    /**
     * Wires an annotation extending {@link After} to a corresponding hook.
     *
     * @param clazz The annotation to wire to.
     * @param hook The hook to wire with.
     * @return The current injector instance to allow for chaining.
     */
    public AspectInjector connectAfter(Class<? extends Annotation> clazz, AfterHook hook) {
        return connect(clazz, hook, After.class);
    }

    /**
     * Wires an annotation extending {@link Wrap} to a corresponding hook.
     *
     * @param clazz The annotation to wire to.
     * @param hook The hook to wire with.
     * @return The current injector instance to allow for chaining.
     */
    public AspectInjector connectAround(Class<? extends Annotation> clazz, WrapHook hook) {
        return connect(clazz, hook, Wrap.class);
    }

    /**
     * Wires an annotation extending {@link Init} to a corresponding hook.
     *
     * @param clazz The annotation to wire to.
     * @param hook The hook to wire with.
     * @return The current injector instance to allow for chaining.
     */
    public AspectInjector connectConstructor(Class<? extends Annotation> clazz, InitHook hook) {
        return connect(clazz, hook, Init.class);
    }

    /**
     * Wires an annotation extending {@link Replace} to a corresponding hook.
     *
     * @param clazz The annotation to wire to.
     * @param hook The hook to wire with.
     * @return The current injector instance to allow for chaining.
     */
    public AspectInjector connectReplacement(Class<? extends Annotation> clazz, InvocationHook hook) { //Using the default hook b/c there is no special logic
        return connect(clazz, hook, Replace.class);
    }

    /**
     * Internal class used for performing execution interceptions.
     */
    public static class Interceptor {

        private final InvocationHook invocationHook;
        private final Class<? extends Annotation> annotation;

        private Interceptor(InvocationHook hook, Class<? extends Annotation> annotation) {
            this.invocationHook = hook;
            this.annotation = annotation;
        }

        @BindingPriority(BindingPriority.DEFAULT * 2)
        @RuntimeType
        public Object intercept(@Origin Executable info, @This(optional = true) Object obj,
                                @SuperCall(nullIfImpossible = true) Callable<Object> zuper, @AllArguments Object[] args,
                                @StubValue Object stub) throws Throwable {
            if (invocationHook != null) {
                Annotation annotationInstance = info.isAnnotationPresent(annotation) ? info.getDeclaredAnnotation(annotation) : info.getDeclaringClass().getAnnotation(annotation);
                ExecutionSignal<?> sig = invocationHook.intercept(info, annotationInstance, obj, zuper == null ? () -> null : zuper, args);
                switch (sig.getType()) {
                    case RETURN:
                        return stub;
                    case RETURN_VALUE:
                        return sig.getReturnValue();
                    case PASS:
                        return zuper != null ? zuper.call() : null;
                    case THROW:
                        throw sig.getThrowable();
                    default:
                        return stub;
                }
            } else {
                throw new RuntimeException("No valid hook!");
            }
        }
        
        public static Interceptor wrap(InvocationHook hook, Class<? extends Annotation> annotation) {
            return new Interceptor(hook, annotation);
        }
    }

    /**
     * Internal class used for performing classpath scanning.
     */
    public static class ClasspathSniffer {

        private final ClassDiscoveryHook classHook;
        private final MethodDiscoveryHook methodHook;
        private final FieldDiscoveryHook fieldHook;
        private final Class<? extends Annotation> annotation;

        private ClasspathSniffer(ClassDiscoveryHook classHook, MethodDiscoveryHook methodHook, FieldDiscoveryHook fieldHook, Class<? extends Annotation> annotation) {
            this.classHook = classHook;
            this.methodHook = methodHook;
            this.fieldHook = fieldHook;
            this.annotation = annotation;
        }

        private void sniff(String name, ClassInfo classInfo, MethodInfo methodInfo, FieldInfo fieldInfo) {
            if (classHook != null && methodInfo == null && fieldInfo == null) {
                classHook.onDiscover(name, classInfo);
            } else if (methodHook != null && methodInfo != null) {
                methodHook.onDiscover(name, classInfo, methodInfo);
            } else if (fieldHook != null && fieldInfo != null) {
                fieldHook.onDiscover(name, classInfo, fieldInfo);
            }
        }

        private Class<? extends Annotation> getDiscoveryAnnotationType() {
            return annotation;
        }

        public static ClasspathSniffer wrap(ClassDiscoveryHook hook, Class<? extends Annotation> annotation) {
            return new ClasspathSniffer(hook, null, null, annotation);
        }

        public static ClasspathSniffer wrap(MethodDiscoveryHook hook, Class<? extends Annotation> annotation) {
            return new ClasspathSniffer(null, hook, null, annotation);
        }

        public static ClasspathSniffer wrap(FieldDiscoveryHook hook, Class<? extends Annotation> annotation) {
            return new ClasspathSniffer(null, null, hook, annotation);
        }
    }
}
