package com.raphael.tcgtracker.service;

import com.raphael.tcgtracker.domain.PriceSnapshot;
import com.raphael.tcgtracker.domain.WatchedCard;
import com.raphael.tcgtracker.dto.CardAnalyticsResponse;
import com.raphael.tcgtracker.exception.ResourceNotFoundException;
import com.raphael.tcgtracker.repository.PriceSnapshotRepository;
import com.raphael.tcgtracker.repository.WatchedCardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AnalyticsService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal DROP_ALERT_THRESHOLD = new BigDecimal("-5.00");
    private static final BigDecimal SPIKE_ALERT_THRESHOLD = new BigDecimal("5.00");

    private final WatchedCardRepository watchedCardRepository;
    private final PriceSnapshotRepository priceSnapshotRepository;

    public AnalyticsService(WatchedCardRepository watchedCardRepository,
                            PriceSnapshotRepository priceSnapshotRepository) {
        this.watchedCardRepository = watchedCardRepository;
        this.priceSnapshotRepository = priceSnapshotRepository;
    }

    @Transactional(readOnly = true)
    public List<CardAnalyticsResponse> generateAnalyticsForAllActiveCards() {
        List<WatchedCard> activeCards = watchedCardRepository.findAllByActiveTrueOrderByCardNameAsc();
        List<CardAnalyticsResponse> reports = new ArrayList<>();

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime sevenDaysAgo = now.minusDays(7);
        OffsetDateTime thirtyDaysAgo = now.minusDays(30);

        for (WatchedCard card : activeCards) {
            buildCardAnalytics(card, sevenDaysAgo, thirtyDaysAgo)
                    .ifPresent(reports::add);
        }

        return reports;
    }

    @Transactional(readOnly = true)
    public CardAnalyticsResponse getAnalyticsByCardId(Long watchedCardId) {
        WatchedCard card = watchedCardRepository.findById(watchedCardId)
                .orElseThrow(() -> new ResourceNotFoundException("WatchedCard not found with id: " + watchedCardId));

        OffsetDateTime now = OffsetDateTime.now();
        return buildCardAnalytics(card, now.minusDays(7), now.minusDays(30))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No price snapshots recorded yet for card: " + card.getCardName()));
    }

    private Optional<CardAnalyticsResponse> buildCardAnalytics(
            WatchedCard card, OffsetDateTime sevenDaysAgo, OffsetDateTime thirtyDaysAgo) {

        List<PriceSnapshot> latestTwo = priceSnapshotRepository
                .findTop2ByWatchedCardIdOrderByFetchedAtDesc(card.getId());

        if (latestTwo.isEmpty()) {
            return Optional.empty();
        }

        PriceSnapshot latestSnapshot = latestTwo.get(0);
        PriceSnapshot previousSnapshot = latestTwo.size() > 1 ? latestTwo.get(1) : latestSnapshot;
        PriceSnapshot initialSnapshot = priceSnapshotRepository
                .findFirstByWatchedCardIdOrderByFetchedAtAsc(card.getId())
                .orElse(latestSnapshot);

        BigDecimal currentPrice = latestSnapshot.getCheapestPrice();
        BigDecimal previousPrice = previousSnapshot.getCheapestPrice();
        BigDecimal initialPrice = initialSnapshot.getCheapestPrice();

        BigDecimal avg7d = Optional.ofNullable(
                priceSnapshotRepository.calculateAveragePriceSince(card.getId(), sevenDaysAgo))
                .orElse(currentPrice);

        BigDecimal avg30d = Optional.ofNullable(
                priceSnapshotRepository.calculateAveragePriceSince(card.getId(), thirtyDaysAgo))
                .orElse(currentPrice);

        BigDecimal varInitial = calculatePercentageVariation(initialPrice, currentPrice);
        BigDecimal varPrevious = calculatePercentageVariation(previousPrice, currentPrice);
        BigDecimal var7d = calculatePercentageVariation(avg7d, currentPrice);
        BigDecimal var30d = calculatePercentageVariation(avg30d, currentPrice);

        String trend = classifyMarketTrend(var7d, varInitial);
        long totalSnapshots = priceSnapshotRepository.countByWatchedCardId(card.getId());

        return Optional.of(new CardAnalyticsResponse(
                card.getId(),
                card.getBlueprintId(),
                card.getCardName(),
                card.getRarity(),
                card.getExpansionName(),
                latestSnapshot.isFirstEdition() ? "1st NM" : "NM",
                initialPrice,
                previousPrice,
                currentPrice,
                avg7d,
                avg30d,
                varInitial,
                varPrevious,
                var7d,
                var30d,
                trend,
                latestSnapshot.getValidListingsCount(),
                totalSnapshots,
                latestSnapshot.getFetchedAt()
        ));
    }

    // Package-private for unit testing with JUnit 5 in Step 4!
    BigDecimal calculatePercentageVariation(BigDecimal basePrice, BigDecimal currentPrice) {
        if (basePrice == null || currentPrice == null || basePrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return currentPrice.subtract(basePrice)
                .multiply(HUNDRED)
                .divide(basePrice, 2, RoundingMode.HALF_UP);
    }

    String classifyMarketTrend(BigDecimal var7dPct, BigDecimal varInitialPct) {
        if (var7dPct.compareTo(DROP_ALERT_THRESHOLD) <= 0 || varInitialPct.compareTo(DROP_ALERT_THRESHOLD) <= 0) {
            return "PRICE_DROP_ALERT";
        }
        if (var7dPct.compareTo(SPIKE_ALERT_THRESHOLD) >= 0 || varInitialPct.compareTo(SPIKE_ALERT_THRESHOLD) >= 0) {
            return "PRICE_SPIKE";
        }
        return "STABLE";
    }
}