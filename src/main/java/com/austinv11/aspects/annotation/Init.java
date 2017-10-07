package com.austinv11.aspects.annotation;

import java.lang.annotation.*;

/**
 * This annotation is used to indicate that a constructor should be injected with a pointcut after it completes.
 *
 * Once the constructor is called, it is possible to execute arbitrary logic after the object has been initialized.
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
@Target({ElementType.CONSTRUCTOR})
public @interface Init {
}
