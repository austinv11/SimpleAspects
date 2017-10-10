package com.austinv11.aspects.hook;

import com.austinv11.aspects.bridge.ExecutionSignal;

import java.util.concurrent.Callable;

@FunctionalInterface
public interface AfterHook extends Hook {
    
    ExecutionSignal after(String clazz, Object obj, Object[] args) throws Throwable;
    
    @Override
    default ExecutionSignal intercept(String clazz, Object obj, Callable<Object> zuper, Object[] args) throws Throwable {
        ExecutionSignal<?> orig;
        try {
            orig = ExecutionSignal.returnValue(zuper.call());
        } catch (Throwable t) {
            orig = ExecutionSignal.throwException(t);
        }
        ExecutionSignal<?> sig = after(clazz, obj, args);
        if (sig.getType() == ExecutionSignal.SignalType.PASS)
            return orig;
        else
            return sig;
    }
}
