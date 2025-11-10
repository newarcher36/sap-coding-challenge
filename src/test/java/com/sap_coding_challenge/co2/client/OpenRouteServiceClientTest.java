package com.sap_coding_challenge.co2.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.stream.Stream;

import com.sap_coding_challenge.co2.domain.Coordinates;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class OpenRouteServiceClientTest {
    private static final String API_KEY = "test-key";

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .failOnUnmatchedRequests(true)
            .options(wireMockConfig().dynamicPort())
            .build();

    private final ObjectMapper mapper = new ObjectMapper();
    private final OkHttpClient httpClient = new OkHttpClient();

    @Test
    void fetchCityCoordinatesReturnsLonLat() throws IOException {
        wireMock.stubFor(get(urlPathEqualTo("/geocode/search"))
                .withQueryParam("api_key", matching(".+"))
                .withQueryParam("text", matching(".+"))
                .withQueryParam("layers", matching(".+"))
                .willReturn(okJson("""
                        {
                          "features": [
                            {
                              "geometry": {
                                "coordinates": [10.000654, 53.550341]
                              }
                            }
                          ]
                        }
                        """)));

        var client = newClient();

        var coordinates = client.fetchCityCoordinates("Hamburg");
        assertThat(coordinates.longitude()).isEqualByComparingTo("10.000654");
        assertThat(coordinates.latitude()).isEqualByComparingTo("53.550341");

        wireMock.verify(getRequestedFor(urlPathEqualTo("/geocode/search"))
                .withQueryParam("api_key", equalTo(API_KEY))
                .withQueryParam("text", equalTo("Hamburg"))
                .withQueryParam("layers", equalTo("locality")));
    }

    @Test
    void fetchCityCoordinatesPropagatesHttpErrors() {
        wireMock.stubFor(get(urlPathEqualTo("/geocode/search"))
                .withQueryParam("api_key", equalTo(API_KEY))
                .withQueryParam("text", equalTo("Hamburg"))
                .withQueryParam("layers", equalTo("locality"))
                .willReturn(aResponse().withStatus(404)));

        var client = newClient();

        assertThatThrownBy(() -> client.fetchCityCoordinates("Hamburg"))
                .isInstanceOf(IOException.class)
                .hasMessage("Could not fetch coordinates for city \"Hamburg\": HTTP 404");
    }

    @ParameterizedTest
    @MethodSource("invalidGeocodeBodies")
    void fetchCityCoordinatesThrowsWhenCoordinatesMissing(String responseBody) {
        wireMock.stubFor(get(urlPathEqualTo("/geocode/search"))
                .withQueryParam("api_key", equalTo(API_KEY))
                .withQueryParam("text", equalTo("Atlantis"))
                .withQueryParam("layers", equalTo("locality"))
                .willReturn(okJson(responseBody)));

        var client = newClient();

        assertThatThrownBy(() -> client.fetchCityCoordinates("Atlantis"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Could not fetch coordinates for city \"Atlantis\".");
    }

    @Test
    void fetchDistanceBetweenLocalitiesReturnsKilometers() throws IOException {
        wireMock.stubFor(post(urlPathEqualTo("/v2/matrix/driving-car"))
                .withHeader("Authorization", equalTo(API_KEY))
                .willReturn(okJson("""
                        {
                          "distances": [
                            [0.0, 289876.4],
                            [289876.4, 0.0]
                          ]
                        }
                        """)));

        var client = newClient();
        var kilometers = client.fetchDistanceBetweenLocalities(
                new Coordinates(new BigDecimal("10.000654"), new BigDecimal("53.550341")),
                new Coordinates(new BigDecimal("13.404954"), new BigDecimal("52.520008")),
                "Hamburg", "Berlin");

        assertThat(kilometers).isEqualByComparingTo("289.8764");
    }

    @Test
    void fetchDistanceBetweenLocalitiesPropagatesHttpErrors() {
        wireMock.stubFor(post(urlPathEqualTo("/v2/matrix/driving-car"))
                .withHeader("Authorization", equalTo(API_KEY))
                .willReturn(aResponse().withStatus(500)));

        var client = newClient();

        assertThatThrownBy(() -> client.fetchDistanceBetweenLocalities(
                new Coordinates(BigDecimal.TEN, new BigDecimal("53")),
                new Coordinates(new BigDecimal("13"), new BigDecimal("52")),
                "Hamburg", "Berlin"))
                .isInstanceOf(IOException.class)
                .hasMessage("Could not fetch distance between \"Hamburg\" and \"Berlin\": HTTP 500");
    }

    @ParameterizedTest
    @MethodSource("invalidDistanceBodies")
    void fetchDistanceBetweenLocalitiesThrowsWhenMatrixInvalid(String responseBody) {
        wireMock.stubFor(post(urlPathEqualTo("/v2/matrix/driving-car"))
                .withHeader("Authorization", equalTo(API_KEY))
                .willReturn(okJson(responseBody)));

        var client = newClient();

        assertThatThrownBy(() -> client.fetchDistanceBetweenLocalities(
                new Coordinates(BigDecimal.TEN, new BigDecimal("53")),
                new Coordinates(new BigDecimal("13"), new BigDecimal("52")),
                "Hamburg", "Berlin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Could not fetch distance between \"Hamburg\" and \"Berlin\": unexpected response.");
    }

    private OpenRouteServiceClient newClient() {
        var baseUrl = HttpUrl.parse(wireMock.baseUrl());
        return new OpenRouteServiceClient(API_KEY, httpClient, mapper, baseUrl);
    }

    private static Stream<String> invalidGeocodeBodies() {
        return Stream.of(
                """
                        {
                          "features": []
                        }
                        """,
                """
                        {
                          "features": [
                            {
                              "geometry": {
                                "coordinates": null
                              }
                            }
                          ]
                        }
                        """,
                """
                        {
                          "features": [
                            {
                              "geometry": {
                                "coordinates": "not-an-array"
                              }
                            }
                          ]
                        }
                        """
        );
    }

    private static Stream<String> invalidDistanceBodies() {
        return Stream.of(
                """
                        {
                          "distances": null
                        }
                        """,
                """
                        {
                          "distances": [
                            [0.0]
                          ]
                        }
                        """,
                """
                        {
                          "distances": [
                            [0.0, "not-a-number"]
                          ]
                        }
                        """
        );
    }
}
