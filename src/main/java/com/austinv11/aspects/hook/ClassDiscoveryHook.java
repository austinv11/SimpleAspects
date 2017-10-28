package com.austinv11.aspects.hook;

import io.github.lukehutch.fastclasspathscanner.scanner.ClassInfo;

@FunctionalInterface
public interface ClassDiscoveryHook {

    void onDiscover(String fullyQualifiedClassName, ClassInfo classInfo);
}
