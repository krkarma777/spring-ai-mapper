package com.krkarma777.springaimapper.proxy;

import com.krkarma777.springaimapper.annotation.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles method invocations for {@link com.krkarma777.springaimapper.annotation.LlmClient} interfaces.
 * <p>
 * This handler intercepts the method call, constructs a prompt using the template,
 * sends it to the LLM via {@link ChatClient}, and converts the response into the return type.
 * </p>
 */
public class LlmClientInvocationHandler implements InvocationHandler {

    private static final Logger logger = LoggerFactory.getLogger(LlmClientInvocationHandler.class);
    
    private final ChatClient chatClient;
    private final Class<?> interfaceType;
    private final String systemMessage;
    private final String modelName;

    /**
     * Creates a new invocation handler.
     *
     * @param chatClient the ChatClient instance to use for LLM calls
     * @param interfaceType the interface type being proxied
     * @param modelName the model name to use (if specified in @LlmClient annotation)
     */
    public LlmClientInvocationHandler(ChatClient chatClient, Class<?> interfaceType, String modelName) {
        this.chatClient = chatClient;
        this.interfaceType = interfaceType;
        this.systemMessage = extractSystemMessage();
        this.modelName = modelName;
    }

    /**
     * Intercepts the method call and executes the LLM request.
     *
     * @param proxy the proxy instance
     * @param method the method being invoked
     * @param args the method arguments
     * @return the LLM response converted to the method's return type
     * @throws Throwable if an error occurs during invocation
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
        }

        com.krkarma777.springaimapper.annotation.UserMessage userMessageAnnotation = 
            method.getAnnotation(com.krkarma777.springaimapper.annotation.UserMessage.class);
        
        if (userMessageAnnotation == null) {
            logger.warn("Method {} is missing @UserMessage annotation.", method.getName());
            return null; 
        }

        String promptTemplate = userMessageAnnotation.value();
        Map<String, Object> variables = buildParameterMap(method, args);
        Class<?> returnType = method.getReturnType();

        BeanOutputConverter<?> converter = null;
        String formatInstruction = "";
        
        // Prepare BeanOutputConverter if return type is a complex object
        if (!isSimpleType(returnType)) {
            converter = new BeanOutputConverter<>(returnType);
            formatInstruction = converter.getFormat();
        }

        Prompt prompt = createPrompt(promptTemplate, variables, formatInstruction);

        // Execute LLM call using Fluent API
        // Model is already configured in ChatClient.Builder via defaultOptions
        String responseContent = chatClient.prompt(prompt).call().content();

        return convertResponse(responseContent, returnType, converter);
    }

    /**
     * Builds a parameter map from method parameters and arguments.
     * <p>
     * Uses {@link Param} annotation if present, otherwise falls back to reflection-based name extraction.
     * </p>
     *
     * @param method the method
     * @param args the method arguments
     * @return a map of parameter names to values
     */
    private Map<String, Object> buildParameterMap(Method method, Object[] args) {
        Map<String, Object> variables = new HashMap<>();
        Parameter[] parameters = method.getParameters();
        
        if (args != null) {
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                String paramName = getParameterName(parameter, i);
                variables.put(paramName, args[i]);
            }
        }
        return variables;
    }

    /**
     * Extracts the parameter name from a parameter.
     * <p>
     * Priority order:
     * <ol>
     *   <li>{@link Param} annotation value</li>
     *   <li>Reflection-based parameter name (requires {@code -parameters} compiler flag)</li>
     *   <li>Index-based fallback</li>
     * </ol>
     * </p>
     *
     * @param parameter the parameter
     * @param index the parameter index
     * @return the parameter name
     */
    private String getParameterName(Parameter parameter, int index) {
        Param paramAnnotation = parameter.getAnnotation(Param.class);
        if (paramAnnotation != null && StringUtils.hasText(paramAnnotation.value())) {
            return paramAnnotation.value();
        }
        String name = parameter.getName();
        if (name != null && !name.startsWith("arg")) {
            return name;
        }
        return String.valueOf(index);
    }

    /**
     * Creates a prompt from the template and variables.
     * <p>
     * Includes system message if present, and appends format instructions for complex return types.
     * </p>
     *
     * @param promptTemplate the prompt template string
     * @param variables the variables to substitute
     * @param formatInstruction the format instruction for output conversion
     * @return the constructed prompt
     */
    private Prompt createPrompt(String promptTemplate, Map<String, Object> variables, String formatInstruction) {
        List<Message> messages = new ArrayList<>();

        if (StringUtils.hasText(systemMessage)) {
            messages.add(new SystemMessage(systemMessage));
        }

        PromptTemplate template = new PromptTemplate(promptTemplate);
        String userMessageText = template.render(variables);

        if (StringUtils.hasText(formatInstruction)) {
            userMessageText += "\n\n" + formatInstruction;
        }

        messages.add(new UserMessage(userMessageText));
        return new Prompt(messages);
    }

    /**
     * Extracts the system message from the interface-level {@link com.krkarma777.springaimapper.annotation.SystemMessage} annotation.
     *
     * @return the system message, or null if not present
     */
    private String extractSystemMessage() {
        com.krkarma777.springaimapper.annotation.SystemMessage systemMessageAnnotation = 
            interfaceType.getAnnotation(com.krkarma777.springaimapper.annotation.SystemMessage.class);
        return systemMessageAnnotation != null ? systemMessageAnnotation.value() : null;
    }

    /**
     * Converts the LLM response text to the method's return type.
     *
     * @param responseText the raw response text from the LLM
     * @param returnType the expected return type
     * @param converter the converter to use for complex types
     * @return the converted response object
     */
    private Object convertResponse(String responseText, Class<?> returnType, BeanOutputConverter<?> converter) {
        if (returnType == void.class || returnType == Void.class) {
            return null;
        }
        if (returnType == String.class) {
            return responseText;
        }
        return converter.convert(responseText);
    }

    /**
     * Checks if a type is a simple type that doesn't require conversion.
     * <p>
     * Simple types include: String, void, primitives, and Number subclasses.
     * </p>
     *
     * @param type the type to check
     * @return true if the type is simple
     */
    private boolean isSimpleType(Class<?> type) {
        return type == String.class || type == void.class || type == Void.class || 
               type.isPrimitive() || Number.class.isAssignableFrom(type);
    }
}
