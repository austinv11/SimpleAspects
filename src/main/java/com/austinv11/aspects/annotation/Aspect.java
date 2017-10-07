package com.austinv11.aspects.annotation;

import java.lang.annotation.*;

/**
 * This annotation is annotation marks another annotation as a "meta aspect annotation". This means that SimpleAspects
 * will allow that annotation to be "inherited".
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Aspect {
}
