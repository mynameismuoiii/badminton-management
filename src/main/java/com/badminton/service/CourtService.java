package com.badminton.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import com.badminton.dao.CourtDAO;
import com.badminton.model.Court;

public class CourtService {

    private final CourtDAO courtDAO = new CourtDAO();

    public List<Court> listCourts() {
        try {
            return courtDAO.findAll();
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể tải danh sách sân.", e
            );
        }
    }

    public long addCourt(
            String name,
            BigDecimal pricePerHour,
            boolean active
    ) {

        validate(name, pricePerHour);

        Court court = new Court(
                0,
                name.trim(),
                pricePerHour,
                active
        );

        try {
            return courtDAO.insert(court);
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể thêm sân.", e
            );
        }
    }

    public boolean updateCourt(Court court) {

        validate(
                court.name(),
                court.pricePerHour()
        );

        try {
            return courtDAO.update(court);
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể cập nhật sân.", e
            );
        }
    }

    public boolean deleteCourt(long id) {
        try {
            return courtDAO.delete(id);
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể xóa/ngừng hoạt động sân.", e
            );
        }
    }

    private void validate(
            String name,
            BigDecimal pricePerHour
    ) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Tên sân không được để trống."
            );
        }

        if (pricePerHour == null ||
                pricePerHour.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Giá sân phải lớn hơn 0."
            );
        }
    }
}