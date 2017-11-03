package com.austinv11.aspects.hook;

import com.austinv11.aspects.bridge.ExecutionSignal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.util.concurrent.Callable;

/**
 * This represents a hook called after execution of a constructor.
 *
 * This corresponds to the {@link com.austinv11.aspects.annotation.Init} annotation.
 */
@FunctionalInterface
public interface InitHook extends InvocationHook {

    /**
     * This is called after the class has been constructed.
     *
     * @param origin This is the constructor which has been executed.
     * @param aspect The annotation corresponding to the hook.
     * @param obj The instance of the object the constructor is being being invoked for.
     * @param args The arguments for the construction.
     * @return The execution signal representing the results of this overridden invocation.
     *
     * @throws Throwable
     */
    ExecutionSignal construct(Executable origin, Annotation aspect, Object obj, Object[] args) throws Throwable; //Ignore the super call b/c this should happen after everything else was constructed

    @Override
    default ExecutionSignal intercept(Executable origin, Annotation aspect, Object obj, Callable<Object> zuper, Object[] args) throws Throwable {
        zuper.call();
        return (ExecutionSignal<?>) construct(origin, aspect, obj, args);
    }
}
