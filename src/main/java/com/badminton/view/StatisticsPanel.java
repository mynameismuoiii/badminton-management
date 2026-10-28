package com.badminton.view;

import com.badminton.service.StatisticsService;
import com.badminton.util.AppFonts;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class StatisticsPanel extends JPanel {

    private static final Color PRIMARY =
            new Color(47, 201, 174);

    private static final Color PRIMARY_DARK =
            new Color(24, 156, 139);

    private static final Color BACKGROUND =
            new Color(243, 248, 247);

    private static final Color TEXT =
            new Color(28, 55, 50);

    private static final Color MUTED =
            new Color(116, 137, 133);


    private final StatisticsService statisticsService =
            new StatisticsService();


    private JLabel bookingValue;

    private JLabel courtValue;

    private JLabel paymentValue;

    private JLabel revenueValue;


    public StatisticsPanel() {

        setLayout(
                new BorderLayout(
                        0,
                        25
                )
        );

        setBackground(
                BACKGROUND
        );

        setBorder(
                new EmptyBorder(
                        25,
                        30,
                        30,
                        30
                )
        );


        add(
                createHeader(),
                BorderLayout.NORTH
        );

        add(
                createDashboard(),
                BorderLayout.CENTER
        );


        loadStatistics();
    }


    // ============================================================
    // HEADER
    // ============================================================

    private JPanel createHeader() {

        JPanel panel =
                new JPanel(
                        new BorderLayout()
                );

        panel.setOpaque(false);


        JPanel textPanel =
                new JPanel();

        textPanel.setOpaque(false);

        textPanel.setLayout(
                new BoxLayout(
                        textPanel,
                        BoxLayout.Y_AXIS
                )
        );


        JLabel title =
                new JLabel(
                        "Thống kê hệ thống"
                );

        title.setFont(
                AppFonts.bold(26f)
        );

        title.setForeground(
                TEXT
        );


        JLabel description =
                new JLabel(
                        "Theo dõi tổng quan hoạt động của TH-MINTON."
                );

        description.setFont(
                AppFonts.regular(13f)
        );

        description.setForeground(
                MUTED
        );


        textPanel.add(title);

        textPanel.add(
                Box.createRigidArea(
                        new Dimension(
                                0,
                                5
                        )
                )
        );

        textPanel.add(description);


        JButton refreshButton =
                new JButton(
                        "Làm mới"
                );

        refreshButton.setPreferredSize(
                new Dimension(
                        105,
                        38
                )
        );

        refreshButton.setBackground(
                PRIMARY_DARK
        );

        refreshButton.setForeground(
                Color.WHITE
        );

        refreshButton.setFont(
                AppFonts.medium(13f)
        );

        refreshButton.setFocusPainted(
                false
        );

        refreshButton.setBorderPainted(
                false
        );

        refreshButton.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        refreshButton.addActionListener(
                e -> loadStatistics()
        );


        panel.add(
                textPanel,
                BorderLayout.WEST
        );

        panel.add(
                refreshButton,
                BorderLayout.EAST
        );


        return panel;
    }


    // ============================================================
    // DASHBOARD
    // ============================================================

    private JPanel createDashboard() {

        JPanel dashboard =
                new JPanel(
                        new GridLayout(
                                2,
                                2,
                                20,
                                20
                        )
                );

        dashboard.setOpaque(false);


        bookingValue =
                new JLabel("0");

        courtValue =
                new JLabel("0");

        paymentValue =
                new JLabel("0");

        revenueValue =
                new JLabel("0 ₫");


        dashboard.add(
                createStatCard(
                        "Tổng lượt đặt sân",
                        bookingValue
                )
        );


        dashboard.add(
                createStatCard(
                        "Sân đang hoạt động",
                        courtValue
                )
        );


        dashboard.add(
                createStatCard(
                        "Số thanh toán",
                        paymentValue
                )
        );


        dashboard.add(
                createStatCard(
                        "Tổng doanh thu",
                        revenueValue
                )
        );


        return dashboard;
    }


    // ============================================================
    // STAT CARD
    // ============================================================

    private JPanel createStatCard(
            String title,
            JLabel valueLabel
    ) {

        JPanel card =
                new JPanel();

        card.setBackground(
                Color.WHITE
        );

        card.setLayout(
                new BoxLayout(
                        card,
                        BoxLayout.Y_AXIS
                )
        );

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(
                                        226,
                                        238,
                                        235
                                ),
                                1
                        ),
                        new EmptyBorder(
                                30,
                                30,
                                30,
                                30
                        )
                )
        );


        JLabel titleLabel =
                new JLabel(
                        title
                );

        titleLabel.setFont(
                AppFonts.medium(14f)
        );

        titleLabel.setForeground(
                MUTED
        );


        valueLabel.setFont(
                AppFonts.bold(30f)
        );

        valueLabel.setForeground(
                PRIMARY_DARK
        );


        card.add(
                titleLabel
        );

        card.add(
                Box.createRigidArea(
                        new Dimension(
                                0,
                                14
                        )
                )
        );

        card.add(
                valueLabel
        );


        return card;
    }


    // ============================================================
    // LOAD
    // ============================================================

    private void loadStatistics() {

        try {

            bookingValue.setText(
                    String.valueOf(
                            statisticsService
                                    .totalBookings()
                    )
            );


            courtValue.setText(
                    String.valueOf(
                            statisticsService
                                    .activeCourts()
                    )
            );


            paymentValue.setText(
                    String.valueOf(
                            statisticsService
                                    .totalPayments()
                    )
            );


            revenueValue.setText(
                    formatMoney(
                            statisticsService
                                    .revenue()
                    )
            );


        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Không thể tải thống kê",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }


    // ============================================================
    // MONEY
    // ============================================================

    private String formatMoney(
            BigDecimal value
    ) {

        NumberFormat format =
                NumberFormat.getCurrencyInstance(
                        new Locale(
                                "vi",
                                "VN"
                        )
                );

        return format.format(
                value
        );
    }
}