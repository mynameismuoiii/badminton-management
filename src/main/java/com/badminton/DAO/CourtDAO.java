package com.badminton.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.badminton.config.DatabaseConnection;
import com.badminton.model.Court;

public class CourtDAO {

    // Lấy tất cả sân
    public List<Court> findAll() throws SQLException {

        String sql = """
                SELECT id, name, price_per_hour, active
                FROM courts
                ORDER BY id
                """;

        List<Court> courts = new ArrayList<>();

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet resultSet =
                        statement.executeQuery()
        ) {

            while (resultSet.next()) {

                Court court = new Court(
                        resultSet.getLong("id"),
                        resultSet.getString("name"),
                        resultSet.getBigDecimal("price_per_hour"),
                        resultSet.getBoolean("active")
                );

                courts.add(court);
            }
        }

        return courts;
    }


    // Tìm sân theo ID
    public Optional<Court> findById(long id)
            throws SQLException {

        String sql = """
                SELECT id, name, price_per_hour, active
                FROM courts
                WHERE id = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(1, id);

            try (ResultSet resultSet =
                    statement.executeQuery()) {

                if (resultSet.next()) {

                    Court court = new Court(
                            resultSet.getLong("id"),
                            resultSet.getString("name"),
                            resultSet.getBigDecimal("price_per_hour"),
                            resultSet.getBoolean("active")
                    );

                    return Optional.of(court);
                }
            }
        }

        return Optional.empty();
    }


    // Thêm sân
    public long insert(Court court)
            throws SQLException {

        String sql = """
                INSERT INTO courts
                    (name, price_per_hour, active)
                VALUES (?, ?, ?)
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(
                                sql,
                                Statement.RETURN_GENERATED_KEYS
                        )
        ) {

            statement.setString(
                    1,
                    court.name()
            );

            statement.setBigDecimal(
                    2,
                    court.pricePerHour()
            );

            statement.setBoolean(
                    3,
                    court.active()
            );

            int affectedRows =
                    statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException(
                        "Thêm sân thất bại."
                );
            }

            try (ResultSet generatedKeys =
                    statement.getGeneratedKeys()) {

                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
            }
        }

        throw new SQLException(
                "Thêm sân thành công nhưng không lấy được ID."
        );
    }


    // Sửa sân
    public boolean update(Court court)
            throws SQLException {

        String sql = """
                UPDATE courts
                SET name = ?,
                    price_per_hour = ?,
                    active = ?
                WHERE id = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    court.name()
            );

            statement.setBigDecimal(
                    2,
                    court.pricePerHour()
            );

            statement.setBoolean(
                    3,
                    court.active()
            );

            statement.setLong(
                    4,
                    court.id()
            );

            int affectedRows =
                    statement.executeUpdate();

            return affectedRows > 0;
        }
    }


    // Ngừng hoạt động sân
    public boolean delete(long id)
            throws SQLException {

        String sql = """
                UPDATE courts
                SET active = FALSE
                WHERE id = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(1, id);

            int affectedRows =
                    statement.executeUpdate();

            return affectedRows > 0;
        }
    }
}