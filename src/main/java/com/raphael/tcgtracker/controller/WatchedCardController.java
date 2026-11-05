package com.raphael.tcgtracker.controller;

import com.raphael.tcgtracker.dto.WatchedCardRequest;
import com.raphael.tcgtracker.dto.WatchedCardResponse;
import com.raphael.tcgtracker.service.WatchedCardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cards")
public class WatchedCardController {

    private final WatchedCardService watchedCardService;

    public WatchedCardController(WatchedCardService watchedCardService) {
        this.watchedCardService = watchedCardService;
    }

    @GetMapping
    public ResponseEntity<List<WatchedCardResponse>> listAll(
            @RequestParam(defaultValue = "false") boolean onlyActive) {
        return ResponseEntity.ok(watchedCardService.listAllCards(onlyActive));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WatchedCardResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(watchedCardService.getCardById(id));
    }

    @PostMapping
    public ResponseEntity<WatchedCardResponse> register(@Valid @RequestBody WatchedCardRequest request) {
        WatchedCardResponse created = watchedCardService.registerCard(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<WatchedCardResponse> toggleActive(
            @PathVariable Long id,
            @RequestParam boolean active) {
        return ResponseEntity.ok(watchedCardService.toggleActiveStatus(id, active));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        watchedCardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }
}