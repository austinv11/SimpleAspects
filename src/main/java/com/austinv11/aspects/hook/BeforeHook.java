package com.austinv11.aspects.hook;

import com.austinv11.aspects.bridge.ExecutionSignal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.util.concurrent.Callable;

@FunctionalInterface
public interface BeforeHook extends InvocationHook {
	
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
