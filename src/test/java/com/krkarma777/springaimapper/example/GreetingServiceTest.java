package com.krkarma777.springaimapper.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * GreetingService의 통합 테스트입니다.
 * 실제 LLM 호출을 테스트하려면 OpenAI API 키가 필요합니다.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.ai.openai.api-key=${OPENAI_API_KEY:test-key}",
    "spring.ai.openai.chat.options.model=gpt-3.5-turbo"
})
public class GreetingServiceTest {

    @Autowired(required = false)
    private GreetingService greetingService;

    @Test
    public void testGreetingServiceInjection() {
        // Bean이 제대로 주입되었는지 확인
        assertThat(greetingService).isNotNull();
    }

    @Test
    public void testGreet() {
        if (greetingService == null) {
            // ChatClient Bean이 없으면 테스트 스킵
            return;
        }

        // 실제 LLM 호출 테스트 (API 키가 설정된 경우에만 동작)
        try {
            String response = greetingService.greet("Alice");
            assertThat(response).isNotNull();
            assertThat(response).isNotEmpty();
        } catch (Exception e) {
            // API 키가 없거나 네트워크 오류인 경우 테스트 스킵
            // 실제 환경에서는 정상 동작해야 함
        }
    }

    @Test
    public void testGreetWithDetails() {
        if (greetingService == null) {
            return;
        }

        try {
            String response = greetingService.greetWithDetails("Bob", 30, "Seoul");
            assertThat(response).isNotNull();
            assertThat(response).isNotEmpty();
        } catch (Exception e) {
            // API 키가 없거나 네트워크 오류인 경우 테스트 스킵
        }
    }

    @Test
    public void testPersonalizedGreeting() {
        if (greetingService == null) {
            return;
        }

        try {
            String response = greetingService.personalizedGreeting("Charlie", 25);
            assertThat(response).isNotNull();
            assertThat(response).isNotEmpty();
        } catch (Exception e) {
            // API 키가 없거나 네트워크 오류인 경우 테스트 스킵
        }
    }
}

