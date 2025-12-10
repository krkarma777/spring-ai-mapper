package com.krkarma777.springaimapper.example;

import com.krkarma777.springaimapper.annotation.LlmClient;
import com.krkarma777.springaimapper.annotation.Param;
import com.krkarma777.springaimapper.annotation.SystemMessage;
import com.krkarma777.springaimapper.annotation.UserMessage;

/**
 * LLM 클라이언트 사용 예제 인터페이스.
 * @LlmClient, @SystemMessage, @UserMessage, @Param 어노테이션의 사용법을 보여줍니다.
 */
@LlmClient(model = "gpt-3.5-turbo")
@SystemMessage("You are a friendly assistant that greets people warmly.")
public interface GreetingService {

    /**
     * 간단한 인사 메시지를 생성합니다.
     * @Param 어노테이션을 사용하여 파라미터 이름을 명시적으로 지정합니다.
     */
    @UserMessage("Hello, {name}! How are you today?")
    String greet(@Param("name") String name);

    /**
     * 여러 파라미터를 사용하는 예제.
     * 파라미터 이름이 컴파일 시 보존되는 경우 @Param을 생략할 수 있습니다.
     */
    @UserMessage("Hello, {name}! You are {age} years old and from {city}.")
    String greetWithDetails(String name, int age, String city);

    /**
     * @Param을 사용하여 파라미터 이름을 명시적으로 지정하는 예제.
     */
    @UserMessage("Create a personalized greeting for {personName} who is {personAge} years old.")
    String personalizedGreeting(@Param("personName") String name, @Param("personAge") int age);
}

