package com.badminton.view;

import com.badminton.model.Booking;
import com.badminton.service.ScheduleService;
import com.badminton.util.AppFonts;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class SchedulePanel extends JPanel {

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


    private final ScheduleService scheduleService =
            new ScheduleService();


    private JTable table;

    private DefaultTableModel tableModel;

    private JSpinner dateSpinner;

    private JLabel resultLabel;


    public SchedulePanel() {

        setLayout(
                new BorderLayout()
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
                createTopPanel(),
                BorderLayout.NORTH
        );

        add(
                createTablePanel(),
                BorderLayout.CENTER
        );


        loadSchedule();
    }


    // ============================================================
    // TOP
    // ============================================================

    private JPanel createTopPanel() {

        JPanel wrapper =
                new JPanel();

        wrapper.setOpaque(false);

        wrapper.setLayout(
                new BoxLayout(
                        wrapper,
                        BoxLayout.Y_AXIS
                )
        );


        JLabel title =
                new JLabel(
                        "Lịch sân"
                );

        title.setFont(
                AppFonts.bold(26f)
        );

        title.setForeground(
                TEXT
        );


        JLabel description =
                new JLabel(
                        "Theo dõi các lịch đặt sân theo ngày."
                );

        description.setFont(
                AppFonts.regular(13f)
        );

        description.setForeground(
                MUTED
        );


        JPanel filterPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT,
                                10,
                                0
                        )
                );

        filterPanel.setOpaque(false);


        JLabel dateLabel =
                new JLabel(
                        "Ngày:"
                );

        dateLabel.setFont(
                AppFonts.medium(13f)
        );

        dateLabel.setForeground(
                TEXT
        );


        // =====================================================
        // DATE PICKER
        // =====================================================

        SpinnerDateModel dateModel =
                new SpinnerDateModel(
                        new Date(),
                        null,
                        null,
                        java.util.Calendar.DAY_OF_MONTH
                );

        dateSpinner =
                new JSpinner(
                        dateModel
                );

        JSpinner.DateEditor editor =
                new JSpinner.DateEditor(
                        dateSpinner,
                        "dd/MM/yyyy"
                );

        dateSpinner.setEditor(
                editor
        );

        dateSpinner.setPreferredSize(
                new Dimension(
                        140,
                        36
                )
        );

        dateSpinner.setFont(
                AppFonts.regular(13f)
        );


        // =====================================================
        // BUTTON REFRESH
        // =====================================================

        JButton refreshButton =
                new JButton(
                        "Xem lịch"
                );

        refreshButton.setPreferredSize(
                new Dimension(
                        110,
                        36
                )
        );

        refreshButton.setFont(
                AppFonts.medium(13f)
        );

        refreshButton.setForeground(
                Color.WHITE
        );

        refreshButton.setBackground(
                PRIMARY_DARK
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
                e -> loadSchedule()
        );


        resultLabel =
                new JLabel();

        resultLabel.setFont(
                AppFonts.regular(12f)
        );

        resultLabel.setForeground(
                MUTED
        );


        filterPanel.add(
                dateLabel
        );

        filterPanel.add(
                dateSpinner
        );

        filterPanel.add(
                refreshButton
        );

        filterPanel.add(
                resultLabel
        );


        wrapper.add(
                title
        );

        wrapper.add(
                Box.createRigidArea(
                        new Dimension(
                                0,
                                5
                        )
                )
        );

        wrapper.add(
                description
        );

        wrapper.add(
                Box.createRigidArea(
                        new Dimension(
                                0,
                                20
                        )
                )
        );

        wrapper.add(
                filterPanel
        );

        wrapper.add(
                Box.createRigidArea(
                        new Dimension(
                                0,
                                20
                        )
                )
        );


        return wrapper;
    }


    // ============================================================
    // TABLE
    // ============================================================

    private JPanel createTablePanel() {

        JPanel panel =
                new JPanel(
                        new BorderLayout()
                );

        panel.setBackground(
                Color.WHITE
        );

        panel.setBorder(
                new EmptyBorder(
                        15,
                        15,
                        15,
                        15
                )
        );


        String[] columns = {
                "Booking ID",
                "Sân",
                "Khách hàng",
                "Bắt đầu",
                "Kết thúc",
                "Trạng thái"
        };


        tableModel =
                new DefaultTableModel(
                        columns,
                        0
                ) {

                    @Override
                    public boolean isCellEditable(
                            int row,
                            int column
                    ) {

                        return false;
                    }
                };


        table =
                new JTable(
                        tableModel
                );

        table.setFont(
                AppFonts.regular(13f)
        );

        table.setRowHeight(
                40
        );

        table.setForeground(
                TEXT
        );

        table.setGridColor(
                new Color(
                        230,
                        238,
                        236
                )
        );

        table.setSelectionBackground(
                new Color(
                        218,
                        248,
                        241
                )
        );

        table.setSelectionForeground(
                TEXT
        );

        table.getTableHeader()
                .setFont(
                        AppFonts.medium(13f)
                );

        table.getTableHeader()
                .setBackground(
                        new Color(
                                230,
                                247,
                                243
                        )
                );

        table.getTableHeader()
                .setForeground(
                        TEXT
                );


        JScrollPane scrollPane =
                new JScrollPane(
                        table
                );

        scrollPane.setBorder(
                BorderFactory.createEmptyBorder()
        );

        scrollPane.getViewport()
                .setBackground(
                        Color.WHITE
                );


        panel.add(
                scrollPane,
                BorderLayout.CENTER
        );


        return panel;
    }


    // ============================================================
    // LOAD DATA
    // ============================================================

    private void loadSchedule() {

        try {

            LocalDate date =
                    getSelectedDate();

            List<Booking> bookings =
                    scheduleService.schedule(
                            date
                    );


            tableModel.setRowCount(
                    0
            );


            DateTimeFormatter timeFormatter =
                    DateTimeFormatter.ofPattern(
                            "HH:mm"
                    );


            for (Booking booking : bookings) {

                // Booking đã hủy vẫn hiển thị,
                // để quản lý có thể xem lịch sử trạng thái.

                tableModel.addRow(
                        new Object[] {

                                booking.id(),

                                "Sân "
                                        + booking.courtId(),

                                "KH "
                                        + booking.customerId(),

                                booking.startTime()
                                        .format(
                                                timeFormatter
                                        ),

                                booking.endTime()
                                        .format(
                                                timeFormatter
                                        ),

                                statusText(
                                        booking.status()
                                )
                        }
                );
            }


            resultLabel.setText(
                    bookings.size()
                    + " lịch đặt"
            );


        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }


    // ============================================================
    // DATE
    // ============================================================

    private LocalDate getSelectedDate() {

        Date selectedDate =
                (Date) dateSpinner
                        .getValue();


        return selectedDate
                .toInstant()
                .atZone(
                        ZoneId.systemDefault()
                )
                .toLocalDate();
    }


    // ============================================================
    // STATUS
    // ============================================================

    private String statusText(
            String status
    ) {

        if (status == null) {
            return "";
        }


        return switch (status) {

            case "PENDING" ->
                    "Chờ xác nhận";

            case "CONFIRMED" ->
                    "Đã xác nhận";

            case "CANCELLED" ->
                    "Đã hủy";

            default ->
                    status;
        };
    }
}