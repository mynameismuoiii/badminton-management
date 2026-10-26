package com.badminton.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalTime;

public final class PriceCalculator {

    private PriceCalculator() {
    }

    public static BigDecimal calculate(
            BigDecimal hourlyPrice,
            LocalTime from,
            LocalTime to
    ) {

        if (hourlyPrice == null
                || hourlyPrice.signum() < 0
                || from == null
                || to == null
                || !from.isBefore(to)) {

            throw new IllegalArgumentException(
                    "Invalid price or time"
            );
        }

        long minutes =
                Duration.between(from, to).toMinutes();

        return hourlyPrice
                .multiply(BigDecimal.valueOf(minutes))
                .divide(
                        BigDecimal.valueOf(60),
                        2,
                        RoundingMode.HALF_UP
                );
    }
}