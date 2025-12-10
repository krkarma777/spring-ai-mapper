package com.krkarma777.springaimapper.config;

import com.krkarma777.springaimapper.annotation.LlmClient;
import com.krkarma777.springaimapper.factory.LlmClientFactoryBean;
import org.springframework.ai.chat.ChatClient;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.StringUtils;

import java.util.Set;

/**
 * @LlmClient 어노테이션이 붙은 인터페이스를 자동으로 스캔하고
 * 각 인터페이스에 대한 FactoryBean을 등록하는 Auto Configuration 클래스입니다.
 */
@AutoConfiguration
@ConditionalOnClass(ChatClient.class)
public class LlmClientAutoConfiguration {

    @ConditionalOnClass(ChatClient.class)
    @org.springframework.context.annotation.Bean
    public static LlmClientBeanDefinitionRegistryPostProcessor llmClientBeanDefinitionRegistryPostProcessor() {
        return new LlmClientBeanDefinitionRegistryPostProcessor();
    }

    /**
     * BeanDefinitionRegistryPostProcessor를 사용하여 @LlmClient 인터페이스를 스캔하고 등록합니다.
     */
    static class LlmClientBeanDefinitionRegistryPostProcessor 
            implements BeanDefinitionRegistryPostProcessor, ApplicationContextAware {

        private ApplicationContext applicationContext;

        @Override
        public void setApplicationContext(ApplicationContext applicationContext) {
            this.applicationContext = applicationContext;
        }

        @Override
        public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
            // ChatClient Bean이 없으면 등록하지 않음
            if (!hasChatClient(registry)) {
                return;
            }

            // LlmClientComponentProvider를 사용하여 인터페이스 스캔
            LlmClientComponentProvider scanner = new LlmClientComponentProvider();
            
            // 기본 패키지 스캔 (전체 클래스패스 스캔은 비효율적이므로 기본 패키지만)
            String basePackage = "com.krkarma777";
            Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents(basePackage);
            
            // 각 인터페이스에 대해 FactoryBean 등록
            for (BeanDefinition beanDefinition : candidateComponents) {
                String className = beanDefinition.getBeanClassName();
                if (className != null) {
                    try {
                        Class<?> clazz = Class.forName(className);
                        if (clazz.isInterface() && clazz.isAnnotationPresent(LlmClient.class)) {
                            registerLlmClientBean(registry, clazz);
                        }
                    } catch (ClassNotFoundException e) {
                        // 클래스를 찾을 수 없는 경우 무시
                    }
                }
            }
        }

        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
            // BeanFactory 후처리는 필요 없음
        }

        /**
         * ChatClient Bean이 존재하는지 확인합니다.
         */
        private boolean hasChatClient(BeanDefinitionRegistry registry) {
            // registry에서 직접 확인
            String[] beanNames = registry.getBeanDefinitionNames();
            for (String beanName : beanNames) {
                BeanDefinition bd = registry.getBeanDefinition(beanName);
                if (bd.getBeanClassName() != null && 
                    bd.getBeanClassName().contains("ChatClient")) {
                    return true;
                }
            }
            
            // ApplicationContext에서 확인
            if (applicationContext != null) {
                try {
                    return applicationContext.getBean(ChatClient.class) != null;
                } catch (Exception e) {
                    // Bean이 없으면 false
                    return false;
                }
            }
            
            return false;
        }

        /**
         * @LlmClient 어노테이션이 붙은 인터페이스에 대한 FactoryBean을 등록합니다.
         */
        private void registerLlmClientBean(BeanDefinitionRegistry registry, Class<?> interfaceClass) {
            String beanName = generateBeanName(interfaceClass);
            
            BeanDefinitionBuilder builder = BeanDefinitionBuilder
                .genericBeanDefinition(LlmClientFactoryBean.class)
                .addConstructorArgValue(interfaceClass)
                .addConstructorArgReference("chatClient");
            
            registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
        }

        /**
         * 인터페이스 클래스로부터 Bean 이름을 생성합니다.
         */
        private String generateBeanName(Class<?> interfaceClass) {
            String simpleName = interfaceClass.getSimpleName();
            // 첫 글자를 소문자로 변환
            return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
        }
    }
}
