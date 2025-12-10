package com.krkarma777.springaimapper.annotation;

import java.lang.annotation.*;

/**
 * Defines a system message at the interface level.
 * <p>
 * This annotation is optional and can be used to provide role instructions
 * or context to the LLM for all methods in the interface.
 * </p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SystemMessage {

    /**
     * The system message content.
     *
     * @return the system message
     */
    String value();
}
