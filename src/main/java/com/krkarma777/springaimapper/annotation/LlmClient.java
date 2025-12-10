package com.krkarma777.springaimapper.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Marks an interface as a declarative LLM client.
 * <p>
 * Interfaces annotated with this will be automatically scanned and implemented by the library.
 * The implementation uses Spring AI's ChatClient to communicate with LLM models.
 * </p>
 *
 * @author krkarma777
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LlmClient {

    /**
     * The model name to be used for this client (e.g., "gpt-4", "gpt-3.5-turbo").
     * If empty, the default model configured in application properties will be used.
     *
     * @return the model name
     */
    @AliasFor("value")
    String model() default "";

    /**
     * Alias for {@link #model()}.
     *
     * @return the model name
     */
    @AliasFor("model")
    String value() default "";
}
