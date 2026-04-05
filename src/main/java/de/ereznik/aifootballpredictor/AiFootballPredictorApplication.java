package de.ereznik.aifootballpredictor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AiFootballPredictorApplication {

    static void main(String[] args) {
        SpringApplication.run(AiFootballPredictorApplication.class, args);
    }

}
