package com.badminton.view;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class CourtPanel extends JPanel {

    public CourtPanel() {

        setLayout(new BorderLayout());

        JLabel title = new JLabel(
                "Quản lý sân",
                SwingConstants.CENTER
        );

        title.setFont(
                title.getFont().deriveFont(Font.BOLD, 22f)
        );

        add(title, BorderLayout.NORTH);

        // TODO N2:
        // Tạo JTable hiển thị danh sách sân.
        // Thêm form nhập thông tin sân.
        // Thêm nút Thêm, Sửa, Xóa, Tìm kiếm.
        // Kết nối CourtService.

        add(
                new JLabel(
                        "TODO N2: giao diện quản lý sân",
                        SwingConstants.CENTER
                ),
                BorderLayout.CENTER
        );
    }
}