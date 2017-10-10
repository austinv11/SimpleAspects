package com.austinv11.aspects.hook;

import com.austinv11.aspects.bridge.ExecutionSignal;

import java.util.concurrent.Callable;

@FunctionalInterface
public interface BeforeHook extends Hook {
	
    ExecutionSignal before(String clazz, Object obj, Object[] args) throws Throwable;
	
    @Override
    default ExecutionSignal intercept(String clazz, Object obj, Callable<Object> zuper, Object[] args) throws Throwable {
        ExecutionSignal<?> sig = before(clazz, obj, args);
        if (sig.getType() == ExecutionSignal.SignalType.PASS)
            return ExecutionSignal.returnValue(zuper.call());
        else
            return sig;
	}
}
