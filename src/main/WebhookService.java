package com.example.bfh.service;

import com.example.bfh.dto.GenerateWebhookRequest;
import com.example.bfh.dto.GenerateWebhookResponse;
import com.example.bfh.dto.FinalQueryRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class WebhookService {
    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);

    // Endpoints from the task
    private static final String GENERATE_URL = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
    private static final String TEST_WEBHOOK_URL_PLACEHOLDER = "https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // final SQL query (exact string)
    private final String finalSql = "SELECT p.AMOUNT AS SALARY, CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS NAME, "
            + "TIMESTAMPDIFF(YEAR, e.DOB, CURDATE()) AS AGE, d.DEPARTMENT_NAME "
            + "FROM PAYMENTS p "
            + "JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID "
            + "JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID "
            + "WHERE DAY(p.PAYMENT_TIME) <> 1 "
            + "AND p.AMOUNT = (SELECT MAX(AMOUNT) FROM PAYMENTS WHERE DAY(PAYMENT_TIME) <> 1);";

    public WebhookService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public void performFlow() {
        try {
            log.info("Starting webhook generation flow...");

            // 1) Build request body
            GenerateWebhookRequest req = new GenerateWebhookRequest("John Doe", "REG12347", "john@example.com");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<GenerateWebhookRequest> entity = new HttpEntity<>(req, headers);

            log.info("Posting to generateWebhook endpoint: {}", GENERATE_URL);
            ResponseEntity<GenerateWebhookResponse> response = restTemplate.postForEntity(
                    GENERATE_URL, entity, GenerateWebhookResponse.class);

            if (response.getStatusCode() != HttpStatus.OK && response.getStatusCode() != HttpStatus.CREATED) {
                log.error("generateWebhook returned status: {}", response.getStatusCode());
                log.error("Response body: {}", response.getBody());
                return;
            }

            GenerateWebhookResponse respBody = response.getBody();
            if (respBody == null || respBody.getWebhook() == null || respBody.getAccessToken() == null) {
                log.error("Missing webhook or accessToken in response. Full response: {}", respBody);
                return;
            }

            String webhookUrl = respBody.getWebhook();
            String accessToken = respBody.getAccessToken();

            log.info("Received webhookUrl: {}", webhookUrl);
            log.info("Received accessToken: {}", accessToken.substring(0, Math.min(12, accessToken.length())) + "...");

            // 2) Prepare final query request body
            FinalQueryRequest finalReq = new FinalQueryRequest(finalSql);

            HttpHeaders postHeaders = new HttpHeaders();
            postHeaders.setContentType(MediaType.APPLICATION_JSON);

            // Use accessToken as JWT in Authorization header. The instructions say just provide accessToken,
            // so we will set the header value directly. If API expects "Bearer <token>" you can change to that.
            postHeaders.set("Authorization", accessToken);

            HttpEntity<FinalQueryRequest> postEntity = new HttpEntity<>(finalReq, postHeaders);

            log.info("Posting final query to webhook URL: {}", webhookUrl);
            ResponseEntity<String> postResponse = restTemplate.postForEntity(webhookUrl, postEntity, String.class);

            log.info("Final webhook post returned status: {}", postResponse.getStatusCode());
            log.info("Final webhook response body: {}", postResponse.getBody());

        } catch (Exception e) {
            log.error("Exception during webhook flow", e);
        }
    }
}
