package com.austinv11.aspects.hook;

import com.austinv11.aspects.bridge.ExecutionSignal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.util.concurrent.Callable;

@FunctionalInterface
public interface InvocationHook {
    
    ExecutionSignal intercept(Executable origin, Annotation aspect,
                              Object obj, Callable<Object> zuper, Object[] args) throws Throwable;
}
