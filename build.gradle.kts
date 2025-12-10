plugins {
    `java-library`
    id("org.springframework.boot") version "3.4.1"
    // [중요] Boot 3.4.x와 호환되도록 플러그인 버전 업그레이드 (1.1.0 -> 1.1.7)
    id("io.spring.dependency-management") version "1.1.7"
    `maven-publish`
}

group = "com.krkarma777"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
    maven {
        // 혹시 Milestone 버전을 쓰게 될 경우를 대비해 유지 (GA 버전인 1.1.1은 Central에도 있음)
        url = uri("https://repo.spring.io/milestone")
    }
    maven {
        // 1.1.2-SNAPSHOT 등 개발 버전을 꼭 써야 한다면 이 저장소가 필요함
        url = uri("https://repo.spring.io/snapshot")
    }
}

dependencyManagement {
    imports {
        // [핵심 수정] 1.1.2는 아직 정식 배포 전일 수 있음 -> 최신 안정 버전 1.1.1로 변경
        mavenBom("org.springframework.ai:spring-ai-bom:1.1.1")
    }
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-autoconfigure")

    // Spring AI
    implementation("org.springframework.ai:spring-ai-starter-model-openai")

    // Jackson
    implementation("com.fasterxml.jackson.core:jackson-databind")

    // Lombok (테스트 코드에서도 동작하도록 설정 추가)
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")      // 추가됨
    testAnnotationProcessor("org.projectlombok:lombok") // 추가됨

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.ai:spring-ai-starter-model-openai")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// 라이브러리 프로젝트 설정: 실행 가능한 BootJar 끄기
tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}

// 일반 Jar 켜기
tasks.named<Jar>("jar") {
    enabled = true
    archiveClassifier.set("")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name.set("Spring AI Mapper")
                description.set("Declarative HTTP client for LLMs using Spring AI")
                url.set("https://github.com/krkarma777/spring-ai-mapper")
            }
        }
    }
}