package com.example.bfh;

import com.example.bfh.service.WebhookService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BfhApplication {

    public static void main(String[] args) {
        SpringApplication.run(BfhApplication.class, args);
    }

    // Run on startup automatically
    @Bean
    public ApplicationRunner runner(WebhookService webhookService) {
        return args -> {
            webhookService.performFlow();
        };
    }
}
