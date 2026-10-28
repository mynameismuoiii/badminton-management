package com.badminton.view;

import com.badminton.model.Court;
import com.badminton.service.SuggestionService;
import com.badminton.util.AppFonts;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SuggestionPanel extends JPanel {

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


    private final SuggestionService suggestionService =
            new SuggestionService();


    private JSpinner dateSpinner;

    private JComboBox<String> startTimeCombo;

    private JComboBox<String> endTimeCombo;

    private JTable table;

    private DefaultTableModel tableModel;

    private JLabel resultLabel;


    public SuggestionPanel() {

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
                        "Gợi ý sân trống"
                );

        title.setFont(
                AppFonts.bold(26f)
        );

        title.setForeground(
                TEXT
        );


        JLabel description =
                new JLabel(
                        "Chọn ngày và khung giờ để tìm sân còn trống."
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


        // =========================
        // DATE
        // =========================

        JLabel dateLabel =
                new JLabel("Ngày:");

        dateLabel.setFont(
                AppFonts.medium(13f)
        );


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

        dateSpinner.setEditor(
                new JSpinner.DateEditor(
                        dateSpinner,
                        "dd/MM/yyyy"
                )
        );

        dateSpinner.setPreferredSize(
                new Dimension(
                        130,
                        36
                )
        );


        // =========================
        // START TIME
        // =========================

        JLabel startLabel =
                new JLabel("Từ:");

        startLabel.setFont(
                AppFonts.medium(13f)
        );

        startTimeCombo =
                createTimeCombo();

        startTimeCombo.setSelectedItem(
                "18:00"
        );


        // =========================
        // END TIME
        // =========================

        JLabel endLabel =
                new JLabel("Đến:");

        endLabel.setFont(
                AppFonts.medium(13f)
        );

        endTimeCombo =
                createTimeCombo();

        endTimeCombo.setSelectedItem(
                "19:00"
        );


        // =========================
        // SEARCH BUTTON
        // =========================

        JButton searchButton =
                new JButton(
                        "Tìm sân"
                );

        searchButton.setPreferredSize(
                new Dimension(
                        100,
                        36
                )
        );

        searchButton.setFont(
                AppFonts.medium(13f)
        );

        searchButton.setBackground(
                PRIMARY_DARK
        );

        searchButton.setForeground(
                Color.WHITE
        );

        searchButton.setFocusPainted(
                false
        );

        searchButton.setBorderPainted(
                false
        );

        searchButton.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        searchButton.addActionListener(
                e -> searchCourts()
        );


        resultLabel =
                new JLabel();

        resultLabel.setFont(
                AppFonts.regular(12f)
        );

        resultLabel.setForeground(
                MUTED
        );


        filterPanel.add(dateLabel);
        filterPanel.add(dateSpinner);

        filterPanel.add(startLabel);
        filterPanel.add(startTimeCombo);

        filterPanel.add(endLabel);
        filterPanel.add(endTimeCombo);

        filterPanel.add(searchButton);
        filterPanel.add(resultLabel);


        wrapper.add(title);

        wrapper.add(
                Box.createRigidArea(
                        new Dimension(
                                0,
                                5
                        )
                )
        );

        wrapper.add(description);

        wrapper.add(
                Box.createRigidArea(
                        new Dimension(
                                0,
                                20
                        )
                )
        );

        wrapper.add(filterPanel);

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
                "ID",
                "Tên sân",
                "Giá / giờ",
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

        table.setRowHeight(
                42
        );

        table.setFont(
                AppFonts.regular(13f)
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


        panel.add(
                scrollPane,
                BorderLayout.CENTER
        );

        return panel;
    }


    // ============================================================
    // SEARCH
    // ============================================================

    private void searchCourts() {

        try {

            LocalDate date =
                    getSelectedDate();

            LocalTime from =
                    LocalTime.parse(
                            (String)
                                    startTimeCombo
                                            .getSelectedItem()
                    );

            LocalTime to =
                    LocalTime.parse(
                            (String)
                                    endTimeCombo
                                            .getSelectedItem()
                    );


            List<Court> courts =
                    suggestionService.suggest(
                            date,
                            from,
                            to
                    );


            tableModel.setRowCount(
                    0
            );


            for (Court court : courts) {

                tableModel.addRow(
                        new Object[] {
                                court.id(),
                                court.name(),
                                formatMoney(
                                        court.pricePerHour()
                                ),
                                "Còn trống"
                        }
                );
            }


            resultLabel.setText(
                    courts.size()
                    + " sân phù hợp"
            );


        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Không thể tìm sân",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }


    // ============================================================
    // TIME COMBO
    // ============================================================

    private JComboBox<String> createTimeCombo() {

        JComboBox<String> combo =
                new JComboBox<>();

        LocalTime current =
                LocalTime.of(
                        6,
                        0
                );

        LocalTime closing =
                LocalTime.of(
                        22,
                        0
                );


        while (!current.isAfter(closing)) {

            combo.addItem(
                    current.toString()
            );

            current =
                    current.plusMinutes(
                            30
                    );
        }


        combo.setPreferredSize(
                new Dimension(
                        90,
                        36
                )
        );

        combo.setFont(
                AppFonts.regular(13f)
        );

        return combo;
    }


    // ============================================================
    // DATE
    // ============================================================

    private LocalDate getSelectedDate() {

        Date date =
                (Date) dateSpinner
                        .getValue();

        return date
                .toInstant()
                .atZone(
                        ZoneId.systemDefault()
                )
                .toLocalDate();
    }


    // ============================================================
    // MONEY
    // ============================================================

    private String formatMoney(
            BigDecimal amount
    ) {

        NumberFormat format =
                NumberFormat.getCurrencyInstance(
                        new Locale(
                                "vi",
                                "VN"
                        )
                );

        return format.format(
                amount
        );
    }
}