package com.krkarma777.springaimapper.config;

import com.krkarma777.springaimapper.factory.LlmClientFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
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

/**
 * Auto-configuration that registers {@link com.krkarma777.springaimapper.annotation.LlmClient} interfaces.
 * <p>
 * This configuration scans the classpath for interfaces annotated with {@code @LlmClient}
 * and registers a {@link LlmClientFactoryBean} for each one.
 * </p>
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

    /**
     * Scans for candidate interfaces and registers bean definitions.
     *
     * @param importingClassMetadata annotation metadata of the importing class
     * @param registry the bean definition registry
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        LlmClientComponentProvider scanner = new LlmClientComponentProvider(environment);
        scanner.setResourceLoader(resourceLoader);

        List<String> packages = AutoConfigurationPackages.get((BeanFactory) registry);
        
        if (packages.isEmpty()) {
            logger.warn("Could not determine auto-configuration package. Ensure @SpringBootApplication is present.");
        }

        for (String basePackage : packages) {
            logger.debug("Scanning for @LlmClient interfaces in package: {}", basePackage);
            
            var candidateComponents = scanner.findCandidateComponents(basePackage);

            for (var beanDefinition : candidateComponents) {
                String className = beanDefinition.getBeanClassName();
                registerLlmClientBean(registry, className);
            }
        }
    }

    /**
     * Registers a bean definition for an LlmClient interface.
     *
     * @param registry the bean definition registry
     * @param className the fully qualified class name of the interface
     */
    private void registerLlmClientBean(BeanDefinitionRegistry registry, String className) {
        try {
            Class<?> interfaceClass = ClassUtils.forName(className, classLoader);
            
            BeanDefinitionBuilder builder = BeanDefinitionBuilder
                    .genericBeanDefinition(LlmClientFactoryBean.class);
            
            builder.addConstructorArgValue(interfaceClass);
            
            String beanName = generateBeanName(interfaceClass);
            registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
            
            logger.info("Registered LlmClient bean: {} for interface: {}", beanName, className);

        } catch (ClassNotFoundException e) {
            logger.error("Failed to load class for LlmClient: {}", className, e);
        }
    }

    /**
     * Generates a bean name from the interface class name.
     * <p>
     * Converts the first character to lowercase (e.g., "GreetingService" -> "greetingService").
     * </p>
     *
     * @param interfaceClass the interface class
     * @return the generated bean name
     */
    private String generateBeanName(Class<?> interfaceClass) {
        String simpleName = interfaceClass.getSimpleName();
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }
}
