package com.austinv11.aspects.hook;

import com.austinv11.aspects.bridge.ExecutionSignal;

@FunctionalInterface
public interface BeforeHook {

    ExecutionSignal before(String clazz, Object obj, Object[] args);
}
