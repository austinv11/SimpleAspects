package com.austinv11.aspects.inject;

import com.austinv11.aspects.annotation.*;
import com.austinv11.aspects.bridge.ExecutionSignal;
import com.austinv11.aspects.hook.*;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.*;
import net.bytebuddy.matcher.ElementMatchers;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class AspectInjector {

    private AgentBuilder buddy;
    private final Instrumentation instrumentation;

    public AspectInjector(Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
        this.buddy = new AgentBuilder.Default().with(AgentBuilder.LambdaInstrumentationStrategy.ENABLED);
    }

    public AspectInjector() {
        this(null);
    }

    public void inject() throws IOException {
        if (instrumentation != null)
            buddy.enableBootstrapInjection(instrumentation, File.createTempFile("bootstrap", "temp")).installOn(instrumentation);
        else
            buddy.enableUnsafeBootstrapInjection().installOnByteBuddyAgent();
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

            return inherited;
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
        return annotations;
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
    
    private AspectInjector connect(Class<? extends Annotation> clazz, InvocationHook hook, Class<? extends Annotation> expectedAspect) {
        List<Class<? extends Annotation>> inherited = getInheritanceList(null, clazz);
    
        if (inherited.isEmpty())
            throw new RuntimeException("Invalid aspect inheritance!");
        
        if (expectedAspect != null && !inherited.contains(expectedAspect))
            throw new RuntimeException("Expected an aspect of type " + expectedAspect);

        connectMethod(clazz, Interceptor.wrap(hook, clazz));

        return this;
    }

    public AspectInjector connectBefore(Class<? extends Annotation> clazz, BeforeHook hook) {
        return connect(clazz, hook, Before.class);
    }
    
    public AspectInjector connectAfter(Class<? extends Annotation> clazz, AfterHook hook) {
        return connect(clazz, hook, After.class);
    }
    
    public AspectInjector connectAround(Class<? extends Annotation> clazz, WrapHook hook) {
        return connect(clazz, hook, Wrap.class);
    }

    public AspectInjector connectConstructor(Class<? extends Annotation> clazz, InitHook hook) {
        return connect(clazz, hook, Init.class);
    }

    public AspectInjector connectReplacement(Class<? extends Annotation> clazz, InvocationHook hook) { //Using the default hook b/c there is no special logic
        return connect(clazz, hook, Replace.class);
    }

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
}
