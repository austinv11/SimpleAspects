package com.austinv11.aspects;

import com.austinv11.aspects.annotation.Before;
import com.austinv11.aspects.annotation.Discover;
import com.austinv11.aspects.bridge.ExecutionSignal;
import com.austinv11.aspects.hook.ClassDiscoveryHook;
import com.austinv11.aspects.hook.FieldDiscoveryHook;
import com.austinv11.aspects.hook.MethodDiscoveryHook;
import com.austinv11.aspects.inject.AspectInjector;
import io.github.lukehutch.fastclasspathscanner.scanner.ClassInfo;
import io.github.lukehutch.fastclasspathscanner.scanner.FieldInfo;
import io.github.lukehutch.fastclasspathscanner.scanner.MethodInfo;
import net.bytebuddy.agent.ByteBuddyAgent;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestInjector {

    private static final long DISCOVERY_TEST_TIME_LIMIT = 10000;

    private final AspectInjector getInjector() {
        return new AspectInjector(ByteBuddyAgent.install());
    }

    @Test
    public void testBefore() throws IOException {
        getInjector().connectBefore(Before.class, (origin, annotation, obj, args) -> ExecutionSignal.returnValue("yes"))
                .connectBefore(TestAspect.class, (origin, annotation, obj, args) -> ExecutionSignal.returnValue("no"))
                .inject();
        assertEquals(new TestClass().thing(), "yes");
        assertEquals(new TestClass().thing2(), "no");
    }

    @Test
    public void testDiscovery() throws IOException, InterruptedException {
        final List<String> discovered = Collections.synchronizedList(new ArrayList<>());

        getInjector().discoverClasses(Discover.class, new ClassDiscoveryHook() {
            @Override
            public void onDiscover(String fullyQualifiedClassName, ClassInfo classInfo) {
                discovered.add(fullyQualifiedClassName);
            }
        }).discoverFields(Discover.class, new FieldDiscoveryHook() {
            @Override
            public void onDiscover(String fullyQualifiedClassName, ClassInfo classInfo, FieldInfo fieldInfo) {
                discovered.add(fullyQualifiedClassName);
            }
        }).discoverMethods(Discover.class, new MethodDiscoveryHook() {
            @Override
            public void onDiscover(String fullyQualifiedClassName, ClassInfo classInfo, MethodInfo methodInfo) {
                discovered.add(fullyQualifiedClassName);
            }
        }).inject();

        long scanStart = System.currentTimeMillis();
        while (System.currentTimeMillis() < scanStart + DISCOVERY_TEST_TIME_LIMIT) { //Yes, I know this is a hack
            Thread.sleep(10);
        }

        assertEquals(3, discovered.size());
        assertTrue(discovered.stream().allMatch(Predicate.isEqual("com.austinv11.aspects.TestDiscovery")));
    }

    public static class TestClass {

        @Before
        public String thing() {
            return "no";
        }

        @TestAspect
        public String thing2() {
            return "yes";
        }
    }
}
