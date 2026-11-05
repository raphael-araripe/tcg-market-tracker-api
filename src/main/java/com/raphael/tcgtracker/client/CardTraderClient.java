package com.raphael.tcgtracker.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class CardTraderClient {

    private static final Logger log = LoggerFactory.getLogger(CardTraderClient.class);

    private final RestClient restClient;

    public CardTraderClient(RestClient cardTraderRestClient) {
        this.restClient = cardTraderRestClient;
    }

    public List<CardTraderListingDto> fetchMarketplaceListings(Long blueprintId) {
        try {
            Map<String, List<CardTraderListingDto>> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/marketplace/products")
                            .queryParam("blueprint_id", blueprintId)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (response == null) {
                return Collections.emptyList();
            }

            return response.getOrDefault(String.valueOf(blueprintId), Collections.emptyList());
        } catch (Exception ex) {
            log.error("Failed to fetch listings from CardTrader API for blueprintId {}: {}",
                    blueprintId, ex.getMessage());
            return Collections.emptyList();
        }
    }
}