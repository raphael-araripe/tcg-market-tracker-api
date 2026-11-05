package com.raphael.tcgtracker.repository;

import com.raphael.tcgtracker.domain.PriceSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PriceSnapshotRepository extends JpaRepository<PriceSnapshot, Long> {

    List<PriceSnapshot> findByWatchedCardIdOrderByFetchedAtDesc(Long watchedCardId);

    Optional<PriceSnapshot> findFirstByWatchedCardIdOrderByFetchedAtDesc(Long watchedCardId);

    Optional<PriceSnapshot> findFirstByWatchedCardIdOrderByFetchedAtAsc(Long watchedCardId);
}