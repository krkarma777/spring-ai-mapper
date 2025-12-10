package com.krkarma777.springaimapper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 메서드 레벨 어노테이션으로 사용자 메시지(프롬프트 템플릿)를 정의합니다.
 * 플레이스홀더를 사용하여 메서드 파라미터를 바인딩할 수 있습니다.
 * 
 * 예시: @UserMessage("Hello, {name}! How are you?")
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UserMessage {
    /**
     * 프롬프트 템플릿 문자열
     * 플레이스홀더는 {parameterName} 형태로 지정합니다.
     * 
     * @return 프롬프트 템플릿
     */
    String value();
}

