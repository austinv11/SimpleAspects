package com.austinv11.aspects.annotation;

import java.lang.annotation.*;

/**
 * This annotation is used to indicate that a method or field should be "discovered" at runtime.
 *
 * Once the method is called, it is possible to execute arbitrary logic before the method has executed.
 *
 * <b>Note:</b> You can apply this to three targets: a method, a class, or a field. Placing it at the class-level will
 * apply this annotation to all methods and fields contained in the class.
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
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.FIELD})
public @interface Discover {
}
