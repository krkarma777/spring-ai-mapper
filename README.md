# Spring AI Mapper

[![Java](https://img.shields.io/badge/Java-21-orange)](https://www.oracle.com/java/)

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.8-green)](https://spring.io/projects/spring-boot)

[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.1.2-blue)](https://spring.io/projects/spring-ai)

[![License](https://img.shields.io/badge/License-Apache%202.0-grey)](LICENSE)

> **Stop writing boilerplate code for LLMs.**

> Turn your Raw Text Prompts into Java Objects with a single Interface.

**Spring AI Mapper** is a declarative HTTP client for LLMs, inspired by **Spring Cloud OpenFeign**. It eliminates the complexity of `PromptTemplate`, `ChatClient`, and `BeanOutputConverter`, allowing you to interact with AI models using simple Java Interfaces.

---

## 🔥 Why use this?

### ❌ Before (Standard Spring AI)

You have to manually manage prompts, variables, and parsing logic.

```java
// Too much boilerplate code...
BeanOutputConverter<ActorInfo> converter = new BeanOutputConverter<>(ActorInfo.class);
String format = converter.getFormat();

String promptText = "Tell me about " + actorName + ". " + format;
ChatResponse response = chatClient.prompt().user(promptText).call().content();

ActorInfo actor = converter.convert(response); // Error handling? Retry?
```

### ✅ After (Spring AI Mapper)

Just declare an interface. The library handles the rest.

```java
@LlmClient(model = "gpt-4o")
public interface MovieClient {

    @UserMessage("Tell me about {actorName}.")
    ActorInfo getActorInfo(@Param("actorName") String name);
}
```

-----

## 🚀 Quick Start

### 1. Installation (JitPack)

Add the repository and dependency to your `build.gradle.kts`:

```kotlin
repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.krkarma777:spring-ai-mapper:master-SNAPSHOT")
}
```

### 2. Configuration

Set your API key in `application.yml` (or `application.properties`).

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4o-mini # Default model
```

### 3. Usage

Define your interface and use it immediately.

#### Step 1: Define Interface

```java
@LlmClient
@SystemMessage("You are a helpful movie database assistant.")
public interface MovieService {

    // Return simple String
    @UserMessage("Recommend 3 movies directed by {director}.")
    String recommendMovies(@Param("director") String director);

    // Return Java Object (Structured Output)
    @UserMessage("Extract profile of {actor}.")
    ActorProfile getProfile(@Param("actor") String actorName);
}

// DTO (Record recommended)
public record ActorProfile(String name, int age, List<String> famousWorks) {}
```

#### Step 2: Inject and Use

```java
@Service
public class MyService {

    private final MovieService movieService;

    public MyService(MovieService movieService) {
        this.movieService = movieService;
    }

    public void printTomCruiseInfo() {
        // Returns a structured Java Object automatically!
        ActorProfile profile = movieService.getProfile("Tom Cruise");
        
        System.out.println(profile.name()); // "Tom Cruise"
        System.out.println(profile.age());  // 61
    }
}
```

-----

## 🛠 Features

  * **Declarative Interface:** No implementation required. Just define signatures.

  * **Auto Structured Output:** Automatically appends JSON schema instructions and converts LLM responses to POJOs/Records.

  * **Parameter Binding:** Supports `@Param` to map method arguments to prompt variables `{variable}`.

  * **System Prompts:** Define reusable system instructions with `@SystemMessage`.

  * **Spring AI Native:** Built on top of Spring AI 1.1.2, supporting all major LLMs (OpenAI, Anthropic, Gemini, etc.).

-----

## 📦 Requirements

  * Java 21+

  * Spring Boot 3.4.x

  * Spring AI 1.1.2

-----

## 🤝 Contributing

Pull requests are welcome! For major changes, please open an issue first to discuss what you would like to change.

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.
