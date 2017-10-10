package com.austinv11.aspects.hook;

import com.austinv11.aspects.bridge.ExecutionSignal;

import java.util.concurrent.Callable;

public interface WrapHook extends BeforeHook, AfterHook {
    
    @Override
    default ExecutionSignal intercept(String clazz, Object obj, Callable<Object> zuper, Object[] args) throws Throwable {
        ExecutionSignal<?> beforeSig = before(clazz, obj, args);
        if (beforeSig.getType() != ExecutionSignal.SignalType.PASS)
            return beforeSig;
        
        ExecutionSignal<?> orig;
        try {
            orig = ExecutionSignal.returnValue(zuper.call());
        } catch (Throwable t) {
            orig = ExecutionSignal.throwException(t);
        }
        
        
        ExecutionSignal<?> afterSig = after(clazz, obj, args);
        if (afterSig.getType() == ExecutionSignal.SignalType.PASS)
            return orig;
        else
            return afterSig;
    }
}
