package com.austinv11.aspects.hook;

import com.austinv11.aspects.bridge.ExecutionSignal;

import java.util.concurrent.Callable;

@FunctionalInterface
public interface Hook {
    
    ExecutionSignal intercept(String clazz, Object obj, Callable<Object> zuper, Object[] args) throws Throwable;
}
