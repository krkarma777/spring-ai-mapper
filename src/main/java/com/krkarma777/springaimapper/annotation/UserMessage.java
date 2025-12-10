package com.krkarma777.springaimapper.annotation;

import java.lang.annotation.*;

/**
 * Defines the user prompt template for a specific method.
 * <p>
 * Supports variable interpolation using curly braces (e.g., "Hello, {name}").
 * Method arguments matching the variable names will be automatically substituted.
 * </p>
 *
 * @see Param
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UserMessage {

    /**
     * The prompt template string.
     *
     * @return the prompt template
     */
    String value();
}
