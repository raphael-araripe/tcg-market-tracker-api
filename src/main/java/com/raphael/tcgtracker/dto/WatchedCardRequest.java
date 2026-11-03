package com.raphael.tcgtracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record WatchedCardRequest(
        @NotNull(message = "Blueprint ID is required")
        @Positive(message = "Blueprint ID must be a positive number")
        Long blueprintId,

        @NotBlank(message = "Card name is required")
        @Size(max = 150, message = "Card name must not exceed 150 characters")
        String cardName,

        @Size(max = 150, message = "Expansion name must not exceed 150 characters")
        String expansionName,

        @Size(max = 80, message = "Rarity must not exceed 80 characters")
        String rarity
) {
}