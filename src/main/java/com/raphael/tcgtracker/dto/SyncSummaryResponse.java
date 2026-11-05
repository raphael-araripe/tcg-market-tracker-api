package com.raphael.tcgtracker.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record SyncSummaryResponse(
        int totalCardsProcessed,
        int snapshotsSaved,
        int outOfStockOrFilteredOut,
        OffsetDateTime syncedAt,
        List<SnapshotItemDto> results
) {
    public record SnapshotItemDto(
            Long blueprintId,
            String cardName,
            String rarity,
            String expansionName,
            String conditionDisplay,
            String cheapestPrice,
            String secondPrice,
            String thirdPrice,
            int validListingsCount
    ) {}
}