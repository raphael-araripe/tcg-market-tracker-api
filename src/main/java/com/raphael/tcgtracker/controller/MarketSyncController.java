package com.raphael.tcgtracker.controller;

import com.raphael.tcgtracker.dto.SyncSummaryResponse;
import com.raphael.tcgtracker.service.MarketSyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sync")
public class MarketSyncController {

    private final MarketSyncService marketSyncService;

    public MarketSyncController(MarketSyncService marketSyncService) {
        this.marketSyncService = marketSyncService;
    }

    @PostMapping
    public ResponseEntity<SyncSummaryResponse> triggerManualSync() {
        SyncSummaryResponse summary = marketSyncService.syncAllActiveCards();
        return ResponseEntity.ok(summary);
    }
}