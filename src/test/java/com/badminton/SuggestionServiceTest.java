package com.badminton;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;

import com.badminton.util.TimeSlotUtil;

class SuggestionServiceTest {

    @Test
    void adjacentBookingsDoNotOverlap() {

        boolean result = TimeSlotUtil.overlaps(
                LocalTime.of(8, 0),
                LocalTime.of(9, 0),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0)
        );

        assertFalse(result);
    }
}