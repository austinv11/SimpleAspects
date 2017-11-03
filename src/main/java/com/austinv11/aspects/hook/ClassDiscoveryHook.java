package com.austinv11.aspects.hook;

import io.github.lukehutch.fastclasspathscanner.scanner.ClassInfo;

/**
 * This is called on discovery of a class.
 */
@FunctionalInterface
public interface ClassDiscoveryHook {

    /**
     * This is called in response to the classpath scanner resolving an eligible class.
     *
     * @param fullyQualifiedClassName The fully qualified class name.
     * @param classInfo The {@link ClassInfo} object representing the class. This is not {@link Class} in order to
     *                  prevent unwanted classloading.
     */
    void onDiscover(String fullyQualifiedClassName, ClassInfo classInfo);
}
