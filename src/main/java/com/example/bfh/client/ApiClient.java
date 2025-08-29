package com.example.bfh.client;

import com.example.bfh.model.GenerateWebhookRequest;
import com.example.bfh.model.GenerateWebhookResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class ApiClient {
    private static final Logger log = LoggerFactory.getLogger(ApiClient.class);

    private final WebClient webClient;

    public ApiClient(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    public GenerateWebhookResponse generateWebhook(GenerateWebhookRequest req) {
        String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
        log.info("POST {}", url);
        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .retrieve()
                .bodyToMono(GenerateWebhookResponse.class)
                .onErrorResume(ex -> {
                    log.error("Error calling generateWebhook: {}", ex.toString());
                    return Mono.empty();
                })
                .block();
    }

    public void submitFinalQuery(String webhookUrl, String accessToken, String finalQuery, boolean useBearer) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalArgumentException("accessToken is null/blank");
        }
        String token = useBearer ? ("Bearer " + accessToken) : accessToken;

        log.info("POST {} (Authorization: {}...)", webhookUrl, token.substring(0, Math.min(16, token.length())));
        webClient.post()
                .uri(webhookUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .bodyValue(new FinalQuery(finalQuery))
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(body -> log.info("Server response: {}", body))
                .block();
    }

    private record FinalQuery(String finalQuery) {}
}
