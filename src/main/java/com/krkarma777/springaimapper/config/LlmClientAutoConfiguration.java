package com.krkarma777.springaimapper.config;

import com.krkarma777.springaimapper.annotation.LlmClient;
import com.krkarma777.springaimapper.factory.LlmClientFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

import java.util.List;
import java.util.Set;

/**
 * @LlmClient 어노테이션이 붙은 인터페이스를 자동으로 스캔하고
 * 각 인터페이스에 대한 FactoryBean을 등록하는 Auto Configuration 클래스입니다.
 */
@AutoConfiguration
@ConditionalOnClass(ChatClient.class)
public class LlmClientAutoConfiguration implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware, BeanClassLoaderAware {

    private static final Logger logger = LoggerFactory.getLogger(LlmClientAutoConfiguration.class);
    
    private ResourceLoader resourceLoader;
    private Environment environment;
    private ClassLoader classLoader;

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
    
    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        // 1. 스캐너 초기화 (인터페이스 스캔 가능하도록 오버라이딩된 녀석)
        LlmClientComponentProvider scanner = new LlmClientComponentProvider(environment);
        scanner.setResourceLoader(resourceLoader);

        // 2. 사용자의 메인 패키지 위치 자동 감지 (여기가 핵심)
        // AutoConfigurationPackages.get(registry)는 @SpringBootApplication이 있는 패키지 목록을 가져온다.
        // BeanDefinitionRegistry는 BeanFactory를 상속받으므로 캐스팅 가능
        List<String> packages = AutoConfigurationPackages.get((BeanFactory) registry);
        
        if (packages.isEmpty()) {
            logger.warn("Could not determine auto-configuration package, scanning default package 'com.krkarma777' is risky.");
        }

        for (String basePackage : packages) {
            logger.debug("Scanning for @LlmClient interfaces in package: {}", basePackage);
            
            Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents(basePackage);

            for (BeanDefinition beanDefinition : candidateComponents) {
                String className = beanDefinition.getBeanClassName();
                registerLlmClientBean(registry, className);
            }
        }
    }

    private void registerLlmClientBean(BeanDefinitionRegistry registry, String className) {
        try {
            Class<?> interfaceClass = ClassUtils.forName(className, classLoader);
            
            // FactoryBean 정의 생성
            BeanDefinitionBuilder builder = BeanDefinitionBuilder
                    .genericBeanDefinition(LlmClientFactoryBean.class);
            
            // 생성자 인자: 인터페이스 타입
            builder.addConstructorArgValue(interfaceClass);
            
            // 중요: ChatClient는 이름으로 참조하지 않고, FactoryBean 내부에서 @Autowired로 주입받게 함.
            // (setAutowireMode는 Deprecated 되었지만 FactoryBean 구현체 내부에서 필드 주입이나 생성자 주입을 유도해야 함)
            // 여기서는 FactoryBean 정의 자체를 등록하므로, FactoryBean이 빈으로 생성될 때 스프링이 알아서 의존성을 주입해줌.
            
            // 빈 등록
            String beanName = generateBeanName(interfaceClass);
            registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
            
            logger.info("Registered LlmClient bean: {} for interface: {}", beanName, className);

        } catch (ClassNotFoundException e) {
            logger.error("Failed to load class for LlmClient: {}", className, e);
        }
    }

    private String generateBeanName(Class<?> interfaceClass) {
        String simpleName = interfaceClass.getSimpleName();
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }
}
