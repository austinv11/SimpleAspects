package com.austinv11.aspects.hook;

import io.github.lukehutch.fastclasspathscanner.scanner.ClassInfo;
import io.github.lukehutch.fastclasspathscanner.scanner.MethodInfo;

/**
 * This is called on discovery of a method.
 */
@FunctionalInterface
public interface MethodDiscoveryHook {

    /**
     * This is called in response to the classpath scanner resolving an eligible method.
     *
     * @param fullyQualifiedClassName The fully qualified class name.
     * @param classInfo The {@link ClassInfo} object representing the class the method is in. This is not {@link Class}
     *                  in order to prevent unwanted classloading.
     * @param methodInfo The {@link MethodInfo} object representing the method found. This is not {@link java.lang.reflect.Method}
     *                  in order to prevent unwanted classloading.
     */
    void onDiscover(String fullyQualifiedClassName, ClassInfo classInfo, MethodInfo methodInfo);
}
