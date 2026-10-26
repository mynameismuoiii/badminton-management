package com.badminton.view;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class PaymentPanel extends JPanel {

    public PaymentPanel() {

        setLayout(new BorderLayout());

        JLabel title = new JLabel(
                "Thanh toán",
                SwingConstants.CENTER
        );

        title.setFont(
                title.getFont().deriveFont(Font.BOLD, 22f)
        );

        add(title, BorderLayout.NORTH);

        // TODO N1:
        // Chọn booking cần thanh toán.
        // Hiển thị thông tin khách hàng và sân.
        // Hiển thị tổng tiền.
        // Chọn phương thức thanh toán.
        // Nút xác nhận thanh toán.
        // Kết nối PaymentService.

        add(
                new JLabel(
                        "TODO N1: giao diện thanh toán",
                        SwingConstants.CENTER
                ),
                BorderLayout.CENTER
        );
    }
}