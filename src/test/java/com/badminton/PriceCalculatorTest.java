package com.badminton;

import java.math.BigDecimal;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import com.badminton.util.PriceCalculator;

class PriceCalculatorTest {

    @Test
    void ninetyMinutes() {

        BigDecimal result = PriceCalculator.calculate(
                new BigDecimal("100000"),
                LocalTime.of(8, 0),
                LocalTime.of(9, 30)
        );

        assertEquals(
                new BigDecimal("150000.00"),
                result
        );
    }
}