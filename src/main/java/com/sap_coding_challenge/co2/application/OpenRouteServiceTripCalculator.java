package com.sap_coding_challenge.co2.application;

import com.sap_coding_challenge.co2.client.OpenRouteServiceClient;
import com.sap_coding_challenge.co2.domain.TransportMethod;

import java.io.IOException;
import java.math.BigDecimal;

import static java.util.Objects.requireNonNull;

public final class OpenRouteServiceTripCalculator {
    private final OpenRouteServiceClient client;

    public OpenRouteServiceTripCalculator(OpenRouteServiceClient client) {
        this.client = requireNonNull(client, "client");
    }

    public BigDecimal compute(String startCity, String endCity, String transportMethodKey) throws IOException {
        var startCoords = client.fetchCityCoordinates(startCity);
        var endCoords = client.fetchCityCoordinates(endCity);
        var distanceKm = client.fetchDistanceBetweenLocalities(startCoords, endCoords, startCity, endCity);
        var gramsPerKm = BigDecimal.valueOf(TransportMethod.fromKey(transportMethodKey).getGramsPerKm());
        return gramsPerKm.multiply(distanceKm).movePointLeft(3);
    }
}
