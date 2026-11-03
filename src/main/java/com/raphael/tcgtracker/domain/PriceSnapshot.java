package com.raphael.tcgtracker.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "price_snapshots")
public class PriceSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "watched_card_id", nullable = false)
    private WatchedCard watchedCard;

    @Column(name = "cheapest_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal cheapestPrice;

    @Column(name = "second_price", precision = 10, scale = 2)
    private BigDecimal secondPrice;

    @Column(name = "third_price", precision = 10, scale = 2)
    private BigDecimal thirdPrice;

    @Column(name = "is_first_edition", nullable = false)
    private boolean firstEdition;

    @Column(name = "valid_listings_count", nullable = false)
    private Integer validListingsCount;

    @Column(name = "fetched_at", nullable = false, updatable = false)
    private OffsetDateTime fetchedAt;

    protected PriceSnapshot() {
        // Required by JPA
    }

    public PriceSnapshot(WatchedCard watchedCard, BigDecimal cheapestPrice, BigDecimal secondPrice,
                         BigDecimal thirdPrice, boolean firstEdition, Integer validListingsCount) {
        this.watchedCard = watchedCard;
        this.cheapestPrice = cheapestPrice;
        this.secondPrice = secondPrice;
        this.thirdPrice = thirdPrice;
        this.firstEdition = firstEdition;
        this.validListingsCount = validListingsCount;
    }

    @PrePersist
    protected void onCreate() {
        if (this.fetchedAt == null) {
            this.fetchedAt = OffsetDateTime.now();
        }
    }

    // Getters
    public Long getId() { return id; }
    public WatchedCard getWatchedCard() { return watchedCard; }
    public BigDecimal getCheapestPrice() { return cheapestPrice; }
    public BigDecimal getSecondPrice() { return secondPrice; }
    public BigDecimal getThirdPrice() { return thirdPrice; }
    public boolean isFirstEdition() { return firstEdition; }
    public Integer getValidListingsCount() { return validListingsCount; }
    public OffsetDateTime getFetchedAt() { return fetchedAt; }
}