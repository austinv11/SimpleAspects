package com.austinv11.aspects.hook;

import java.lang.annotation.Annotation;

@FunctionalInterface
public interface ValueHook {

    Object intercept(Class<?> clazz, Annotation annotation, Object thiz, Object param);
}
