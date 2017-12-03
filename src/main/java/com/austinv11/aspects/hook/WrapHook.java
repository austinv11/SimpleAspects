package com.austinv11.aspects.hook;

import com.austinv11.aspects.bridge.ExecutionSignal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.util.concurrent.Callable;

/**
 * This represents a hook called before and after execution of a method.
 *
 * This corresponds to the {@link com.austinv11.aspects.annotation.Wrap} annotation.
 *
 * @see BeforeHook
 * @see AfterHook
 */
public interface WrapHook extends BeforeHook, AfterHook {
    
    @Override
    default ExecutionSignal intercept(Executable origin, Annotation aspect, Object obj, Callable<Object> zuper, Object[] args) throws Throwable {
        ExecutionSignal<?> beforeSig = before(origin, aspect, obj, args);
        if (beforeSig.getType() != ExecutionSignal.SignalType.PASS)
            return beforeSig;
        
        ExecutionSignal<?> orig;
        try {
            orig = ExecutionSignal.returnValue(zuper.call());
        } catch (Throwable t) {
            orig = ExecutionSignal.throwException(t);
        }
        
        
        ExecutionSignal<?> afterSig = after(origin, aspect, obj, args, orig);
        if (afterSig.getType() == ExecutionSignal.SignalType.PASS)
            return orig;
        else
            return afterSig;
    }
}
