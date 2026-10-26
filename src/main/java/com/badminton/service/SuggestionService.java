package com.badminton.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.badminton.model.Court;

public class SuggestionService {

    public List<Court> suggest(
            LocalDate date,
            LocalTime from,
            LocalTime to
    ) {

        // TODO N2:
        // 1. Lấy các sân đang hoạt động.
        // 2. Lấy booking theo ngày của từng sân.
        // 3. Bỏ qua booking đã CANCELLED.
        // 4. Dùng TimeSlotUtil kiểm tra trùng giờ.
        // 5. Chỉ giữ lại các sân còn trống.
        // 6. Có thể sắp xếp theo giá hoặc tên.

        throw new UnsupportedOperationException(
                "N2: suggest"
        );
    }
}