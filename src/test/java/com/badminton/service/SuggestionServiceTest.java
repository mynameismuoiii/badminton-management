package com.badminton.service;

import com.badminton.dao.CourtDAO;
import com.badminton.model.Court;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SuggestionServiceTest {


    @Test
    void testInvalidTimeSlot() {

        SuggestionService service =
                new SuggestionService();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.suggest(
                        LocalDate.of(
                                2026,
                                10,
                                28
                        ),
                        LocalTime.of(
                                19,
                                0
                        ),
                        LocalTime.of(
                                18,
                                0
                        )
                )
        );
    }


    @Test
    void testSuggestNoBookingDate()
            throws Exception {

        SuggestionService service =
                new SuggestionService();

        CourtDAO courtDAO =
                new CourtDAO();


        // Dùng một ngày chưa có booking để test
        LocalDate date =
                LocalDate.of(
                        2099,
                        1,
                        1
                );


        List<Court> result =
                service.suggest(
                        date,
                        LocalTime.of(
                                18,
                                0
                        ),
                        LocalTime.of(
                                19,
                                0
                        )
                );


        List<Court> activeCourts =
                courtDAO.findAll()
                        .stream()
                        .filter(
                                Court::active
                        )
                        .toList();


        assertNotNull(
                result
        );


        assertEquals(
                activeCourts.size(),
                result.size()
        );


        for (Court court : result) {

            assertTrue(
                    court.active()
            );
        }
    }
}