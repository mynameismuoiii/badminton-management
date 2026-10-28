package com.badminton.service;

import com.badminton.dao.CustomerDAO;
import com.badminton.dao.CustomerManagementDAO;
import com.badminton.dao.CustomerManagementDAO.BookingHistory;
import com.badminton.dao.CustomerManagementDAO.Summary;
import com.badminton.model.Customer;
import com.badminton.model.User;

import java.sql.SQLException;
import java.util.List;

/** Business operations for administrators; Customers cannot construct this service. */
public final class CustomerService {
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final CustomerManagementDAO reports = new CustomerManagementDAO();

    public CustomerService(User signedIn) {
        if (signedIn == null || !"ADMIN".equalsIgnoreCase(signedIn.role())) {
            throw new SecurityException("Chỉ quản trị viên mới được quản lý khách hàng.");
        }
    }

    public List<Customer> listCustomers() throws SQLException {
        return customerDAO.findAll();
    }

    public List<Summary> listSummaries() throws SQLException {
        return reports.findSummaries();
    }

    public List<BookingHistory> listBookingHistory(long customerId) throws SQLException {
        requireId(customerId);
        return reports.findBookings(customerId);
    }

    /** Adds a customer profile only; does NOT create a login/password. */
    public long addCustomer(String fullName, String phone, String email) throws SQLException {
        try {
            return customerDAO.insert(validated(0, fullName, phone, email));
        } catch (SQLException ex) {
            throw translateSqlException(ex);
        }
    }

    /** Also syncs full name/phone with a linked user, using CustomerDAO transaction. */
    public boolean updateCustomer(long customerId, String fullName, String phone, String email)
            throws SQLException {
        requireId(customerId);
        try {
            return customerDAO.update(validated(customerId, fullName, phone, email));
        } catch (SQLException ex) {
            throw translateSqlException(ex);
        }
    }

    /** Existing CustomerDAO only deletes customers without a linked login; FK protects bookings. */
    public boolean deleteCustomer(long customerId) throws SQLException {
        requireId(customerId);
        try {
            return customerDAO.delete(customerId);
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 1451) {
                throw new IllegalStateException(
                        "Khách đã có lịch đặt sân hoặc dữ liệu liên quan nên không thể xóa.", ex);
            }
            throw ex;
        }
    }

    private static void requireId(long id) {
        if (id <= 0) throw new IllegalArgumentException("Mã khách hàng không hợp lệ.");
    }

    private static Customer validated(long id, String fullName, String phone, String email) {
        String name = fullName == null ? "" : fullName.strip();
        String number = phone == null ? "" : phone.strip();
        String mail = email == null ? "" : email.strip();
        if (name.isEmpty() || name.codePointCount(0, name.length()) > 120) {
            throw new IllegalArgumentException("Họ tên phải có từ 1 đến 120 ký tự.");
        }
        if (!number.matches("0[0-9]{9}")) {
            throw new IllegalArgumentException("Số điện thoại phải có 10 chữ số, bắt đầu bằng số 0.");
        }
        if (mail.length() > 120 || (!mail.isEmpty()
                && !mail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))) {
            throw new IllegalArgumentException("Email không hợp lệ (tối đa 120 ký tự).");
        }
        return new Customer(id, name, number, mail.isEmpty() ? null : mail);
    }

    private static SQLException translateSqlException(SQLException ex) {
        if (ex.getErrorCode() == 1062) {
            return new SQLException("Số điện thoại đã có trong hệ thống hoặc trùng với tài khoản.",
                    ex.getSQLState(), ex.getErrorCode(), ex);
        }
        return ex;
    }
}
