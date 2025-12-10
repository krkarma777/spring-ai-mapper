package com.krkarma777.springaimapper.factory;

import com.krkarma777.springaimapper.proxy.LlmClientInvocationHandler;
import org.springframework.ai.chat.ChatClient;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Proxy;

/**
 * @LlmClient 어노테이션이 붙은 인터페이스에 대한 프록시를 생성하는 FactoryBean입니다.
 * Java Dynamic Proxy를 사용하여 인터페이스의 구현체를 동적으로 생성합니다.
 */
public class LlmClientFactoryBean<T> implements FactoryBean<T> {

    private final Class<T> interfaceType;
    private final ChatClient chatClient;

    public LlmClientFactoryBean(Class<T> interfaceType, ChatClient chatClient) {
        this.interfaceType = interfaceType;
        this.chatClient = chatClient;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getObject() {
        return (T) Proxy.newProxyInstance(
            interfaceType.getClassLoader(),
            new Class<?>[]{interfaceType},
            new LlmClientInvocationHandler(chatClient, interfaceType)
        );
    }

    @Override
    public Class<?> getObjectType() {
        return interfaceType;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}

