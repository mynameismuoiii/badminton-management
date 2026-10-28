
package com.badminton;

import com.badminton.util.TimeSlotUtil;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertFalse;

class SuggestionServiceTest {

    @Test
    void adjacentBookingsDoNotOverlap() {

        boolean result = TimeSlotUtil.isOverlap(
                LocalTime.of(8, 0),
                LocalTime.of(9, 0),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0)
        );

        assertFalse(result);
    }
}
