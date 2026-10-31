-- Table 1: Stores the Yu-Gi-Oh! cards monitored by the system
CREATE TABLE watched_cards (
    id BIGSERIAL PRIMARY KEY,
    blueprint_id BIGINT NOT NULL UNIQUE,
    card_name VARCHAR(150) NOT NULL,
    expansion_name VARCHAR(150),
    rarity VARCHAR(80),
    target_language VARCHAR(10) NOT NULL DEFAULT 'en',
    target_condition VARCHAR(30) NOT NULL DEFAULT 'Near Mint',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Table 2: Stores historical price snapshots fetched from CardTrader API
CREATE TABLE price_snapshots (
    id BIGSERIAL PRIMARY KEY,
    watched_card_id BIGINT NOT NULL,
    cheapest_price NUMERIC(10, 2) NOT NULL,
    second_price NUMERIC(10, 2),
    third_price NUMERIC(10, 2),
    is_first_edition BOOLEAN NOT NULL DEFAULT FALSE,
    valid_listings_count INTEGER NOT NULL DEFAULT 0,
    fetched_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_watched_card
        FOREIGN KEY (watched_card_id)
        REFERENCES watched_cards (id)
        ON DELETE CASCADE
);

-- Performance Indexes for fast historical analytics queries
CREATE INDEX idx_watched_cards_blueprint_id ON watched_cards(blueprint_id);
CREATE INDEX idx_price_snapshots_card_fetched ON price_snapshots(watched_card_id, fetched_at DESC);