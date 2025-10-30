package com.sap_coding_challenge.co2.cli;

import com.sap_coding_challenge.co2.application.OpenRouteServiceTripCalculator;
import com.sap_coding_challenge.co2.client.OpenRouteServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.math.RoundingMode;
import java.util.Objects;
import java.util.concurrent.Callable;

@Command(name = "co2-calculator",
        mixinStandardHelpOptions = true,
        version = "co2-calculator 1.0.0",
        description = "Compute CO2e for a city-to-city trip using OpenRouteService.")
public final class Co2CalculatorCommand implements Callable<Integer> {

    private static final Logger log = LoggerFactory.getLogger(Co2CalculatorCommand.class);

    @Option(names = "--start", required = true, paramLabel = "<city>", description = "Start city (e.g., Hamburg)")
    String start;

    @Option(names = "--end", required = true, paramLabel = "<city>", description = "End city (e.g., Berlin)")
    String end;

    @Option(names = "--transportation-method", required = true, paramLabel = "<key>", description = "Transport method key (see TransportMethod enum)")
    String transportationMethod;

    private final OpenRouteServiceTripCalculator tripCalculator;

    Co2CalculatorCommand(OpenRouteServiceTripCalculator tripCalculator) {
        this.tripCalculator = Objects.requireNonNull(tripCalculator, "tripCalculator is required");
    }

    @Override
    public Integer call() throws Exception {
        var totalCo2Kg = tripCalculator.compute(start, end, transportationMethod);
        var rounded = totalCo2Kg.setScale(1, RoundingMode.HALF_UP);
        log.info("Computed CO2 footprint: {}kg ({} -> {}, method: {})", rounded, start, end, transportationMethod);
        log.info("Your trip caused {}kg of CO2-equivalent.", rounded);
        return 0;
    }

    public static void main(String[] args) {
        var factory = new CliFactory();
        int exit = new CommandLine(Co2CalculatorCommand.class, factory).execute(args);
        System.exit(exit);
    }

    private static final class CliFactory implements CommandLine.IFactory {
        private final CommandLine.IFactory delegate = CommandLine.defaultFactory();

        @Override
        public <K> K create(Class<K> cls) throws Exception {
            if (cls == Co2CalculatorCommand.class) {
                var token = System.getenv("ORS_TOKEN");
                var client = new OpenRouteServiceClient(token);
                var calculator = new OpenRouteServiceTripCalculator(client);
                @SuppressWarnings("unchecked")
                K command = (K) new Co2CalculatorCommand(calculator);
                return command;
            }
            return delegate.create(cls);
        }
    }
}
