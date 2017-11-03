package com.austinv11.aspects.hook;

import io.github.lukehutch.fastclasspathscanner.scanner.ClassInfo;
import io.github.lukehutch.fastclasspathscanner.scanner.FieldInfo;

/**
 * This is called on discovery of a field.
 */
@FunctionalInterface
public interface FieldDiscoveryHook {

    /**
     * This is called in response to the classpath scanner resolving an eligible field.
     *
     * @param fullyQualifiedClassName The fully qualified class name.
     * @param classInfo The {@link ClassInfo} object representing the class the field is in. This is not {@link Class}
     *                  in order to prevent unwanted classloading.
     * @param fieldInfo The {@link FieldInfo} object representing the field found. This is not {@link java.lang.reflect.Field}
     *                  in order to prevent unwanted classloading.
     */
    void onDiscover(String fullyQualifiedClassName, ClassInfo classInfo, FieldInfo fieldInfo);
}
