package com.austinv11.aspects.annotation;

import java.lang.annotation.*;

/**
 * This annotation is used to indicate that a field should be injected with a value at runtime.
 *
 * <b>Warning:</b> This annotation should not be used in its current state, it should be inherited by another. This
 * prevents clashes.
 *
 * @see com.austinv11.aspects.inject.AspectInjector
 */
@Aspect
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Fill {
}
