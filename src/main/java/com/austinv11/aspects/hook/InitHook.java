package com.austinv11.aspects.hook;

import com.austinv11.aspects.bridge.ExecutionSignal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.util.concurrent.Callable;

@FunctionalInterface
public interface InitHook extends InvocationHook {

    ExecutionSignal construct(Executable origin, Annotation aspect, Object obj, Object[] args) throws Throwable; //Ignore the super call b/c this should happen after everything else was constructed

    @Override
    default ExecutionSignal intercept(Executable origin, Annotation aspect, Object obj, Callable<Object> zuper, Object[] args) throws Throwable {
        zuper.call();
        return (ExecutionSignal<?>) construct(origin, aspect, obj, args);
    }
}
