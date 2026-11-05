package com.raphael.tcgtracker.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "watched_cards")
public class WatchedCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "blueprint_id", nullable = false, unique = true)
    private Long blueprintId;

    @Column(name = "card_name", nullable = false, length = 150)
    private String cardName;

    @Column(name = "expansion_name", length = 150)
    private String expansionName;

    @Column(name = "rarity", length = 80)
    private String rarity;

    @Column(name = "target_language", nullable = false, length = 10)
    private String targetLanguage = "en";

    @Column(name = "target_condition", nullable = false, length = 30)
    private String targetCondition = "Near Mint";

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected WatchedCard() {
        // Required no-args constructor by JPA specification
    }

    public WatchedCard(Long blueprintId, String cardName, String expansionName, String rarity) {
        this.blueprintId = blueprintId;
        this.cardName = cardName;
        this.expansionName = expansionName;
        this.rarity = rarity;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = OffsetDateTime.now();
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public Long getBlueprintId() { return blueprintId; }
    public void setBlueprintId(Long blueprintId) { this.blueprintId = blueprintId; }
    public String getCardName() { return cardName; }
    public void setCardName(String cardName) { this.cardName = cardName; }
    public String getExpansionName() { return expansionName; }
    public void setExpansionName(String expansionName) { this.expansionName = expansionName; }
    public String getRarity() { return rarity; }
    public void setRarity(String rarity) { this.rarity = rarity; }
    public String getTargetLanguage() { return targetLanguage; }
    public void setTargetLanguage(String targetLanguage) { this.targetLanguage = targetLanguage; }
    public String getTargetCondition() { return targetCondition; }
    public void setTargetCondition(String targetCondition) { this.targetCondition = targetCondition; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}