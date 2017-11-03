package com.austinv11.aspects.hook;

import com.austinv11.aspects.bridge.ExecutionSignal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.util.concurrent.Callable;

/**
 * This represents a hook called before execution of a method.
 *
 * This corresponds to the {@link com.austinv11.aspects.annotation.Before} annotation.
 */
@FunctionalInterface
public interface BeforeHook extends InvocationHook {

    /**
     * This is called before a method has been executed.
     *
     * @param origin This is the method which has not been executed yet.
     * @param aspect The annotation corresponding to the hook.
     * @param obj The instance of the object the method is being being invoked for.
     * @param args The arguments for the execution.
     * @return The execution signal representing the results of this overridden invocation.
     *
     * @throws Throwable
     */
    ExecutionSignal before(Executable origin, Annotation aspect, Object obj, Object[] args) throws Throwable;
	
    @Override
    default ExecutionSignal intercept(Executable origin, Annotation aspect, Object obj, Callable<Object> zuper, Object[] args) throws Throwable {
        ExecutionSignal<?> sig = before(origin, aspect, obj, args);
        if (sig.getType() == ExecutionSignal.SignalType.PASS)
            return ExecutionSignal.returnValue(zuper.call());
        else
            return sig;
	}
}
