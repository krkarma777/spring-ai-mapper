package com.krkarma777.springaimapper.factory;

import com.krkarma777.springaimapper.proxy.LlmClientInvocationHandler;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Proxy;

/**
 * @LlmClient 어노테이션이 붙은 인터페이스에 대한 프록시를 생성하는 FactoryBean입니다.
 * Java Dynamic Proxy를 사용하여 인터페이스의 구현체를 동적으로 생성합니다.
 */
public class LlmClientFactoryBean<T> implements FactoryBean<T> {

    private final Class<T> interfaceType;
    
    // [핵심 변경] ChatClient 대신 Builder를 주입받음 (Spring AI 1.x 표준)
    @Autowired
    private ChatClient.Builder chatClientBuilder;

    public LlmClientFactoryBean(Class<T> interfaceType) {
        this.interfaceType = interfaceType;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getObject() {
        // Builder를 사용해 이 인터페이스 전용 클라이언트 생성
        ChatClient chatClient = chatClientBuilder.build();
        
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

