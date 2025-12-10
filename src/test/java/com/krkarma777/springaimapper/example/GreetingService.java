package com.krkarma777.springaimapper.example;

import com.krkarma777.springaimapper.annotation.LlmClient;
import com.krkarma777.springaimapper.annotation.Param;
import com.krkarma777.springaimapper.annotation.SystemMessage;
import com.krkarma777.springaimapper.annotation.UserMessage;

@LlmClient(model = "gpt-4o-mini") // 가성비 좋은 모델로 변경
@SystemMessage("You are a helpful database assistant. You strictly answer in JSON format when requested.")
public interface GreetingService {

    // 1. 기본 String 반환 테스트
    @UserMessage("Say hello to {name} strictly in Korean.")
    String greet(@Param("name") String name);

    // 2. [핵심] 객체(Record) 자동 매핑 테스트
    // 라이브러리가 자동으로 "JSON 포맷 지시문"을 프롬프트 뒤에 붙여줄 것임.
    @UserMessage("Extract information about the movie actor '{actorName}'.")
    ActorInfo getActorInfo(@Param("actorName") String actorName);
}

// 테스트용 DTO (Record)
record ActorInfo(String name, String mostFamousMovie, int age) {}

