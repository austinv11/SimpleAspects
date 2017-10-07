package com.austinv11.aspects.annotation;

import java.lang.annotation.*;

/**
 * This annotation is used to indicate that a method should be injected with an after pointcut.
 *
 * Once the method is called, it is possible to execute arbitrary logic after the method has executed.
 *
 * <b>Note:</b> You can apply this to three targets: a method, a class, or a package. Functionally, this annotation will
 * only have an effect on methods. However, placing it at the class-level will apply this annotation to all methods
 * contained in the class. Additionally, placing the annotation at the package-level will apply this annotation to all
 * methods contained in the current package.
 *
 * <b>Warning:</b> This annotation should not be used in its current state, it should be inherited by another. This
 * prevents clashes.
 *
 * @see Before
 * @see Wrap
 * @see com.austinv11.aspects.inject.Pointcut
 * @see com.austinv11.aspects.inject.AspectInjector
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface After {
}
