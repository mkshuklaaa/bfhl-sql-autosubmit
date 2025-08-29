package com.example.bfhl;

import com.example.bfhl.model.FinalReq;
import com.example.bfhl.model.GenerateReq;
import com.example.bfhl.model.GenerateResp;
import com.example.bfhl.service.SqlSolver;
import com.example.bfhl.service.WebhookClient;
import com.example.bfhl.util.RegNoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class App {
  private static final Logger log = LoggerFactory.getLogger(App.class);

  public static void main(String[] args) {
    SpringApplication.run(App.class, args);
  }

  @Bean
  ApplicationRunner onStart(
      WebhookClient client,
      SqlSolver solver,
      @Value("${app.name}") String name,
      @Value("${app.regNo}") String regNo,
      @Value("${app.email}") String email
  ) {
    return args -> {
      log.info("🚀 Starting BFHL SQL AutoSubmit…");

      // 1) generate webhook + access token
      GenerateReq req = new GenerateReq(name, regNo, email);
      GenerateResp resp = client.generate(req);
      if (resp == null || resp.getWebhook() == null || resp.getAccessToken() == null) {
        log.error("❌ Failed to obtain webhook or token. Aborting.");
        return;
      }
      log.info("✅ Got webhook: {}", resp.getWebhook());

      // 2) decide which SQL to use based on last two digits
      int lastTwo = RegNoUtil.lastTwoDigits(regNo);
      boolean isOdd = (lastTwo % 2) == 1;
      String finalSql = isOdd ? solver.sqlForQuestion1() : solver.sqlForQuestion2();
      log.info("🧠 Selected {} (lastTwo={} ⇒ {}):\n{}",
              isOdd ? "Question 1" : "Question 2", lastTwo, isOdd ? "odd" : "even", finalSql);

      // 3) submit to the returned webhook with Authorization header = accessToken (JWT)
      FinalReq finalReq = new FinalReq(finalSql);
      client.submit(resp.getWebhook(), resp.getAccessToken(), finalReq);
      log.info("🎉 Submission attempted — check server response above.");
    };
  }
}
