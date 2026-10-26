package com.badminton.view;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class StatisticsPanel extends JPanel {

    public StatisticsPanel() {

        setLayout(new BorderLayout());

        JLabel title = new JLabel(
                "Thống kê",
                SwingConstants.CENTER
        );

        title.setFont(
                title.getFont().deriveFont(Font.BOLD, 22f)
        );

        add(title, BorderLayout.NORTH);

        // TODO N2:
        // Thống kê tổng số booking.
        // Thống kê tổng doanh thu.
        // Thống kê theo ngày/tháng.
        // Hiển thị bảng hoặc biểu đồ.
        // Kết nối StatisticsService.

        add(
                new JLabel(
                        "TODO N2: giao diện thống kê",
                        SwingConstants.CENTER
                ),
                BorderLayout.CENTER
        );
    }
}