package com.krkarma777.springaimapper.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
// 주의: 실제 테스트 시엔 환경변수로 OPENAI_API_KEY를 주입해야 함
public class GreetingServiceTest {

    @Autowired(required = false)
    private GreetingService greetingService;

    // API 키가 있는지 확인하는 헬퍼 메서드
    private boolean isApiKeySet() {
        String key = System.getenv("OPENAI_API_KEY");
        return key != null && !key.isEmpty() && !key.equals("test-key");
    }

    @Test
    public void testStringResponse() {
        if (!isApiKeySet() || greetingService == null) {
            System.out.println("⚠️ Skipped: OpenAI API Key not found.");
            return;
        }

        System.out.println("🚀 Testing String Response...");
        String response = greetingService.greet("Iron Man");
        
        System.out.println("Result: " + response);
        assertThat(response).contains("안녕"); // 한국어 응답 확인
    }

    @Test
    public void testObjectMapping() {
        if (!isApiKeySet() || greetingService == null) {
            System.out.println("⚠️ Skipped: OpenAI API Key not found.");
            return;
        }

        System.out.println("🚀 Testing Object Mapping (JSON to POJO)...");
        
        // 실행: 톰 크루즈 정보를 객체로 달라고 요청
        ActorInfo actor = greetingService.getActorInfo("Tom Cruise");
        
        System.out.println("Result: " + actor);
        
        // 검증: 객체 필드가 제대로 채워졌는지 확인
        assertThat(actor).isNotNull();
        assertThat(actor.name()).contains("Tom");
        assertThat(actor.mostFamousMovie()).isNotEmpty();
        assertThat(actor.age()).isGreaterThan(50); // 톰형 나이 많음
    }
}

