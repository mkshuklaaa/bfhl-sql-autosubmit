package com.example.bfhl;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class HttpConfig {
  @Bean
  public RestTemplate restTemplate() {
    // buffering lets us log response bodies if needed
    return new RestTemplate(
        new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
  }
}
