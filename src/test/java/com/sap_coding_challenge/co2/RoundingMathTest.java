package com.sap_coding_challenge.co2;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

public class RoundingMathTest {

    @Test
    void co2Math_roundsToOneDecimal() {
        double km = 123.456; // arbitrary
        int gpkm = 171; // diesel-car-medium
        double kg = (gpkm * km) / 1000.0;
        BigDecimal out = new BigDecimal(kg).setScale(1, RoundingMode.HALF_UP);
        assertEquals( "21.1", out.toString().substring(0, 4)); // sanity check format
    }
}
