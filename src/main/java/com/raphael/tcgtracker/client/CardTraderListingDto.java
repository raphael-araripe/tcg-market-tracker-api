package com.raphael.tcgtracker.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CardTraderListingDto(
        Long id,
        PriceDto price,
        ExpansionDto expansion,
        @JsonProperty("properties_hash") PropertiesHashDto propertiesHash
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PriceDto(
            Integer cents,
            String currency
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ExpansionDto(
            @JsonProperty("name_en") String nameEn
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PropertiesHashDto(
            String condition,
            @JsonProperty("yugioh_language") String yugiohLanguage,
            @JsonProperty("yugioh_rarity") String yugiohRarity,
            @JsonProperty("first_edition") Boolean firstEdition
    ) {}
}