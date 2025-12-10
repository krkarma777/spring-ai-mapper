package com.krkarma777.springaimapper.config;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import com.krkarma777.springaimapper.annotation.LlmClient;

/**
 * Custom component provider to scan for interfaces annotated with {@link LlmClient}.
 * <p>
 * By default, Spring's ClassPathScanningCandidateComponentProvider does not detect interfaces.
 * This class overrides {@link #isCandidateComponent(AnnotatedBeanDefinition)} to allow them.
 * </p>
 */
public class LlmClientComponentProvider extends ClassPathScanningCandidateComponentProvider {

    /**
     * Creates a new provider with the given environment.
     *
     * @param environment the Spring environment
     */
    public LlmClientComponentProvider(Environment environment) {
        super(false, environment);
        addIncludeFilter(new AnnotationTypeFilter(LlmClient.class));
    }

    /**
     * Determines whether the given bean definition qualifies as a candidate component.
     * <p>
     * Overridden to allow interfaces annotated with {@code @LlmClient}.
     * </p>
     *
     * @param beanDefinition the bean definition to check
     * @return true if the bean definition is an interface and has the required annotation
     */
    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        AnnotationMetadata metadata = beanDefinition.getMetadata();
        return metadata.isInterface() && metadata.hasAnnotation(LlmClient.class.getName());
    }
}
