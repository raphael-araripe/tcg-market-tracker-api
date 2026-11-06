package com.raphael.tcgtracker.service;

import com.raphael.tcgtracker.domain.PriceSnapshot;
import com.raphael.tcgtracker.domain.WatchedCard;
import com.raphael.tcgtracker.dto.CardAnalyticsResponse;
import com.raphael.tcgtracker.exception.ResourceNotFoundException;
import com.raphael.tcgtracker.repository.PriceSnapshotRepository;
import com.raphael.tcgtracker.repository.WatchedCardRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private WatchedCardRepository watchedCardRepository;

    @Mock
    private PriceSnapshotRepository priceSnapshotRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    @Test
    @DisplayName("Should calculate exact percentage variation with HALF_UP rounding")
    void shouldCalculateExactPercentageVariation() {
        // Arrange
        BigDecimal basePrice = new BigDecimal("100.00");
        BigDecimal droppedPrice = new BigDecimal("92.50");

        // Act
        BigDecimal variation = analyticsService.calculatePercentageVariation(basePrice, droppedPrice);

        // Assert
        assertEquals(new BigDecimal("-7.50"), variation);
    }

    @Test
    @DisplayName("Should classify market trends accurately based on 5% threshold")
    void shouldClassifyMarketTrendsCorrectly() {
        // Arrange & Act & Assert
        assertEquals("PRICE_DROP_ALERT",
                analyticsService.classifyMarketTrend(new BigDecimal("-5.00"), new BigDecimal("0.00")));

        assertEquals("PRICE_SPIKE",
                analyticsService.classifyMarketTrend(new BigDecimal("2.10"), new BigDecimal("6.40")));

        assertEquals("STABLE",
                analyticsService.classifyMarketTrend(new BigDecimal("-2.50"), new BigDecimal("3.20")));
    }

    @Test
    @DisplayName("Should generate full analytics report with mocked database snapshots")
    void shouldGenerateCardAnalyticsReportFromSnapshots() {
        // Arrange
        WatchedCard card = new WatchedCard(302314L, "S:P Little Knight", "Age of Overlord", "Secret Rare");
        PriceSnapshot initial = new PriceSnapshot(card, new BigDecimal("100.00"), null, null, true, 5);
        PriceSnapshot previous = new PriceSnapshot(card, new BigDecimal("95.00"), null, null, true, 4);
        PriceSnapshot latest = new PriceSnapshot(card, new BigDecimal("85.00"), null, null, true, 6);

        when(watchedCardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(priceSnapshotRepository.findTop2ByWatchedCardIdOrderByFetchedAtDesc(any()))
                .thenReturn(List.of(latest, previous));
        when(priceSnapshotRepository.findFirstByWatchedCardIdOrderByFetchedAtAsc(any()))
                .thenReturn(Optional.of(initial));
        when(priceSnapshotRepository.calculateAveragePriceSince(any(), any()))
                .thenReturn(new BigDecimal("90.00"));
        when(priceSnapshotRepository.countByWatchedCardId(any())).thenReturn(3L);

        // Act
        CardAnalyticsResponse response = analyticsService.getAnalyticsByCardId(1L);

        // Assert
        assertEquals("S:P Little Knight", response.cardName());
        assertEquals(new BigDecimal("100.00"), response.initialPrice());
        assertEquals(new BigDecimal("85.00"), response.currentPrice());
        assertEquals(new BigDecimal("-15.00"), response.varFromInitialPct());
        assertEquals("PRICE_DROP_ALERT", response.marketTrend());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when card ID does not exist")
    void shouldThrowExceptionWhenCardNotFound() {
        // Arrange
        when(watchedCardRepository.findById(eq(999L))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> analyticsService.getAnalyticsByCardId(999L));
    }
}