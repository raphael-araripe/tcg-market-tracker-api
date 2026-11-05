package com.raphael.tcgtracker.controller;

import com.raphael.tcgtracker.dto.CardAnalyticsResponse;
import com.raphael.tcgtracker.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/summary")
    public ResponseEntity<List<CardAnalyticsResponse>> getMarketAnalyticsSummary() {
        return ResponseEntity.ok(analyticsService.generateAnalyticsForAllActiveCards());
    }

    @GetMapping("/cards/{watchedCardId}")
    public ResponseEntity<CardAnalyticsResponse> getCardAnalytics(@PathVariable Long watchedCardId) {
        return ResponseEntity.ok(analyticsService.getAnalyticsByCardId(watchedCardId));
    }
}