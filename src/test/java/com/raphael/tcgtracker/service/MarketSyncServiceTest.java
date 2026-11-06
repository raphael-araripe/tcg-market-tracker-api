package com.raphael.tcgtracker.service;

import com.raphael.tcgtracker.client.CardTraderClient;
import com.raphael.tcgtracker.client.CardTraderListingDto;
import com.raphael.tcgtracker.client.CardTraderListingDto.ExpansionDto;
import com.raphael.tcgtracker.client.CardTraderListingDto.PriceDto;
import com.raphael.tcgtracker.client.CardTraderListingDto.PropertiesHashDto;
import com.raphael.tcgtracker.repository.PriceSnapshotRepository;
import com.raphael.tcgtracker.repository.WatchedCardRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class MarketSyncServiceTest {

    @Mock
    private WatchedCardRepository watchedCardRepository;

    @Mock
    private PriceSnapshotRepository priceSnapshotRepository;

    @Mock
    private CardTraderClient cardTraderClient;

    @InjectMocks
    private MarketSyncService marketSyncService;

    @Test
    @DisplayName("Should filter only English Near Mint listings and sort by lowest price in cents")
    void shouldFilterAndSortValidListings() {
        // Arrange
        CardTraderListingDto expensiveValid = createListing(1L, 5000, "Near Mint", "en", true);
        CardTraderListingDto cheapestValid = createListing(2L, 3500, "NM", "english", false);
        CardTraderListingDto playedCard = createListing(3L, 1000, "Moderately Played", "en", true);
        CardTraderListingDto italianCard = createListing(4L, 2000, "Near Mint", "it", true);
        CardTraderListingDto poorCondition = createListing(5L, 500, "Poor", "en", false);

        List<CardTraderListingDto> rawListings = List.of(
                expensiveValid, cheapestValid, playedCard, italianCard, poorCondition
        );

        // Act
        List<CardTraderListingDto> result = marketSyncService.filterAndSortListings(rawListings);

        // Assert
        assertEquals(2, result.size(), "Only 2 listings should pass the EN Near Mint filter");
        assertEquals(3500, result.get(0).price().cents(), "Cheapest valid listing should be first");
        assertEquals(5000, result.get(1).price().cents(), "Second cheapest valid listing should be second");
    }

    @Test
    @DisplayName("Should return empty list when all listings are out of stock or invalid condition")
    void shouldReturnEmptyListWhenNoValidListingsMatch() {
        // Arrange
        CardTraderListingDto playedListing = createListing(1L, 1500, "Slightly Played", "en", false);
        CardTraderListingDto frenchListing = createListing(2L, 2000, "Near Mint", "fr", true);

        // Act
        List<CardTraderListingDto> result = marketSyncService.filterAndSortListings(List.of(playedListing, frenchListing));

        // Assert
        assertTrue(result.isEmpty(), "Result should be empty when no EN Near Mint cards exist");
    }

    private CardTraderListingDto createListing(Long id, int cents, String condition, String language, boolean firstEd) {
        return new CardTraderListingDto(
                id,
                new PriceDto(cents, "BRL"),
                new ExpansionDto("Quarter Century Bonanza"),
                new PropertiesHashDto(condition, language, "Secret Rare", firstEd)
        );
    }
}