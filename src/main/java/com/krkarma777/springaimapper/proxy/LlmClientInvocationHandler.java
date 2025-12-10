package com.krkarma777.springaimapper.proxy;

import com.krkarma777.springaimapper.annotation.Param;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.output.BeanOutputConverter;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LLM 클라이언트 인터페이스의 메서드 호출을 인터셉트하여
 * 실제 LLM 호출로 변환하는 InvocationHandler입니다.
 */
public class LlmClientInvocationHandler implements InvocationHandler {

    private final ChatClient chatClient;
    private final Class<?> interfaceType;
    private final String systemMessage;

    public LlmClientInvocationHandler(ChatClient chatClient, Class<?> interfaceType) {
        this.chatClient = chatClient;
        this.interfaceType = interfaceType;
        this.systemMessage = extractSystemMessage();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Object 메서드는 직접 처리
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
        }

        // @UserMessage 어노테이션 확인
        com.krkarma777.springaimapper.annotation.UserMessage userMessageAnnotation = 
            method.getAnnotation(com.krkarma777.springaimapper.annotation.UserMessage.class);
        if (userMessageAnnotation == null) {
            throw new IllegalStateException(
                "Method " + method.getName() + " must be annotated with @UserMessage");
        }

        // 프롬프트 템플릿 추출
        String promptTemplate = userMessageAnnotation.value();
        
        // 파라미터 이름 매핑 생성
        Map<String, Object> variables = buildParameterMap(method, args);
        
        // 프롬프트 생성
        Prompt prompt = createPrompt(promptTemplate, variables);
        
        // LLM 호출
        ChatResponse response = chatClient.call(prompt);
        
        // 응답 변환
        return convertResponse(response, method.getReturnType());
    }

    /**
     * 메서드 파라미터를 이름-값 맵으로 변환합니다.
     * @Param 어노테이션을 우선적으로 사용하고, 없으면 리플렉션으로 파라미터명을 추출합니다.
     */
    private Map<String, Object> buildParameterMap(Method method, Object[] args) {
        Map<String, Object> variables = new HashMap<>();
        Parameter[] parameters = method.getParameters();
        
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            String paramName = getParameterName(parameter, i);
            variables.put(paramName, args[i]);
        }
        
        return variables;
    }

    /**
     * 파라미터 이름을 추출합니다.
     * 1. @Param 어노테이션 우선
     * 2. Parameter.getName() (컴파일 시 -parameters 옵션 필요)
     * 3. 인덱스 기반 fallback
     */
    private String getParameterName(Parameter parameter, int index) {
        // @Param 어노테이션 확인
        Param paramAnnotation = parameter.getAnnotation(Param.class);
        if (paramAnnotation != null && StringUtils.hasText(paramAnnotation.value())) {
            return paramAnnotation.value();
        }
        
        // 리플렉션으로 파라미터명 추출 시도
        String name = parameter.getName();
        if (name != null && !name.equals("arg" + index)) {
            return name;
        }
        
        // Fallback: 인덱스 기반
        return String.valueOf(index);
    }

    /**
     * 프롬프트를 생성합니다.
     * 시스템 메시지와 사용자 메시지를 포함합니다.
     */
    private Prompt createPrompt(String promptTemplate, Map<String, Object> variables) {
        List<Message> messages = new ArrayList<>();
        
        // 시스템 메시지 추가 (있는 경우)
        if (StringUtils.hasText(systemMessage)) {
            messages.add(new SystemMessage(systemMessage));
        }
        
        // 사용자 메시지 생성 (플레이스홀더 치환)
        PromptTemplate template = new PromptTemplate(promptTemplate);
        String userMessageText = template.render(variables);
        messages.add(new UserMessage(userMessageText));
        
        return new Prompt(messages);
    }

    /**
     * 인터페이스 레벨의 @SystemMessage 어노테이션에서 시스템 메시지를 추출합니다.
     */
    private String extractSystemMessage() {
        com.krkarma777.springaimapper.annotation.SystemMessage systemMessageAnnotation = 
            interfaceType.getAnnotation(com.krkarma777.springaimapper.annotation.SystemMessage.class);
        if (systemMessageAnnotation != null) {
            return systemMessageAnnotation.value();
        }
        return null;
    }

    /**
     * LLM 응답을 메서드의 반환 타입으로 변환합니다.
     */
    private Object convertResponse(ChatResponse response, Class<?> returnType) {
        String responseText = response.getResult().getOutput().getContent();
        
        // void 반환 타입
        if (returnType == void.class || returnType == Void.class) {
            return null;
        }
        
        // String 반환 타입
        if (returnType == String.class) {
            return responseText;
        }
        
        // 기타 타입: BeanOutputConverter 사용
        BeanOutputConverter<?> converter = new BeanOutputConverter<>(returnType);
        return converter.convert(responseText);
    }
}

