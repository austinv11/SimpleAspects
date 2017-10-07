package com.austinv11.aspects.annotation;

import java.lang.annotation.*;

/**
 * This annotation is used to indicate that a parameter should be injected with a pointcut.
 *
 * Once the method is called, it is possible to execute arbitrary logic with this specific parameter before the method
 * is executed (like mutating or logging the parameter value, for example).
 *
 * <b>Note:</b> You can apply this to three targets: a parameter, a method, or a constructor. Functionally, this
 * annotation will only have an effect on parameters. However, placing it at the method or constructor level will apply
 * this annotation to all parameters contained in the method/constructor.
 *
 * <b>Warning:</b> This annotation should not be used in its current state, it should be inherited by another. This
 * prevents clashes.
 *
 * @see com.austinv11.aspects.inject.Pointcut
 * @see com.austinv11.aspects.inject.AspectInjector
 */
@Aspect
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface Encapsulate {
}
