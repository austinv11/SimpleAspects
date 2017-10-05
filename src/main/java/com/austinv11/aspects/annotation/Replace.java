package com.austinv11.aspects.annotation;

import java.lang.annotation.*;

/**
 * This annotation is used to indicate that a method should be totally replaced with a pointcut.
 *
 * Once the method is called, arbitrary code can run in lieu of the original code.
 *
 * <b>Note:</b> You can apply this to three targets: a method, a class, or a package. Functionally, this annotation will
 * only have an effect on methods. However, placing it at the class-level will apply this annotation to all methods
 * contained in the class. Additionally, placing the annotation at the package-level will apply this annotation to all
 * methods contained in the current package.
 *
 * @see com.austinv11.aspects.inject.Pointcut
 * @see com.austinv11.aspects.inject.AspectInjector
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.PACKAGE})
public @interface Replace {
}