package com.raphael.tcgtracker.repository;

import com.raphael.tcgtracker.domain.WatchedCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WatchedCardRepository extends JpaRepository<WatchedCard, Long> {

    Optional<WatchedCard> findByBlueprintId(Long blueprintId);

    boolean existsByBlueprintId(Long blueprintId);

    List<WatchedCard> findAllByActiveTrueOrderByCardNameAsc();
}