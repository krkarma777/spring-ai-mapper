package com.krkarma777.springaimapper.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
// Note: In actual testing, OPENAI_API_KEY must be provided as an environment variable
public class GreetingServiceTest {

    @Autowired(required = false)
    private GreetingService greetingService;

    /**
     * Helper method to check if API key is set.
     *
     * @return true if a valid API key is found
     */
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
        assertThat(response).contains("안녕"); // Verify Korean response
    }

    @Test
    public void testObjectMapping() {
        if (!isApiKeySet() || greetingService == null) {
            System.out.println("⚠️ Skipped: OpenAI API Key not found.");
            return;
        }

        System.out.println("🚀 Testing Object Mapping (JSON to POJO)...");
        
        // Execute: Request Tom Cruise information as an object
        ActorInfo actor = greetingService.getActorInfo("Tom Cruise");
        
        System.out.println("Result: " + actor);
        
        // Verify: Check that object fields are properly populated
        assertThat(actor).isNotNull();
        assertThat(actor.name()).contains("Tom");
        assertThat(actor.mostFamousMovie()).isNotEmpty();
        assertThat(actor.age()).isGreaterThan(50); // Tom is older than 50
    }
}

