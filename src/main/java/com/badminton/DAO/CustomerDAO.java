
package com.badminton.dao;

import com.badminton.config.DatabaseConnection;
import com.badminton.model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerDAO {

    public List<Customer> findAll() throws SQLException {

        String sql =
            "SELECT id, full_name, phone, email " +
            "FROM customers ORDER BY id";

        List<Customer> result = new ArrayList<>();

        try (
            Connection conn =
                DatabaseConnection.getConnection();
            PreparedStatement ps =
                conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                result.add(map(rs));
            }
        }

        return result;
    }

    public Optional<Customer> findById(
            long id
    ) throws SQLException {

        String sql =
            "SELECT id, full_name, phone, email " +
            "FROM customers WHERE id = ?";

        return findOne(sql, id);
    }

    // Ham bo sung: tim khach hang theo tai khoan
    public Optional<Customer> findByUserId(
            long userId
    ) throws SQLException {

        String sql =
            "SELECT id, full_name, phone, email " +
            "FROM customers WHERE user_id = ?";

        return findOne(sql, userId);
    }

    private Optional<Customer> findOne(
            String sql,
            long id
    ) throws SQLException {

        try (
            Connection conn =
                DatabaseConnection.getConnection();
            PreparedStatement ps =
                conn.prepareStatement(sql)
        ) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }

                return Optional.empty();
            }
        }
    }

    // Them khach vang lai
    public long insert(Customer customer)
            throws SQLException {

        String sql =
            "INSERT INTO customers " +
            "(full_name, phone, email) VALUES (?, ?, ?)";

        try (
            Connection conn =
                DatabaseConnection.getConnection();
            PreparedStatement ps =
                conn.prepareStatement(
                    sql,
                    Statement.RETURN_GENERATED_KEYS
                )
        ) {
            ps.setString(1, customer.fullName());
            ps.setString(2, customer.phone());
            ps.setString(3, customer.email());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }

                throw new SQLException(
                    "Khong lay duoc customer ID"
                );
            }
        }
    }

    // Cap nhat ho so khach hang
    public boolean update(Customer customer)
            throws SQLException {

        String readUser =
            "SELECT user_id FROM customers " +
            "WHERE id = ? FOR UPDATE";

        String updateCustomer =
            "UPDATE customers " +
            "SET full_name = ?, phone = ?, email = ? " +
            "WHERE id = ?";

        String updateUser =
            "UPDATE users " +
            "SET full_name = ?, phone = ? " +
            "WHERE id = ?";

        try (Connection conn =
                 DatabaseConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {
                Long userId = null;

                try (PreparedStatement ps =
                         conn.prepareStatement(readUser)) {

                    ps.setLong(1, customer.id());

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {
                            conn.rollback();
                            return false;
                        }

                        long id = rs.getLong("user_id");

                        if (!rs.wasNull()) {
                            userId = id;
                        }
                    }
                }

                // Cap nhat bang customers
                try (PreparedStatement ps =
                         conn.prepareStatement(updateCustomer)) {

                    ps.setString(1, customer.fullName());
                    ps.setString(2, customer.phone());
                    ps.setString(3, customer.email());
                    ps.setLong(4, customer.id());

                    ps.executeUpdate();
                }

                // Dong bo users neu da co tai khoan
                if (userId != null) {

                    try (PreparedStatement ps =
                             conn.prepareStatement(updateUser)) {

                        ps.setString(1, customer.fullName());
                        ps.setString(2, customer.phone());
                        ps.setLong(3, userId);

                        ps.executeUpdate();
                    }
                }

                conn.commit();
                return true;

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

    public boolean delete(long id)
            throws SQLException {

        // Khong xoa khach da lien ket tai khoan.
        // FK cung ngan xoa khi da co booking.
        String sql =
            "DELETE FROM customers " +
            "WHERE id = ? AND user_id IS NULL";

        try (
            Connection conn =
                DatabaseConnection.getConnection();
            PreparedStatement ps =
                conn.prepareStatement(sql)
        ) {
            ps.setLong(1, id);

            return ps.executeUpdate() > 0;
        }
    }

    private static Customer map(ResultSet rs)
            throws SQLException {

        return new Customer(
            rs.getLong("id"),
            rs.getString("full_name"),
            rs.getString("phone"),
            rs.getString("email")
        );
    }
}
