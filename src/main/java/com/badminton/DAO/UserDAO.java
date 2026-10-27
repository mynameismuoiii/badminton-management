
package com.badminton.dao;

import com.badminton.config.DatabaseConnection;
import com.badminton.model.User;

import java.sql.*;
import java.util.Optional;

public class UserDAO {

    // Tim tai khoan khi dang nhap
    public Optional<User> findByUsername(
            String username
    ) throws SQLException {

        String sql =
            "SELECT id, username, password_hash, role " +
            "FROM users WHERE username = ?";

        try (
            Connection conn =
                DatabaseConnection.getConnection();
            PreparedStatement ps =
                conn.prepareStatement(sql)
        ) {
            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                return Optional.of(new User(
                    rs.getLong("id"),
                    rs.getString("username"),
                    rs.getString("password_hash"),
                    rs.getString("role")
                ));
            }
        }
    }

    // Lay ID khach hang tu ID tai khoan
    public Optional<Long> findCustomerIdByUserId(
            long userId
    ) throws SQLException {

        String sql =
            "SELECT id FROM customers WHERE user_id = ?";

        try (
            Connection conn =
                DatabaseConnection.getConnection();
            PreparedStatement ps =
                conn.prepareStatement(sql)
        ) {
            ps.setLong(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getLong("id"));
                }

                return Optional.empty();
            }
        }
    }

    // Dang ky tai khoan moi
    // Giu nguyen chu ky ham AuthService dang goi
    public long createCustomerAccount(
            String fullName,
            String username,
            String phone,
            String passwordHash
    ) throws SQLException {

        String insertUser =
            "INSERT INTO users " +
            "(username, full_name, phone, password_hash, role) " +
            "VALUES (?, ?, ?, ?, 'CUSTOMER')";

        String insertCustomer =
            "INSERT INTO customers " +
            "(user_id, full_name, phone, email) " +
            "VALUES (?, ?, ?, NULL)";

        try (Connection conn =
                 DatabaseConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {
                long userId;

                // Buoc 1: Tao tai khoan
                try (PreparedStatement ps =
                         conn.prepareStatement(
                             insertUser,
                             Statement.RETURN_GENERATED_KEYS
                         )) {

                    ps.setString(1, username);
                    ps.setString(2, fullName);
                    ps.setString(3, phone);
                    ps.setString(4, passwordHash);

                    ps.executeUpdate();

                    try (ResultSet keys =
                             ps.getGeneratedKeys()) {

                        if (!keys.next()) {
                            throw new SQLException(
                                "Khong lay duoc user ID"
                            );
                        }

                        userId = keys.getLong(1);
                    }
                }

                // Buoc 2: Tao ho so khach hang
                try (PreparedStatement ps =
                         conn.prepareStatement(
                             insertCustomer
                         )) {

                    ps.setLong(1, userId);
                    ps.setString(2, fullName);
                    ps.setString(3, phone);

                    ps.executeUpdate();
                }

                // Buoc 3: Luu ca hai bang
                conn.commit();

                return userId;

            } catch (SQLException | RuntimeException ex) {

                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    ex.addSuppressed(rollbackError);
                }

                throw ex;
            }
        }
    }
}
