package com.sap_coding_challenge.co2.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap_coding_challenge.co2.domain.Coordinates;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;

import static java.util.Objects.requireNonNull;

public class OpenRouteServiceClient {
    private static final HttpUrl DEFAULT_BASE_URL = requireNonNull(
            HttpUrl.parse("https://api.openrouteservice.org"),
            "Invalid ORS base URL");
    private static final MediaType JSON_MEDIA_TYPE = requireNonNull(
            MediaType.parse("application/json"),
            "Invalid JSON media type");

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final HttpUrl baseUrl;
    private final String apiKey;

    public OpenRouteServiceClient(String apiKey) {
        this(apiKey, new OkHttpClient(), new ObjectMapper(), DEFAULT_BASE_URL);
    }

    OpenRouteServiceClient(String apiKey, OkHttpClient httpClient, ObjectMapper objectMapper, HttpUrl baseUrl) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Missing ORS token (env ORS_TOKEN).");
        }
        this.apiKey = apiKey;
        this.httpClient = requireNonNull(httpClient, "http");
        this.objectMapper = requireNonNull(objectMapper, "objectMapper");
        this.baseUrl = requireNonNull(baseUrl, "baseUrl");
    }

    public Coordinates fetchCityCoordinates(String city) throws IOException {
        var request = buildFetchCityCoordinatesRequest(city);
        try (var geoCodeSearchResponse = httpClient.newCall(request).execute()) {
            checkResponseStatusCode(geoCodeSearchResponse,
                    "Could not fetch coordinates for city \"" + city + "\": HTTP ");
            var responseBody = geoCodeSearchResponse.body();
            checkResponseBody(responseBody,
                    "Could not fetch coordinates for city \"" + city + "\": empty response body.");
            var coords = extractCityCoordinates(responseBody.byteStream(), city);
            return new Coordinates(coords.get(0).decimalValue(), coords.get(1).decimalValue());
        }
    }

    public BigDecimal fetchDistanceBetweenLocalities(Coordinates startLonLat, Coordinates endLonLat,
                                                     String startCity, String endCity) throws IOException {
        var request = buildFetchDistanceBetweenLocalitiesRequest(startLonLat, endLonLat);
        try (var response = httpClient.newCall(request).execute()) {
            checkResponseStatusCode(response,
                    "Could not fetch distance between \"" + startCity + "\" and \"" + endCity + "\": HTTP ");
            var responseBody = response.body();
            checkResponseBody(responseBody,
                    "Could not fetch distance between \"" + startCity + "\" and \"" + endCity + "\": empty response body.");
            return extractDistanceInKm(responseBody, startCity, endCity);
        }
    }

    private JsonNode extractCityCoordinates(InputStream inputStream, String city) throws IOException {
        var coords = objectMapper.readTree(inputStream).at("/features/0/geometry/coordinates");
        if (coords.isMissingNode() || !coords.isArray()) {
            throw new IllegalArgumentException("Could not fetch coordinates for city \"" + city + "\".");
        }
        return coords;
    }

    private BigDecimal extractDistanceInKm(ResponseBody responseBody, String startCity, String endCity)
            throws IOException {
        var distanceNode = objectMapper.readTree(responseBody.byteStream()).at("/distances/0/1");
        if (distanceNode.isMissingNode() || !distanceNode.isNumber()) {
            throw new IllegalArgumentException(
                    "Could not fetch distance between \"" + startCity + "\" and \"" + endCity
                            + "\": unexpected response.");
        }
        // meters to kilometers
        return distanceNode.decimalValue().movePointLeft(3);
    }

    private Request buildFetchCityCoordinatesRequest(String city) {
        var geoCodeSearchUrl = baseUrl.newBuilder()
                .addPathSegments("geocode/search")
                .addQueryParameter("api_key", apiKey)
                .addQueryParameter("text", city)
                .addQueryParameter("layers", "locality")
                .build();
        return new Request.Builder()
                .url(geoCodeSearchUrl)
                .get()
                .build();
    }

    private Request buildFetchDistanceBetweenLocalitiesRequest(Coordinates startLonLat, Coordinates endLonLat) {
        var url = baseUrl.newBuilder()
                .addPathSegments("v2/matrix/driving-car")
                .build();
        var payload = """
                {"locations":[[%s,%s],[%s,%s]],"metrics":["distance"]}
                """.formatted(
                startLonLat.longitude().toPlainString(),
                startLonLat.latitude().toPlainString(),
                endLonLat.longitude().toPlainString(),
                endLonLat.latitude().toPlainString());
        return new Request.Builder()
                .url(url)
                .addHeader("Authorization", apiKey)
                .post(RequestBody.create(payload, JSON_MEDIA_TYPE))
                .build();
    }

    private static void checkResponseStatusCode(Response response, String errorMessage) throws IOException {
        if (!response.isSuccessful()) {
            throw new IOException(errorMessage + response.code());
        }
    }

    private static void checkResponseBody(ResponseBody body, String errorMessage) throws IOException {
        if (body == null) throw new IOException(errorMessage);
    }
}
