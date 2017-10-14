package com.austinv11.aspects.hook;

import com.austinv11.aspects.bridge.ExecutionSignal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.util.concurrent.Callable;

@FunctionalInterface
public interface AfterHook extends InvocationHook {
    
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
