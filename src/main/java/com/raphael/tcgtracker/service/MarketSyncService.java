package com.raphael.tcgtracker.service;

import com.raphael.tcgtracker.client.CardTraderClient;
import com.raphael.tcgtracker.client.CardTraderListingDto;
import com.raphael.tcgtracker.domain.PriceSnapshot;
import com.raphael.tcgtracker.domain.WatchedCard;
import com.raphael.tcgtracker.dto.SyncSummaryResponse;
import com.raphael.tcgtracker.dto.SyncSummaryResponse.SnapshotItemDto;
import com.raphael.tcgtracker.repository.PriceSnapshotRepository;
import com.raphael.tcgtracker.repository.WatchedCardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
public class MarketSyncService {

    private static final Logger log = LoggerFactory.getLogger(MarketSyncService.class);

    private static final Set<String> ACCEPTED_LANGUAGES = Set.of("en", "english", "");
    private static final Set<String> ACCEPTED_CONDITIONS = Set.of("near mint", "nm", "");
    private static final Set<String> REJECTED_CONDITIONS = Set.of(
            "played", "pl", "poor", "po", "moderately played", "mp", "slightly played", "sp"
    );

    private final WatchedCardRepository watchedCardRepository;
    private final PriceSnapshotRepository priceSnapshotRepository;
    private final CardTraderClient cardTraderClient;

    public MarketSyncService(WatchedCardRepository watchedCardRepository,
                             PriceSnapshotRepository priceSnapshotRepository,
                             CardTraderClient cardTraderClient) {
        this.watchedCardRepository = watchedCardRepository;
        this.priceSnapshotRepository = priceSnapshotRepository;
        this.cardTraderClient = cardTraderClient;
    }

    // Runs automatically every 6 hours (can also be triggered manually via REST endpoint)
    @Scheduled(cron = "${app.sync.cron:0 0 */6 * * *}")
    @Transactional
    public SyncSummaryResponse syncAllActiveCards() {
        List<WatchedCard> activeCards = watchedCardRepository.findAllByActiveTrueOrderByCardNameAsc();
        log.info("Starting CardTrader market synchronization for {} active cards...", activeCards.size());

        List<SnapshotItemDto> results = new ArrayList<>();
        int snapshotsSaved = 0;
        int skipped = 0;

        for (WatchedCard card : activeCards) {
            List<CardTraderListingDto> rawListings = cardTraderClient.fetchMarketplaceListings(card.getBlueprintId());

            List<CardTraderListingDto> validListings = filterAndSortListings(rawListings);

            if (validListings.isEmpty()) {
                log.warn("No valid EN Near Mint listings found for: {} ({})", card.getCardName(), card.getBlueprintId());
                skipped++;
                continue;
            }

            CardTraderListingDto cheapestItem = validListings.get(0);
            enrichCardMetadataIfNeeded(card, cheapestItem);

            BigDecimal p1 = centsToBigDecimal(cheapestItem.price().cents());
            BigDecimal p2 = validListings.size() > 1 ? centsToBigDecimal(validListings.get(1).price().cents()) : null;
            BigDecimal p3 = validListings.size() > 2 ? centsToBigDecimal(validListings.get(2).price().cents()) : null;

            boolean isFirstEd = cheapestItem.propertiesHash() != null
                    && Boolean.TRUE.equals(cheapestItem.propertiesHash().firstEdition());

            PriceSnapshot snapshot = new PriceSnapshot(card, p1, p2, p3, isFirstEd, validListings.size());
            priceSnapshotRepository.save(snapshot);
            snapshotsSaved++;

            results.add(new SnapshotItemDto(
                    card.getBlueprintId(),
                    card.getCardName(),
                    card.getRarity(),
                    card.getExpansionName(),
                    isFirstEd ? "1st NM" : "NM",
                    p1.toPlainString(),
                    p2 != null ? p2.toPlainString() : "N/A",
                    p3 != null ? p3.toPlainString() : "N/A",
                    validListings.size()
            ));
        }

        log.info("Market sync completed: {} saved, {} skipped.", snapshotsSaved, skipped);
        return new SyncSummaryResponse(activeCards.size(), snapshotsSaved, skipped, OffsetDateTime.now(), results);
    }

    // Package-private for easy unit testing with JUnit 5 in Step 4!
    List<CardTraderListingDto> filterAndSortListings(List<CardTraderListingDto> rawListings) {
        if (rawListings == null || rawListings.isEmpty()) {
            return List.of();
        }
        return rawListings.stream()
                .filter(this::isValidNearMintEnglishListing)
                .sorted(Comparator.comparingInt(item -> item.price().cents()))
                .toList();
    }

    private boolean isValidNearMintEnglishListing(CardTraderListingDto item) {
        if (item.price() == null || item.price().cents() == null || item.propertiesHash() == null) {
            return false;
        }
        String lang = normalize(item.propertiesHash().yugiohLanguage());
        String cond = normalize(item.propertiesHash().condition());

        boolean isEnglish = ACCEPTED_LANGUAGES.contains(lang);
        boolean isNearMint = ACCEPTED_CONDITIONS.contains(cond) && !REJECTED_CONDITIONS.contains(cond);

        return isEnglish && isNearMint;
    }

    private void enrichCardMetadataIfNeeded(WatchedCard card, CardTraderListingDto item) {
        boolean updated = false;
        if ((card.getRarity() == null || card.getRarity().isBlank())
                && item.propertiesHash() != null && item.propertiesHash().yugiohRarity() != null) {
            card.setRarity(item.propertiesHash().yugiohRarity());
            updated = true;
        }
        if ((card.getExpansionName() == null || card.getExpansionName().isBlank())
                && item.expansion() != null && item.expansion().nameEn() != null) {
            card.setExpansionName(item.expansion().nameEn());
            updated = true;
        }
        if (updated) {
            watchedCardRepository.save(card);
        }
    }

    private BigDecimal centsToBigDecimal(Integer cents) {
        return BigDecimal.valueOf(cents)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}