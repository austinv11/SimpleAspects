package com.austinv11.aspects;

import com.austinv11.aspects.annotation.Before;
import com.austinv11.aspects.bridge.ExecutionSignal;
import com.austinv11.aspects.hook.BeforeHook;
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
        getInjector().connect(Before.class, (BeforeHook) (clazz, obj, args) -> ExecutionSignal.returnValue("yes")).inject();
        assertEquals(new TestClass().thing(), "yes");
    }

    public static class TestClass {

        @Before
        public String thing() {
            return "no";
        }
    }
}
