package com.austinv11.aspects.hook;

import com.austinv11.aspects.bridge.ExecutionSignal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.util.concurrent.Callable;

/**
 * This represents a hook called after execution of a method.
 *
 * This corresponds to the {@link com.austinv11.aspects.annotation.After} annotation.
 */
@FunctionalInterface
public interface AfterHook extends InvocationHook {

    /**
     * This is called after a method has been executed.
     *
     * @param origin This is the method which has been executed.
     * @param aspect The annotation corresponding to the hook.
     * @param obj The instance of the object the method is being being invoked for.
     * @param args The arguments for the execution.
     * @return The execution signal representing the results of this overridden invocation.
     *
     * @throws Throwable
     */
    ExecutionSignal after(Executable origin, Annotation aspect, Object obj, Object[] args) throws Throwable;
    
    @Override
    default ExecutionSignal intercept(Executable origin, Annotation aspect, Object obj, Callable<Object> zuper, Object[] args) throws Throwable {
        ExecutionSignal<?> orig;
        try {
            orig = ExecutionSignal.returnValue(zuper.call());
        } catch (Throwable t) {
            orig = ExecutionSignal.throwException(t);
        }
        ExecutionSignal<?> sig = after(origin, aspect, obj, args);
        if (sig.getType() == ExecutionSignal.SignalType.PASS)
            return orig;
        else
            return sig;
    }
}
