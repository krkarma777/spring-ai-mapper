package com.krkarma777.springaimapper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 인터페이스 레벨 어노테이션으로 시스템 메시지를 정의합니다.
 * 이 어노테이션은 선택사항이며, LLM에게 역할이나 지시사항을 제공할 때 사용합니다.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SystemMessage {
    /**
     * 시스템 메시지 내용
     * @return 시스템 메시지
     */
    String value() default "";
}

