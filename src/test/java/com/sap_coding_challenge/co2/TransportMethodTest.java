package com.sap_coding_challenge.co2;

import com.sap_coding_challenge.co2.domain.TransportMethod;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TransportMethodTest {

    @Test
    void fromKey_valid() {
        TransportMethod m = TransportMethod.fromKey("diesel-car-medium");
        assertEquals(171, m.getGramsPerKm());
    }

    @Test
    void fromKey_invalid() {
        assertThrows(IllegalArgumentException.class, () ->
                TransportMethod.fromKey("rocket-scooter"));
    }
}
