package com.austinv11.aspects.annotation;

import java.lang.annotation.*;

/**
 * This annotation is used to indicate that a field, method or class be automatically discovered via classpath scanning
 * on {@link com.austinv11.aspects.inject.AspectInjector#inject()}.
 *
 * Once the method is called, it is possible to execute arbitrary logic after the method has executed.
 *
 * @see com.austinv11.aspects.inject.AspectInjector
 */
@Aspect
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
public @interface Discover {
}
