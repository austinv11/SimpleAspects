package com.austinv11.aspects;

import com.austinv11.aspects.annotation.Before;
import com.austinv11.aspects.bridge.ExecutionSignal;
import com.austinv11.aspects.hook.Hook;
import com.austinv11.aspects.inject.AspectInjector;
import net.bytebuddy.agent.ByteBuddyAgent;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class TestInjector {

    private final AspectInjector getInjector() {
        return new AspectInjector(ByteBuddyAgent.install());
    }

    @Test
    public void testBefore() throws IOException {
        getInjector().connect(Before.class, (Hook) (clazz, obj, args) -> ExecutionSignal.returnValue("yes"))
                .connect(TestAspect.class, (Hook) (clazz, obj, args) -> ExecutionSignal.returnValue("no"))
                .inject();
        assertEquals(new TestClass().thing(), "yes");
        assertEquals(new TestClass().thing2(), "no");
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
