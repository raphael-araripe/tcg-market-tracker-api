package com.raphael.tcgtracker.repository;

import com.raphael.tcgtracker.domain.PriceSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface PriceSnapshotRepository extends JpaRepository<PriceSnapshot, Long> {

    List<PriceSnapshot> findByWatchedCardIdOrderByFetchedAtDesc(Long watchedCardId);

    // Fetches TODAY (index 0) and PREV (index 1) using the composite index
    List<PriceSnapshot> findTop2ByWatchedCardIdOrderByFetchedAtDesc(Long watchedCardId);

    // Fetches START (the very first recorded price)
    Optional<PriceSnapshot> findFirstByWatchedCardIdOrderByFetchedAtAsc(Long watchedCardId);

    long countByWatchedCardId(Long watchedCardId);

    // Calculates moving average directly in PostgreSQL for a given time window (e.g., 7d or 30d)
    @Query(value = """
            SELECT ROUND(AVG(cheapest_price), 2)
            FROM price_snapshots
            WHERE watched_card_id = :cardId
              AND fetched_at >= :since
            """, nativeQuery = true)
    BigDecimal calculateAveragePriceSince(@Param("cardId") Long cardId, @Param("since") OffsetDateTime since);
}