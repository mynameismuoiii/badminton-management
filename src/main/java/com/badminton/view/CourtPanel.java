package com.badminton.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import com.badminton.model.Court;
import com.badminton.service.CourtService;

public class CourtPanel extends JPanel {

    private final CourtService courtService = new CourtService();

    private JTextField txtName;
    private JTextField txtPrice;
    private JTextField txtSearch;

    private JCheckBox chkActive;

    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;

    private long selectedCourtId = -1;

    public CourtPanel() {

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(
                10, 10, 10, 10
        ));

        createTitle();
        createCenterPanel();

        loadCourts();
    }

    private void createTitle() {

        JLabel title = new JLabel(
                "QUẢN LÝ SÂN",
                SwingConstants.CENTER
        );

        title.setFont(
                title.getFont().deriveFont(
                        Font.BOLD,
                        22f
                )
        );

        add(title, BorderLayout.NORTH);
    }

    private void createCenterPanel() {

        JPanel centerPanel =
                new JPanel(new BorderLayout(10, 10));

        centerPanel.add(
                createFormPanel(),
                BorderLayout.NORTH
        );

        centerPanel.add(
                createTablePanel(),
                BorderLayout.CENTER
        );

        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createFormPanel() {

        JPanel container =
                new JPanel(new BorderLayout(10, 10));

        JPanel form =
                new JPanel(new GridLayout(
                        3,
                        2,
                        10,
                        10
                ));

        txtName = new JTextField();
        txtPrice = new JTextField();
        chkActive = new JCheckBox(
                "Đang hoạt động",
                true
        );

        form.add(new JLabel("Tên sân:"));
        form.add(txtName);

        form.add(new JLabel("Giá / giờ:"));
        form.add(txtPrice);

        form.add(new JLabel("Trạng thái:"));
        form.add(chkActive);

        JPanel buttons =
                new JPanel(new FlowLayout(
                        FlowLayout.LEFT
                ));

        JButton btnAdd =
                new JButton("Thêm");

        JButton btnUpdate =
                new JButton("Sửa");

        JButton btnDelete =
                new JButton("Ngừng hoạt động");

        JButton btnRefresh =
                new JButton("Làm mới");

        buttons.add(btnAdd);
        buttons.add(btnUpdate);
        buttons.add(btnDelete);
        buttons.add(btnRefresh);

        btnAdd.addActionListener(
                e -> addCourt()
        );

        btnUpdate.addActionListener(
                e -> updateCourt()
        );

        btnDelete.addActionListener(
                e -> deleteCourt()
        );

        btnRefresh.addActionListener(
                e -> {
                    clearForm();
                    loadCourts();
                }
        );

        container.add(
                form,
                BorderLayout.CENTER
        );

        container.add(
                buttons,
                BorderLayout.SOUTH
        );

        return container;
    }

    private JPanel createTablePanel() {

        JPanel panel =
                new JPanel(new BorderLayout(5, 5));

        JPanel searchPanel =
                new JPanel(new BorderLayout(5, 5));

        txtSearch = new JTextField();

        searchPanel.add(
                new JLabel("Tìm kiếm:"),
                BorderLayout.WEST
        );

        searchPanel.add(
                txtSearch,
                BorderLayout.CENTER
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

        table = new JTable(tableModel);

        table.setRowHeight(25);

        sorter =
                new TableRowSorter<>(tableModel);

        table.setRowSorter(sorter);

        table.getSelectionModel()
                .addListSelectionListener(
                        e -> {

                            if (!e.getValueIsAdjusting()) {
                                fillFormFromTable();
                            }
                        }
                );

        txtSearch.getDocument()
                .addDocumentListener(
                        new DocumentListener() {

                            @Override
                            public void insertUpdate(
                                    DocumentEvent e
                            ) {
                                filterTable();
                            }

                            @Override
                            public void removeUpdate(
                                    DocumentEvent e
                            ) {
                                filterTable();
                            }

                            @Override
                            public void changedUpdate(
                                    DocumentEvent e
                            ) {
                                filterTable();
                            }
                        }
                );

        panel.add(
                searchPanel,
                BorderLayout.NORTH
        );

        panel.add(
                new JScrollPane(table),
                BorderLayout.CENTER
        );

        return panel;
    }

    private void loadCourts() {

        try {

            List<Court> courts =
                    courtService.listCourts();

            tableModel.setRowCount(0);

            for (Court court : courts) {

                tableModel.addRow(
                        new Object[]{
                                court.id(),
                                court.name(),
                                court.pricePerHour(),
                                court.active()
                                        ? "Hoạt động"
                                        : "Ngừng hoạt động"
                        }
                );
            }

        } catch (RuntimeException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void addCourt() {

        try {

            String name =
                    txtName.getText().trim();

            BigDecimal price =
                    new BigDecimal(
                            txtPrice.getText().trim()
                    );

            boolean active =
                    chkActive.isSelected();

            courtService.addCourt(
                    name,
                    price,
                    active
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Thêm sân thành công."
            );

            clearForm();
            loadCourts();

        } catch (NumberFormatException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Giá sân phải là số.",
                    "Dữ liệu không hợp lệ",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (RuntimeException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void updateCourt() {

        if (selectedCourtId == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Hãy chọn sân cần sửa."
            );

            return;
        }

        try {

            String name =
                    txtName.getText().trim();

            BigDecimal price =
                    new BigDecimal(
                            txtPrice.getText().trim()
                    );

            Court court = new Court(
                    selectedCourtId,
                    name,
                    price,
                    chkActive.isSelected()
            );

            boolean success =
                    courtService.updateCourt(court);

            if (success) {

                JOptionPane.showMessageDialog(
                        this,
                        "Cập nhật sân thành công."
                );

                clearForm();
                loadCourts();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Không tìm thấy sân."
                );
            }

        } catch (NumberFormatException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Giá sân phải là số.",
                    "Dữ liệu không hợp lệ",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (RuntimeException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void deleteCourt() {

        if (selectedCourtId == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Hãy chọn sân cần ngừng hoạt động."
            );

            return;
        }

        int choice =
                JOptionPane.showConfirmDialog(
                        this,
                        "Ngừng hoạt động sân này?",
                        "Xác nhận",
                        JOptionPane.YES_NO_OPTION
                );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            boolean success =
                    courtService.deleteCourt(
                            selectedCourtId
                    );

            if (success) {

                JOptionPane.showMessageDialog(
                        this,
                        "Đã ngừng hoạt động sân."
                );

                clearForm();
                loadCourts();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Không tìm thấy sân."
                );
            }

        } catch (RuntimeException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void fillFormFromTable() {

        int selectedRow =
                table.getSelectedRow();

        if (selectedRow == -1) {
            return;
        }

        int modelRow =
                table.convertRowIndexToModel(
                        selectedRow
                );

        selectedCourtId =
                ((Number) tableModel.getValueAt(
                        modelRow,
                        0
                )).longValue();

        txtName.setText(
                tableModel.getValueAt(
                        modelRow,
                        1
                ).toString()
        );

        txtPrice.setText(
                tableModel.getValueAt(
                        modelRow,
                        2
                ).toString()
        );

        String status =
                tableModel.getValueAt(
                        modelRow,
                        3
                ).toString();

        chkActive.setSelected(
                status.equals("Hoạt động")
        );
    }

    private void clearForm() {

        selectedCourtId = -1;

        txtName.setText("");
        txtPrice.setText("");

        chkActive.setSelected(true);

        table.clearSelection();
    }

    private void filterTable() {

        String keyword =
                txtSearch.getText().trim();

        if (keyword.isEmpty()) {

            sorter.setRowFilter(null);

        } else {

            sorter.setRowFilter(
                    RowFilter.regexFilter(
                            "(?i)" +
                            java.util.regex.Pattern.quote(
                                    keyword
                            )
                    )
            );
        }
    }
}