package com.example.bfhl.service;

import com.example.bfhl.model.FinalReq;
import com.example.bfhl.model.GenerateReq;
import com.example.bfhl.model.GenerateResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@Service
public class WebhookClient {
  private static final Logger log = LoggerFactory.getLogger(WebhookClient.class);

  private final RestTemplate rest;
  private final String generateUrl;

  public WebhookClient(RestTemplate rest, @Value("${bfhl.generate-url}") String generateUrl) {
    this.rest = rest;
    this.generateUrl = generateUrl;
  }

  public GenerateResp generate(GenerateReq body) {
    try {
      HttpHeaders h = new HttpHeaders();
      h.setContentType(MediaType.APPLICATION_JSON);
      ResponseEntity<GenerateResp> resp = rest.postForEntity(
          generateUrl, new HttpEntity<>(body, h), GenerateResp.class);
      log.debug("generate() status={} body={}", resp.getStatusCode(), resp.getBody());
      return resp.getBody();
    } catch (RestClientResponseException ex) {
      log.error("generate() failed: status={} body={}", ex.getRawStatusCode(), ex.getResponseBodyAsString());
      return null;
    } catch (Exception ex) {
      log.error("generate() error", ex);
      return null;
    }
  }

  public void submit(String webhookUrl, String jwtToken, FinalReq finalReq) {
    try {
      HttpHeaders h = new HttpHeaders();
      h.setContentType(MediaType.APPLICATION_JSON);
      // IMPORTANT: per instructions, use the JWT *as-is* in Authorization header (no 'Bearer ')
      h.set("Authorization", jwtToken);
      ResponseEntity<String> resp = rest.postForEntity(webhookUrl, new HttpEntity<>(finalReq, h), String.class);
      log.info("submit() status={} body={}", resp.getStatusCode(), resp.getBody());
    } catch (RestClientResponseException ex) {
      log.error("submit() failed: status={} body={}", ex.getRawStatusCode(), ex.getResponseBodyAsString());
    } catch (Exception ex) {
      log.error("submit() error", ex);
    }
  }
}
