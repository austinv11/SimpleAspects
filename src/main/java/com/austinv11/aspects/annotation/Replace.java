package com.austinv11.aspects.annotation;

import java.lang.annotation.*;

/**
 * This annotation is used to indicate that a method should be totally replaced with a pointcut.
 *
 * Once the method is called, arbitrary code can run in lieu of the original code.
 *
 * <b>Note:</b> You can apply this to three targets: a method, a class. Functionally, this annotation will only have an
 * effect on methods. However, placing it at the class-level will apply this annotation to all methods contained in the
 * class.
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
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Replace {
}
