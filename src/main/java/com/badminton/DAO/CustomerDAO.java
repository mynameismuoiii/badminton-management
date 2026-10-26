package com.badminton.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.badminton.model.Customer;

public class CustomerDAO {

    public List<Customer> findAll() throws SQLException {

        // TODO N1: lấy danh sách khách hàng
        throw new UnsupportedOperationException("N1: findAll");
    }

    public Optional<Customer> findById(long id)
            throws SQLException {

        // TODO N1: tìm khách hàng theo ID
        throw new UnsupportedOperationException("N1: findById");
    }

    public long insert(Customer customer)
            throws SQLException {

        // TODO N1: thêm khách hàng, trả về ID
        throw new UnsupportedOperationException("N1: insert");
    }

    public boolean update(Customer customer)
            throws SQLException {

        // TODO N1: cập nhật khách hàng
        throw new UnsupportedOperationException("N1: update");
    }

    public boolean delete(long id)
            throws SQLException {

        // TODO N1: xóa khách hàng
        throw new UnsupportedOperationException("N1: delete");
    }
}