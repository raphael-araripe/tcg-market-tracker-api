package com.raphael.tcgtracker.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CardAnalyticsResponse(
        Long watchedCardId,
        Long blueprintId,
        String cardName,
        String rarity,
        String expansionName,
        String conditionDisplay,
        BigDecimal initialPrice,
        BigDecimal previousPrice,
        BigDecimal currentPrice,
        BigDecimal avgPrice7d,
        BigDecimal avgPrice30d,
        BigDecimal varFromInitialPct,
        BigDecimal varFromPreviousPct,
        BigDecimal varFrom7dAvgPct,
        BigDecimal varFrom30dAvgPct,
        String marketTrend,
        int validListingsCount,
        long totalSnapshots,
        OffsetDateTime lastUpdated
) {
}