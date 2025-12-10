package com.krkarma777.springaimapper.annotation;

import java.lang.annotation.*;

/**
 * Explicitly specifies the parameter name for prompt template variable binding.
 * <p>
 * Use this annotation when parameter names are lost during compilation
 * (e.g., when not using {@code -parameters} compiler flag).
 * </p>
 *
 * @see UserMessage
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Param {

    /**
     * The parameter name to be used in the prompt template.
     *
     * @return the parameter name
     */
    String value();
}
