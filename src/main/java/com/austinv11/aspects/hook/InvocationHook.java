package com.austinv11.aspects.hook;

import com.austinv11.aspects.bridge.ExecutionSignal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.util.concurrent.Callable;

/**
 * This represents a hook called to replace existing logic.
 *
 * This corresponds to the {@link com.austinv11.aspects.annotation.Replace} annotation.
 */
@FunctionalInterface
public interface InvocationHook {

    /**
     * This is called to replace execution of a method.
     *
     * @param origin This is the executable being replaced.
     * @param aspect The annotation corresponding to the hook.
     * @param obj The instance of the object the executable is being being invoked for.
     * @param zuper The invokable method being overridden.
     * @param args The arguments for the execution.
     * @return The execution signal representing the results of this overridden invocation.
     *
     * @throws Throwable
     */
    ExecutionSignal intercept(Executable origin, Annotation aspect,
                              Object obj, Callable<Object> zuper, Object[] args) throws Throwable;
}
