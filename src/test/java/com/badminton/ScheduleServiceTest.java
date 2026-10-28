
package com.badminton;

import com.badminton.util.TimeSlotUtil;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ScheduleServiceTest {

    @Test
    void overlap() {

        boolean result = TimeSlotUtil.isOverlap(
                LocalTime.of(8, 0),
                LocalTime.of(9, 0),
                LocalTime.of(8, 30),
                LocalTime.of(10, 0)
        );

        assertTrue(result);
    }
}
