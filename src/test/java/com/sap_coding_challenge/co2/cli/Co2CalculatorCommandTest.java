package com.sap_coding_challenge.co2.cli;

import com.sap_coding_challenge.co2.application.OpenRouteServiceTripCalculator;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class Co2CalculatorCommandTest {

    private static final String HAMBURG = "Hamburg";
    private static final String BERLIN = "Berlin";
    private static final String TRANSPORTATION_METHOD = "bus";
    private final OpenRouteServiceTripCalculator tripCalculator = mock(OpenRouteServiceTripCalculator.class);

    @Test
    void callInvokesTripCalculatorAndReturnsSuccessExitCode() throws Exception {
        var command = new Co2CalculatorCommand(tripCalculator);
        command.start = HAMBURG;
        command.end = BERLIN;
        command.transportationMethod = TRANSPORTATION_METHOD;

        var totalCo2Kg = new BigDecimal("12.34");
        when(tripCalculator.compute(HAMBURG, BERLIN, TRANSPORTATION_METHOD)).thenReturn(totalCo2Kg);

        int exitCode = command.call();

        assertThat(exitCode).isZero();
        verify(tripCalculator).compute(HAMBURG, BERLIN, TRANSPORTATION_METHOD);
        verifyNoMoreInteractions(tripCalculator);
    }

    @Test
    void constructorRequiresTripCalculator() {
        assertThatThrownBy(() -> new Co2CalculatorCommand(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("tripCalculator is required");
    }

    @Test
    void callPropagatesExceptionsFromTripCalculator() throws IOException {
        var command = new Co2CalculatorCommand(tripCalculator);
        command.start = HAMBURG;
        command.end = BERLIN;
        command.transportationMethod = TRANSPORTATION_METHOD;

        var failure = new IllegalStateException("failure");
        when(tripCalculator.compute(HAMBURG, BERLIN, TRANSPORTATION_METHOD)).thenThrow(failure);

        assertThatThrownBy(command::call)
                .isInstanceOf(IllegalStateException.class)
                .isSameAs(failure);
        verify(tripCalculator).compute(HAMBURG, BERLIN, TRANSPORTATION_METHOD);
        verifyNoMoreInteractions(tripCalculator);
    }
}
