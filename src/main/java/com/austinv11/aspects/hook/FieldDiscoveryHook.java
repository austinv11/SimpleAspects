package com.austinv11.aspects.hook;

import io.github.lukehutch.fastclasspathscanner.scanner.ClassInfo;
import io.github.lukehutch.fastclasspathscanner.scanner.FieldInfo;

@FunctionalInterface
public interface FieldDiscoveryHook {

    void onDiscover(String fullyQualifiedClassName, ClassInfo classInfo, FieldInfo fieldInfo);
}
