package com.sap_coding_challenge.co2.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;

public final class OpenRouteServiceClient {
    private final OkHttpClient http = new OkHttpClient();
    private final ObjectMapper om = new ObjectMapper();
    private final String apiKey;

    public OpenRouteServiceClient(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Missing ORS token (env ORS_TOKEN).");
        }
        this.apiKey = apiKey;
    }

    public double[] geocodeCity(String city) throws IOException {
        HttpUrl url = HttpUrl.parse("https://api.openrouteservice.org/geocode/search")
                .newBuilder()
                .addQueryParameter("api_key", apiKey)
                .addQueryParameter("text", city)
                .addQueryParameter("layers", "locality")
                .build();

        Request req = new Request.Builder().url(url).get().build();
        try (Response res = http.newCall(req).execute()) {
            if (!res.isSuccessful()) {
                throw new IOException("Geocode failed: " + res.code());
            }
            ResponseBody body = res.body();
            if (body == null) throw new IOException("Geocode empty body");
            JsonNode root = om.readTree(body.byteStream());
            JsonNode features = root.path("features");
            if (!features.isArray() || features.size() == 0) {
                throw new IllegalArgumentException("City not found: " + city);
            }
            JsonNode coords = features.get(0).path("geometry").path("coordinates");
            return new double[]{coords.get(0).asDouble(), coords.get(1).asDouble()};
        }
    }

    public double distanceKm(double[] startLonLat, double[] endLonLat) throws IOException {
        ObjectMapper mapper = om;
        String payload = String.format("{'locations':[[%f,%f],[%f,%f]],'metrics':['distance']}",
                startLonLat[0], startLonLat[1], endLonLat[0], endLonLat[1]);

        Request req = new Request.Builder()
                .url("https://api.openrouteservice.org/v2/matrix/driving-car")
                .addHeader("Authorization", apiKey)
                .post(RequestBody.create(payload, MediaType.parse("application/json")))
                .build();

        try (Response res = http.newCall(req).execute()) {
            if (!res.isSuccessful()) {
                throw new IOException("Matrix failed: " + res.code());
            }
            ResponseBody body = res.body();
            if (body == null) throw new IOException("Matrix empty body");
            JsonNode root = mapper.readTree(body.byteStream());
            double meters = root.path("distances").get(0).get(1).asDouble();
            return meters / 1000.0;
        }
    }
}
