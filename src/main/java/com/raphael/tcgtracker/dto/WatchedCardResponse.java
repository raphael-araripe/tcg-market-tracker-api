package com.raphael.tcgtracker.dto;

import com.raphael.tcgtracker.domain.WatchedCard;
import java.time.OffsetDateTime;

public record WatchedCardResponse(
        Long id,
        Long blueprintId,
        String cardName,
        String expansionName,
        String rarity,
        String targetLanguage,
        String targetCondition,
        boolean active,
        OffsetDateTime createdAt
) {
    // Factory method pattern to cleanly convert Entity to DTO
    public static WatchedCardResponse fromEntity(WatchedCard card) {
        return new WatchedCardResponse(
                card.getId(),
                card.getBlueprintId(),
                card.getCardName(),
                card.getExpansionName(),
                card.getRarity(),
                card.getTargetLanguage(),
                card.getTargetCondition(),
                card.isActive(),
                card.getCreatedAt()
        );
    }
}