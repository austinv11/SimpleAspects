package com.austinv11.aspects.inject;

import com.austinv11.aspects.annotation.Before;
import com.austinv11.aspects.bridge.ExecutionSignal;
import com.austinv11.aspects.hook.BeforeHook;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.*;
import net.bytebuddy.matcher.ElementMatchers;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
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

    public <T> AspectInjector connect(Class<? extends Before> clazz, BeforeHook hook) {
        buddy = buddy.type(ElementMatchers.isAnnotatedWith(clazz))
                .transform((builder, typeDescription, classLoader, module) -> builder.method(ElementMatchers.any()).intercept(MethodDelegation.to(new BeforeInterceptor(hook))))
        .type(ElementMatchers.declaresMethod(ElementMatchers.isAnnotatedWith(clazz)))
                .transform((builder, typeDescription, classLoader, module) -> builder.method(ElementMatchers.isAnnotatedWith(clazz)).intercept(MethodDelegation.to(new BeforeInterceptor(hook))));
        return this;
    }

    public class BeforeInterceptor {

        private final BeforeHook hook;

        private BeforeInterceptor(BeforeHook hook) {
            this.hook = hook;
        }

        @RuntimeType
        public Object intercept(@Origin String clazz, @This Object obj, @SuperCall Callable<Object> zuper, @AllArguments Object[] args) throws Throwable {
            ExecutionSignal<?> sig = hook.before(clazz, obj, args);
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
    }
}
