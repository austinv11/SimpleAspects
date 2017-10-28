package com.austinv11.aspects.hook;

import io.github.lukehutch.fastclasspathscanner.scanner.ClassInfo;
import io.github.lukehutch.fastclasspathscanner.scanner.MethodInfo;

@FunctionalInterface
public interface MethodDiscoveryHook {

    void onDiscover(String fullyQualifiedClassName, ClassInfo classInfo, MethodInfo methodInfo);
}
