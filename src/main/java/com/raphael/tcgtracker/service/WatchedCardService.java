package com.raphael.tcgtracker.service;

import com.raphael.tcgtracker.domain.WatchedCard;
import com.raphael.tcgtracker.dto.WatchedCardRequest;
import com.raphael.tcgtracker.dto.WatchedCardResponse;
import com.raphael.tcgtracker.exception.DuplicateResourceException;
import com.raphael.tcgtracker.exception.ResourceNotFoundException;
import com.raphael.tcgtracker.repository.WatchedCardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WatchedCardService {

    private final WatchedCardRepository watchedCardRepository;

    public WatchedCardService(WatchedCardRepository watchedCardRepository) {
        this.watchedCardRepository = watchedCardRepository;
    }

    @Transactional(readOnly = true)
    public List<WatchedCardResponse> listAllCards(boolean onlyActive) {
        List<WatchedCard> cards = onlyActive
                ? watchedCardRepository.findAllByActiveTrueOrderByCardNameAsc()
                : watchedCardRepository.findAll();

        return cards.stream()
                .map(WatchedCardResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public WatchedCardResponse getCardById(Long id) {
        WatchedCard card = findEntityById(id);
        return WatchedCardResponse.fromEntity(card);
    }

    @Transactional
    public WatchedCardResponse registerCard(WatchedCardRequest request) {
        if (watchedCardRepository.existsByBlueprintId(request.blueprintId())) {
            throw new DuplicateResourceException(
                    "Card with blueprintId " + request.blueprintId() + " is already in the watchlist.");
        }

        WatchedCard newCard = new WatchedCard(
                request.blueprintId(),
                request.cardName().trim(),
                request.expansionName(),
                request.rarity()
        );

        WatchedCard savedCard = watchedCardRepository.save(newCard);
        return WatchedCardResponse.fromEntity(savedCard);
    }

    @Transactional
    public WatchedCardResponse toggleActiveStatus(Long id, boolean active) {
        WatchedCard card = findEntityById(id);
        card.setActive(active);
        return WatchedCardResponse.fromEntity(watchedCardRepository.save(card));
    }

    @Transactional
    public void deleteCard(Long id) {
        WatchedCard card = findEntityById(id);
        watchedCardRepository.delete(card);
    }

    private WatchedCard findEntityById(Long id) {
        return watchedCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WatchedCard not found with id: " + id));
    }
}