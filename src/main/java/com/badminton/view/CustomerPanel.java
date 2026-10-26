package com.badminton.view;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class CustomerPanel extends JPanel {

    public CustomerPanel() {

        setLayout(new BorderLayout());

        JLabel title = new JLabel(
                "Quản lý khách hàng",
                SwingConstants.CENTER
        );

        title.setFont(
                title.getFont().deriveFont(Font.BOLD, 22f)
        );

        add(title, BorderLayout.NORTH);

        // TODO N1:
        // Tạo JTable danh sách khách hàng.
        // Thêm form họ tên, số điện thoại, email.
        // Thêm nút Thêm, Sửa, Xóa, Tìm kiếm.
        // Kết nối CustomerService.

        add(
                new JLabel(
                        "TODO N1: giao diện quản lý khách hàng",
                        SwingConstants.CENTER
                ),
                BorderLayout.CENTER
        );
    }
}