package com.badminton.view;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class BookingPanel extends JPanel {

    public BookingPanel() {

        setLayout(new BorderLayout());

        JLabel title = new JLabel(
                "Đặt sân",
                SwingConstants.CENTER
        );

        title.setFont(
                title.getFont().deriveFont(Font.BOLD, 22f)
        );

        add(title, BorderLayout.NORTH);

        // TODO N1:
        // Chọn khách hàng.
        // Chọn sân.
        // Chọn ngày đặt.
        // Chọn giờ bắt đầu và giờ kết thúc.
        // Tạo nút Đặt sân, Hủy đặt sân.
        // Kết nối BookingService.

        add(
                new JLabel(
                        "TODO N1: giao diện đặt sân",
                        SwingConstants.CENTER
                ),
                BorderLayout.CENTER
        );
    }
}