package com.sap_coding_challenge.co2.cli;

import com.sap_coding_challenge.co2.domain.TransportMethod;
import com.sap_coding_challenge.co2.service.OpenRouteServiceClient;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.Callable;

@Command(name = "co2-calculator", mixinStandardHelpOptions = true,
        description = "Compute CO2e for a city-to-city trip using OpenRouteService.")
public class Co2CalculatorCommand implements Callable<Integer> {

    @Option(names = "--start", required = true, paramLabel = "<city>",
            description = "Start city (e.g., Hamburg)")
    String start;

    @Option(names = "--end", required = true, paramLabel = "<city>",
            description = "End city (e.g., Berlin)")
    String end;

    @Option(names = "--transportation-method", required = true, paramLabel = "<key>",
            description = "Transport method key (see TransportMethod enum)")
    String transportationMethod;

    @Override
    public Integer call() throws Exception {
        var token = System.getenv("ORS_TOKEN");
        var openRouteServiceClient = new OpenRouteServiceClient(token);

        double[] startCityCoordinates = openRouteServiceClient.geocodeCity(start);
        double[] endCityCoordinates = openRouteServiceClient.geocodeCity(end);
        var distanceKm = openRouteServiceClient.distanceKm(startCityCoordinates, endCityCoordinates);

        var transportMethod = TransportMethod.fromKey(transportationMethod);
        double kg = (transportMethod.getGramsPerKm() * distanceKm) / 1000.0;

        var out = new BigDecimal(kg).setScale(1, RoundingMode.HALF_UP);
        System.out.println("Your trip caused " + out + "kg of CO2-equivalent.");
        return 0;
    }
}
