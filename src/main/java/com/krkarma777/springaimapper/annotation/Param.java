package com.krkarma777.springaimapper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 메서드 파라미터에 붙이는 어노테이션으로, 파라미터 이름을 명시적으로 지정합니다.
 * Java 컴파일 시 파라미터 이름이 손실되는 문제를 방지하기 위해 사용합니다.
 * 
 * 예시: void greet(@Param("name") String name)
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Param {
    /**
     * 파라미터 이름
     * @return 파라미터 이름
     */
    String value();
}

