package com.krkarma777.springaimapper.config;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import com.krkarma777.springaimapper.annotation.LlmClient;

/**
 * @LlmClient 어노테이션이 붙은 인터페이스를 스캔하기 위한 ComponentProvider.
 * 기본 ClassPathScanningCandidateComponentProvider는 인터페이스를 제외하므로,
 * isCandidateComponent를 오버라이드하여 인터페이스도 감지하도록 구현합니다.
 */
public class LlmClientComponentProvider extends ClassPathScanningCandidateComponentProvider {

    public LlmClientComponentProvider() {
        super(false);
        addIncludeFilter(new AnnotationTypeFilter(LlmClient.class));
    }

    /**
     * 인터페이스도 후보 컴포넌트로 인식하도록 오버라이드합니다.
     * 기본 구현은 인터페이스를 제외하지만, @LlmClient가 붙은 인터페이스는 허용합니다.
     */
    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        AnnotationMetadata metadata = beanDefinition.getMetadata();
        // 인터페이스이면서 @LlmClient 어노테이션이 있는 경우 허용
        return metadata.isInterface() && metadata.hasAnnotation(LlmClient.class.getName());
    }
}

