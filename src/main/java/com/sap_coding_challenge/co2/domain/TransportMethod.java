package com.sap_coding_challenge.co2.domain;

import java.util.Arrays;

public enum TransportMethod {
    DIESEL_CAR_SMALL("diesel-car-small", 142),
    PETROL_CAR_SMALL("petrol-car-small", 154),
    PLUGIN_HYBRID_CAR_SMALL("plugin-hybrid-car-small", 73),
    ELECTRIC_CAR_SMALL("electric-car-small", 50),

    DIESEL_CAR_MEDIUM("diesel-car-medium", 171),
    PETROL_CAR_MEDIUM("petrol-car-medium", 192),
    PLUGIN_HYBRID_CAR_MEDIUM("plugin-hybrid-car-medium", 110),
    ELECTRIC_CAR_MEDIUM("electric-car-medium", 58),

    DIESEL_CAR_LARGE("diesel-car-large", 209),
    PETROL_CAR_LARGE("petrol-car-large", 282),
    PLUGIN_HYBRID_CAR_LARGE("plugin-hybrid-car-large", 126),
    ELECTRIC_CAR_LARGE("electric-car-large", 73),

    BUS_DEFAULT("bus-default", 27),
    TRAIN_DEFAULT("train-default", 6);

    private final String key;
    private final int gramsPerKm;

    TransportMethod(String key, int gramsPerKm) {
        this.key = key;
        this.gramsPerKm = gramsPerKm;
    }

    public String getKey() {
        return key;
    }

    public int getGramsPerKm() {
        return gramsPerKm;
    }

    public static TransportMethod fromKey(String k) {
        return Arrays.stream(values())
                .filter(m -> m.key.equalsIgnoreCase(k))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown transportation-method: " + k));
    }
}
