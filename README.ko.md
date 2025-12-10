# Spring AI Mapper

[![Java](https://img.shields.io/badge/Java-21-orange)](https://www.oracle.com/java/)

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.8-green)](https://spring.io/projects/spring-boot)

[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.1.2-blue)](https://spring.io/projects/spring-ai)

[![License](https://img.shields.io/badge/License-Apache%202.0-grey)](LICENSE)

> **LLM을 위한 보일러플레이트 코드 작성을 그만하세요.**

> 단일 인터페이스로 Raw Text 프롬프트를 Java 객체로 변환하세요.

**Spring AI Mapper**는 **Spring Cloud OpenFeign**에서 영감을 받은 선언적 LLM HTTP 클라이언트입니다. `PromptTemplate`, `ChatClient`, `BeanOutputConverter`의 복잡성을 제거하고, 간단한 Java 인터페이스만으로 AI 모델과 상호작용할 수 있게 해줍니다.

---

## 🔥 왜 사용해야 할까요?

### ❌ 이전 (표준 Spring AI)

프롬프트, 변수, 파싱 로직을 수동으로 관리해야 합니다.

```java
// 너무 많은 보일러플레이트 코드...
BeanOutputConverter<ActorInfo> converter = new BeanOutputConverter<>(ActorInfo.class);
String format = converter.getFormat();

String promptText = "Tell me about " + actorName + ". " + format;
ChatResponse response = chatClient.prompt().user(promptText).call().content();

ActorInfo actor = converter.convert(response); // 에러 처리? 재시도?
```

### ✅ 이후 (Spring AI Mapper)

인터페이스만 선언하세요. 나머지는 라이브러리가 처리합니다.

```java
@LlmClient(model = "gpt-4o")
public interface MovieClient {

    @UserMessage("Tell me about {actorName}.")
    ActorInfo getActorInfo(@Param("actorName") String name);
}
```

-----

## 🚀 빠른 시작

### 1. 설치 (JitPack)

`build.gradle.kts`에 저장소와 의존성을 추가하세요:

```kotlin
repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.krkarma777:spring-ai-mapper:master-SNAPSHOT")
}
```

### 2. 설정

`application.yml` (또는 `application.properties`)에 API 키를 설정하세요.

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4o-mini # 기본 모델
```

### 3. 사용법

인터페이스를 정의하고 즉시 사용하세요.

#### Step 1: 인터페이스 정의

```java
@LlmClient
@SystemMessage("You are a helpful movie database assistant.")
public interface MovieService {

    // 단순 String 반환
    @UserMessage("Recommend 3 movies directed by {director}.")
    String recommendMovies(@Param("director") String director);

    // Java 객체 반환 (구조화된 출력)
    @UserMessage("Extract profile of {actor}.")
    ActorProfile getProfile(@Param("actor") String actorName);
}

// DTO (Record 권장)
public record ActorProfile(String name, int age, List<String> famousWorks) {}
```

#### Step 2: 주입 및 사용

```java
@Service
public class MyService {

    private final MovieService movieService;

    public MyService(MovieService movieService) {
        this.movieService = movieService;
    }

    public void printTomCruiseInfo() {
        // 자동으로 구조화된 Java 객체를 반환합니다!
        ActorProfile profile = movieService.getProfile("Tom Cruise");
        
        System.out.println(profile.name()); // "Tom Cruise"
        System.out.println(profile.age());  // 61
    }
}
```

-----

## 🛠 기능

  * **선언적 인터페이스:** 구현이 필요 없습니다. 시그니처만 정의하세요.

  * **자동 구조화된 출력:** JSON 스키마 지시사항을 자동으로 추가하고 LLM 응답을 POJO/Record로 변환합니다.

  * **파라미터 바인딩:** `@Param`을 지원하여 메서드 인자를 프롬프트 변수 `{variable}`에 매핑합니다.

  * **시스템 프롬프트:** `@SystemMessage`로 재사용 가능한 시스템 지시사항을 정의합니다.

  * **Spring AI 네이티브:** Spring AI 1.1.2 기반으로 구축되어 모든 주요 LLM(OpenAI, Anthropic, Gemini 등)을 지원합니다.

-----

## 📦 요구사항

  * Java 21+

  * Spring Boot 3.4.x

  * Spring AI 1.1.2

-----

## 🤝 기여하기

Pull Request를 환영합니다! 주요 변경사항의 경우, 먼저 이슈를 열어 변경하고 싶은 내용에 대해 논의해 주세요.

## 📄 라이선스

이 프로젝트는 Apache License 2.0 하에 라이선스됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

