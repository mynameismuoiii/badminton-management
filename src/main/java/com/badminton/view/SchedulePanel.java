package com.badminton.view;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class SchedulePanel extends JPanel {

    public SchedulePanel() {

        setLayout(new BorderLayout());

        JLabel title = new JLabel(
                "Xem lịch sân",
                SwingConstants.CENTER
        );

        title.setFont(
                title.getFont().deriveFont(Font.BOLD, 22f)
        );

        add(title, BorderLayout.NORTH);

        // TODO N2:
        // Cho phép chọn ngày.
        // Lấy dữ liệu từ ScheduleService.
        // Hiển thị các sân và khung giờ đặt.
        // Phân biệt sân trống và sân đã đặt.

        add(
                new JLabel(
                        "TODO N2: giao diện xem lịch sân",
                        SwingConstants.CENTER
                ),
                BorderLayout.CENTER
        );
    }
}