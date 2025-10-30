package com.sap_coding_challenge.co2.domain;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Immutable longitude/latitude pair.
 */
public record Coordinates(BigDecimal longitude, BigDecimal latitude) {
    public Coordinates {
        Objects.requireNonNull(longitude, "longitude");
        Objects.requireNonNull(latitude, "latitude");
    }
}
