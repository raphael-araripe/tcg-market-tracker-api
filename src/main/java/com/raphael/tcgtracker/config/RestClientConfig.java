package com.raphael.tcgtracker.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestClient;

@Configuration
@EnableScheduling
public class RestClientConfig {

    private static final Logger log = LoggerFactory.getLogger(RestClientConfig.class);

    @Value("${app.cardtrader.base-url}")
    private String baseUrl;

    @Value("${app.cardtrader.token}")
    private String apiToken;

    @Bean
    public RestClient cardTraderRestClient() {
        if (apiToken == null || apiToken.isBlank()) {
            log.warn("⚠️ CARDTRADER_TOKEN is NOT set! Requests to CardTrader API will fail with 401 Unauthorized.");
        } else {
            log.info("✅ CardTrader RestClient initialized successfully (Token loaded: {} chars).", apiToken.trim().length());
        }

        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + (apiToken != null ? apiToken.trim() : ""))
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}