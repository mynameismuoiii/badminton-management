package com.badminton.view;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class SuggestionPanel extends JPanel {

    public SuggestionPanel() {

        setLayout(new BorderLayout());

        JLabel title = new JLabel(
                "Gợi ý sân trống",
                SwingConstants.CENTER
        );

        title.setFont(
                title.getFont().deriveFont(Font.BOLD, 22f)
        );

        add(title, BorderLayout.NORTH);

        // TODO N2:
        // Chọn ngày cần đặt sân.
        // Chọn giờ bắt đầu, giờ kết thúc.
        // Nút tìm sân trống.
        // Hiển thị danh sách sân phù hợp.
        // Kết nối SuggestionService.

        add(
                new JLabel(
                        "TODO N2: giao diện gợi ý sân",
                        SwingConstants.CENTER
                ),
                BorderLayout.CENTER
        );
    }
}