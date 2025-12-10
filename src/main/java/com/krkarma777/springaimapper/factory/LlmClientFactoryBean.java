package com.krkarma777.springaimapper.factory;

import com.krkarma777.springaimapper.annotation.LlmClient;
import com.krkarma777.springaimapper.proxy.LlmClientInvocationHandler;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.lang.reflect.Proxy;

/**
 * A {@link FactoryBean} that creates a dynamic proxy for an {@link com.krkarma777.springaimapper.annotation.LlmClient} interface.
 * <p>
 * It injects a {@link ChatClient.Builder} to create a dedicated ChatClient instance for the proxy.
 * </p>
 *
 * @param <T> the interface type
 */
public class LlmClientFactoryBean<T> implements FactoryBean<T> {

    private final Class<T> interfaceType;
    
    @Autowired
    private ChatClient.Builder chatClientBuilder;

    /**
     * Creates a new factory bean for the given interface type.
     *
     * @param interfaceType the interface class to create a proxy for
     */
    public LlmClientFactoryBean(Class<T> interfaceType) {
        this.interfaceType = interfaceType;
    }

    /**
     * Creates and returns the proxy instance.
     *
     * @return the proxy instance implementing the interface
     */
    @Override
    @SuppressWarnings("unchecked")
    public T getObject() {
        // Extract model name from @LlmClient annotation
        LlmClient annotation = interfaceType.getAnnotation(LlmClient.class);
        String modelName = (annotation != null) ? annotation.model() : "";
        
        // Build ChatClient with model-specific options if model name is specified
        ChatClient.Builder builder = chatClientBuilder;
        if (StringUtils.hasText(modelName)) {
            builder = builder.defaultOptions(
                OpenAiChatOptions.builder()
                    .withModel(modelName)
                    .build()
            );
        }
        
        ChatClient chatClient = builder.build();
        
        return (T) Proxy.newProxyInstance(
            interfaceType.getClassLoader(),
            new Class<?>[]{interfaceType},
            new LlmClientInvocationHandler(chatClient, interfaceType, modelName)
        );
    }

    /**
     * Returns the interface type.
     *
     * @return the interface class
     */
    @Override
    public Class<?> getObjectType() {
        return interfaceType;
    }

    /**
     * Returns true, indicating this factory creates singleton beans.
     *
     * @return true
     */
    @Override
    public boolean isSingleton() {
        return true;
    }
}
