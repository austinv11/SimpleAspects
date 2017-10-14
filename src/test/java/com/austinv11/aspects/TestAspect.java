package com.austinv11.aspects;

import com.austinv11.aspects.annotation.Before;

import java.lang.annotation.*;

@Before
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface TestAspect {
}
