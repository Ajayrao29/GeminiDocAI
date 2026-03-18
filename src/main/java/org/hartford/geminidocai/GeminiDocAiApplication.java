package org.hartford.GeminiDocAI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class GeminiDocAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(GeminiDocAiApplication.class, args);
    }
}
