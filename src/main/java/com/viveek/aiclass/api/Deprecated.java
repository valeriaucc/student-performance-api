package com.viveek.aiclass.api;

import java.lang.annotation.*;

/**
 * Marks an API endpoint as deprecated.
 * Deprecated endpoints should include information about when they will be removed
 * and what alternative should be used.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Deprecated {

    /**
     * The version in which this API was deprecated.
     */
    String since() default "";

    /**
     * The date when this API will be removed (ISO 8601 format).
     * Example: "2025-12-31"
     */
    String sunset() default "";

    /**
     * Alternative API endpoint or version to use instead.
     */
    String alternative() default "";

    /**
     * Additional description or migration instructions.
     */
    String description() default "";
}

