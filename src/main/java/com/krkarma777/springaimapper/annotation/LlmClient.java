package com.krkarma777.springaimapper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 인터페이스를 LLM 클라이언트로 표시하는 어노테이션.
 * 이 어노테이션이 붙은 인터페이스는 자동으로 프록시가 생성되어 LLM 호출이 가능합니다.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface LlmClient {
    /**
     * 사용할 LLM 모델명
     * @return 모델명 (예: "gpt-4", "gpt-3.5-turbo")
     */
    String model() default "";
}

