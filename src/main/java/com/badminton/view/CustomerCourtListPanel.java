package com.badminton.view;

import com.badminton.model.Court;
import com.badminton.service.CourtService;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

public final class CustomerCourtListPanel extends JPanel {

    private final CourtService courtService = new CourtService();

    // Ham chuyen sang giao dien Dat san
    private final Consumer<Long> onBookCourt;

    private final DefaultTableModel tableModel =
        new DefaultTableModel(
            new Object[]{
                "Mã sân",
                "Tên sân",
                "Giá / giờ"
            },
            0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

    private final JTable table = new JTable(tableModel);

    private final JButton refresh =
        new JButton("Làm mới");

    private final JButton bookButton =
        new JButton("Đặt sân này");

    private final JLabel selectedLabel =
        new JLabel("Hãy chọn một sân trong bảng.");

    // Constructor cu
    public CustomerCourtListPanel() {
        this(null);
    }

    // Constructor moi nhan callback dat san
    public CustomerCourtListPanel(Consumer<Long> onBookCourt) {

        this.onBookCourt = onBookCourt;

        setLayout(new BorderLayout(12, 12));

        setBorder(
            BorderFactory.createEmptyBorder(
                16, 16, 16, 16
            )
        );

        // =============================
        // HEADER
        // =============================

        JPanel header = new JPanel(new BorderLayout());

        JLabel title = new JLabel(
            "DANH SÁCH SÂN ĐANG HOẠT ĐỘNG",
            SwingConstants.LEFT
        );

        header.add(title, BorderLayout.CENTER);

        JPanel actions = new JPanel(
            new FlowLayout(FlowLayout.RIGHT, 0, 0)
        );

        actions.add(refresh);

        header.add(actions, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // =============================
        // BANG DANH SACH SAN
        // =============================

        table.setFillsViewportHeight(true);
        table.setRowHeight(30);

        table.setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION
        );

        table.setAutoCreateRowSorter(true);

        table.getTableHeader()
            .setReorderingAllowed(false);

        add(
            new JScrollPane(table),
            BorderLayout.CENTER
        );

        // =============================
        // FOOTER VA NUT DAT SAN
        // =============================

        JPanel footer = new JPanel(
            new BorderLayout(10, 0)
        );

        footer.add(
            selectedLabel,
            BorderLayout.CENTER
        );

        footer.add(
            bookButton,
            BorderLayout.EAST
        );

        // Ban dau an nut Dat san
        bookButton.setVisible(false);

        add(footer, BorderLayout.SOUTH);

        // =============================
        // SU KIEN CHON SAN
        // =============================

        table.getSelectionModel()
            .addListSelectionListener(e -> {

                if (!e.getValueIsAdjusting()) {
                    updateSelection();
                }
            });

        // Bam Dat san nay
        bookButton.addActionListener(
            e -> bookSelectedCourt()
        );

        // Bam Lam moi
        refresh.addActionListener(
            e -> loadCourts()
        );

        loadCourts();
    }

    // =============================
    // LAY ID SAN DANG CHON
    // =============================

    private Long selectedCourtId() {

        int viewRow = table.getSelectedRow();

        if (viewRow < 0) {
            return null;
        }

        int modelRow =
            table.convertRowIndexToModel(viewRow);

        Object idValue =
            tableModel.getValueAt(modelRow, 0);

        if (idValue instanceof Number number) {
            return number.longValue();
        }

        return null;
    }

    // =============================
    // HIEN NUT KHI CHON SAN
    // =============================

    private void updateSelection() {

        Long courtId = selectedCourtId();

        if (courtId == null) {

            selectedLabel.setText(
                "Hãy chọn một sân trong bảng."
            );

            bookButton.setVisible(false);

            return;
        }

        int row = table.convertRowIndexToModel(
            table.getSelectedRow()
        );

        String courtName =
            tableModel.getValueAt(row, 1).toString();

        selectedLabel.setText(
            "Đã chọn: " + courtName
        );

        bookButton.setVisible(
            onBookCourt != null
        );
    }

    // =============================
    // CHUYEN SANG GIAO DIEN DAT SAN
    // =============================

    private void bookSelectedCourt() {

        Long courtId = selectedCourtId();

        if (courtId == null || onBookCourt == null) {

            JOptionPane.showMessageDialog(
                this,
                "Hãy chọn sân trước khi đặt."
            );

            return;
        }

        // Truyen ID san sang RoleBasedMainFrame
        // Chua tao don dat san tai day
        onBookCourt.accept(courtId);
    }

    // =============================
    // LOAD DANH SACH SAN TU DATABASE
    // =============================

    private void loadCourts() {

        refresh.setEnabled(false);
        bookButton.setVisible(false);

        new SwingWorker<List<Court>, Void>() {

            @Override
            protected List<Court> doInBackground() {

                return courtService.listCourts();
            }

            @Override
            protected void done() {

                refresh.setEnabled(true);

                try {

                    List<Court> courts = get();

                    tableModel.setRowCount(0);

                    NumberFormat money =
                        NumberFormat.getNumberInstance(
                            Locale.forLanguageTag("vi-VN")
                        );

                    for (Court court : courts) {

                        if (!court.active()) {
                            continue;
                        }

                        BigDecimal price =
                            court.pricePerHour();

                        tableModel.addRow(
                            new Object[]{
                                court.id(),
                                court.name(),
                                price == null
                                    ? ""
                                    : money.format(price) + " đ"
                            }
                        );
                    }

                    table.clearSelection();

                    updateSelection();

                } catch (InterruptedException ex) {

                    Thread.currentThread().interrupt();

                    showError();

                } catch (ExecutionException ex) {

                    showError();
                }
            }
        }.execute();
    }

    // =============================
    // HIEN THI LOI
    // =============================

    private void showError() {

        JOptionPane.showMessageDialog(
            this,
            "Không thể tải danh sách sân. "
                + "Vui lòng kiểm tra MySQL.",
            "TH-MINTON",
            JOptionPane.ERROR_MESSAGE
        );
    }
}