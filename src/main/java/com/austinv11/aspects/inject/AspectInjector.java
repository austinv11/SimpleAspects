package com.austinv11.aspects.inject;

import com.austinv11.aspects.annotation.After;
import com.austinv11.aspects.annotation.Aspect;
import com.austinv11.aspects.annotation.Before;
import com.austinv11.aspects.annotation.Wrap;
import com.austinv11.aspects.bridge.ExecutionSignal;
import com.austinv11.aspects.hook.AfterHook;
import com.austinv11.aspects.hook.BeforeHook;
import com.austinv11.aspects.hook.Hook;
import com.austinv11.aspects.hook.WrapHook;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.*;
import net.bytebuddy.matcher.ElementMatchers;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.instrument.Instrumentation;
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
    
    private AspectInjector connect(Class<? extends Annotation> clazz, Hook hook, Class<? extends Annotation> expectedAspect) {
        List<Class<? extends Annotation>> inherited = getInheritanceList(null, clazz);
    
        if (inherited.isEmpty())
            throw new RuntimeException("Invalid aspect inheritance!");
        
        if (expectedAspect != null && !inherited.contains(expectedAspect))
            throw new RuntimeException("Expected an aspect of type " + expectedAspect);
    
        if (inherited.contains(Before.class) || inherited.contains(After.class) || inherited.contains(Wrap.class))
            connectMethod(clazz, Interceptor.wrap(hook));
    
        return this;
    }
    
    public AspectInjector connect(Class<? extends Annotation> clazz, Hook hook) {
        return connect(clazz, hook, null);
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

    public static class Interceptor {

        private final Hook hook;

        private Interceptor(Hook hook) {
            this.hook = hook;
        }

        @RuntimeType
        public Object intercept(@Origin String clazz, @This Object obj, @SuperCall Callable<Object> zuper, @AllArguments Object[] args) throws Throwable {
            ExecutionSignal<?> sig = hook.intercept(clazz, obj, zuper, args);
            switch (sig.getType()) {
                case RETURN:
                    return null;
                case RETURN_VALUE:
                    return sig.getReturnValue();
                case PASS:
                    return zuper.call();
                case THROW:
                    throw sig.getThrowable();
                default:
                    return null;
            }
        }
        
        public static Interceptor wrap(Hook hook) {
            return new Interceptor(hook);
        }
    }
}
