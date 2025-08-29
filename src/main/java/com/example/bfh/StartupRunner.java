package com.example.bfh;

import com.example.bfh.client.ApiClient;
import com.example.bfh.model.GenerateWebhookRequest;
import com.example.bfh.model.GenerateWebhookResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(StartupRunner.class);

    private final ApiClient apiClient;
    private final String finalSql;

    public StartupRunner(ApiClient apiClient) {
        this.apiClient = apiClient;
        // Final SQL (PostgreSQL) as per the prompt
        this.finalSql = "SELECT \n"
                + "  p.amount AS salary,\n"
                + "  e.first_name || ' ' || e.last_name AS name,\n"
                + "  EXTRACT(YEAR FROM age(current_date, e.dob))::int AS age,\n"
                + "  d.department_name\n"
                + "FROM payments p\n"
                + "JOIN employee e ON e.emp_id = p.emp_id\n"
                + "JOIN department d ON d.department_id = e.department\n"
                + "WHERE EXTRACT(DAY FROM p.payment_time) <> 1\n"
                + "ORDER BY p.amount DESC\n"
                + "LIMIT 1;";
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            String name = System.getenv().getOrDefault("BFH_NAME", "Manish Kumar Shukla");
            String regNo = System.getenv().getOrDefault("BFH_REGNO", "22BCY10065");
            String email = System.getenv().getOrDefault("BFH_EMAIL", "manishkumarshukla2022@vitbhopal.ac.in");
            boolean useBearer = Boolean.parseBoolean(System.getenv().getOrDefault("BFH_USE_BEARER", "false"));

            log.info("Generating webhook...");
            GenerateWebhookResponse resp = apiClient.generateWebhook(new GenerateWebhookRequest(name, regNo, email));

            if (resp == null) {
                throw new IllegalStateException("generateWebhook returned null");
            }
            log.info("Webhook: {}", resp.getWebhook());
            log.info("AccessToken: {}...", resp.getAccessToken() == null ? null : resp.getAccessToken().substring(0, Math.min(12, resp.getAccessToken().length())));

            String webhookToUse = (resp.getWebhook() != null && !resp.getWebhook().isBlank())
                    ? resp.getWebhook()
                    : "https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA";

            apiClient.submitFinalQuery(webhookToUse, resp.getAccessToken(), finalSql, useBearer);
            log.info("Final query submitted successfully.");

        } catch (Exception e) {
            // Ensure app doesn't crash silently
            e.printStackTrace();
        }
    }
}
