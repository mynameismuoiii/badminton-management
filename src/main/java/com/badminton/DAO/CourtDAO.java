package com.badminton.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.badminton.model.Court;

public class CourtDAO {

    // Lấy tất cả sân
    public List<Court> findAll() throws SQLException {

        // TODO N2: SELECT * FROM courts
        throw new UnsupportedOperationException("N2: findAll");
    }

    // Tìm sân theo ID
    public Optional<Court> findById(long id)
            throws SQLException {

        // TODO N2: SELECT * FROM courts WHERE id = ?
        throw new UnsupportedOperationException("N2: findById");
    }

    // Thêm sân
    public long insert(Court court) throws SQLException {

        // TODO N2: INSERT INTO courts ...
        // Trả về ID sân mới.
        throw new UnsupportedOperationException("N2: insert");
    }

    // Sửa sân
    public boolean update(Court court) throws SQLException {

        // TODO N2: UPDATE courts SET ... WHERE id = ?
        throw new UnsupportedOperationException("N2: update");
    }

    // Xóa sân
    public boolean delete(long id) throws SQLException {

        // TODO N2: DELETE FROM courts WHERE id = ?
        // Lưu ý sân đã có booking thì nên ngừng hoạt động.
        throw new UnsupportedOperationException("N2: delete");
    }
}