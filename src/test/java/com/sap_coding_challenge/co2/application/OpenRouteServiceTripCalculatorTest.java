package com.sap_coding_challenge.co2.application;

import com.sap_coding_challenge.co2.client.OpenRouteServiceClient;
import com.sap_coding_challenge.co2.domain.Coordinates;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class OpenRouteServiceTripCalculatorTest {

    private static final String HAMBURG = "Hamburg";
    private static final String BERLIN = "Berlin";
    private final OpenRouteServiceClient client = mock(OpenRouteServiceClient.class);
    private final OpenRouteServiceTripCalculator calculator = new OpenRouteServiceTripCalculator(client);

    @Test
    void computeDelegatesToClientAndReturnsKilograms() throws IOException {
        var startCoords = new Coordinates(new BigDecimal("10.0"), new BigDecimal("53.0"));
        var endCoords = new Coordinates(new BigDecimal("13.0"), new BigDecimal("52.0"));
        var distanceBetweenCities = new BigDecimal("150.5");
        when(client.fetchCityCoordinates(anyString())).thenReturn(startCoords).thenReturn(endCoords);
        when(client.fetchDistanceBetweenLocalities(any(), any(), anyString(), anyString())).thenReturn(distanceBetweenCities);

        var totalKg = calculator.compute(HAMBURG, BERLIN, "diesel-car-medium");

        assertThat(totalKg).isEqualByComparingTo("25.7355");
        var inOrder = inOrder(client);
        inOrder.verify(client).fetchCityCoordinates(HAMBURG);
        inOrder.verify(client).fetchCityCoordinates(BERLIN);
        inOrder.verify(client).fetchDistanceBetweenLocalities(startCoords, endCoords, HAMBURG, BERLIN);
        verifyNoMoreInteractions(client);
    }

    @Test
    void computePropagatesClientExceptions() throws IOException {
        var startCoords = new Coordinates(BigDecimal.ONE, BigDecimal.ONE);
        var endCoords = new Coordinates(BigDecimal.TEN, BigDecimal.TEN);
        when(client.fetchCityCoordinates(anyString())).thenReturn(startCoords, endCoords);
        var failure = new IOException("boom");
        when(client.fetchDistanceBetweenLocalities(any(), any(), anyString(), anyString())).thenThrow(failure);

        assertThatThrownBy(() -> calculator.compute("Start", "End", "diesel-car-medium"))
                .isSameAs(failure);
    }

    @Test
    void constructorRejectsNullClient() {
        assertThatNullPointerException()
                .isThrownBy(() -> new OpenRouteServiceTripCalculator(null))
                .withMessage("client");
    }
}
