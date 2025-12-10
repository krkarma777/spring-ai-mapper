package com.krkarma777.springaimapper.example;

import com.krkarma777.springaimapper.annotation.LlmClient;
import com.krkarma777.springaimapper.annotation.Param;
import com.krkarma777.springaimapper.annotation.SystemMessage;
import com.krkarma777.springaimapper.annotation.UserMessage;

@LlmClient(model = "gpt-4o-mini") // Cost-effective model
@SystemMessage("You are a helpful database assistant. You strictly answer in JSON format when requested.")
public interface GreetingService {

    /**
     * Basic String return test.
     */
    @UserMessage("Say hello to {name} strictly in Korean.")
    String greet(@Param("name") String name);

    /**
     * Object (Record) auto-mapping test.
     * The library automatically appends JSON format instructions to the prompt.
     */
    @UserMessage("Extract information about the movie actor '{actorName}'.")
    ActorInfo getActorInfo(@Param("actorName") String actorName);
}

/**
 * Test DTO (Record).
 */
record ActorInfo(String name, String mostFamousMovie, int age) {}

