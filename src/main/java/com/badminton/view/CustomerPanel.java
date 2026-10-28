package com.badminton.view;

import com.badminton.dao.CustomerManagementDAO.BookingHistory;
import com.badminton.dao.CustomerManagementDAO.Summary;
import com.badminton.model.User;
import com.badminton.service.CustomerService;
import com.badminton.util.AppFonts;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

/** TH - MINTON / Customer Management / design 2: statistic cards + customer grid. */
public final class CustomerPanel extends JPanel {
    private static final Color BG = new Color(240, 248, 245);
    private static final Color TEXT = new Color(9, 52, 48);
    private static final Color MUTED = new Color(104, 133, 128);
    private static final Color MINT = new Color(72, 222, 190);
    private static final Color DARK = new Color(7, 56, 50);
    private static final Color BORDER = new Color(211, 231, 225);
    private static final Color RED = new Color(185, 55, 65);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HOUR = DateTimeFormatter.ofPattern("HH:mm");

    private final CustomerService service;
    private final CustomerTableModel model = new CustomerTableModel();
    private final JTable table = new JTable(model);
    private final TableRowSorter<CustomerTableModel> sorter = new TableRowSorter<>(model);
    private final JTextField search = new HintField("Tìm theo tên, SĐT hoặc email...");
    private final JComboBox<String> kind = new JComboBox<>(new String[]{
            "Tất cả khách", "Có tài khoản", "Khách"});
    private final JButton refresh = button("↻  Làm mới", Color.WHITE, DARK);
    private final JButton add = button("+  Thêm khách hàng", MINT, DARK);
    private final JLabel totalValue = statValue("0");
    private final JLabel accountValue = statValue("0");
    private final JLabel guestValue = statValue("0");
    private final JLabel revenueValue = new JLabel("Đã thanh toán: 0 đ");
    private final JLabel count = new JLabel("Đang tải...");
    private final JLabel message = new JLabel(" ");
    private boolean loading;
    private boolean loaded;

    public CustomerPanel(User currentUser) {
        service = new CustomerService(currentUser); // Only ADMIN can open this screen.
        setLayout(new BorderLayout(0, 14));
        setBackground(BG);
        setBorder(new EmptyBorder(22, 24, 22, 24));

        JPanel upper = new JPanel(new BorderLayout(0, 13));
        upper.setOpaque(false);
        upper.add(createHero(), BorderLayout.NORTH);
        upper.add(createToolbar(), BorderLayout.CENTER);
        add(upper, BorderLayout.NORTH);
        add(createTableCard(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        model.setRows(List.of());
        configureTable();

        search.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateFilter(); }
            public void removeUpdate(DocumentEvent e) { updateFilter(); }
            public void changedUpdate(DocumentEvent e) { updateFilter(); }
        });
        kind.addActionListener(e -> updateFilter());
        refresh.addActionListener(e -> reload());
        add.addActionListener(e -> showEditor(null));

        // Reload when the admin opens the tab; never block the Swing event thread.
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0
                    && isShowing() && !loading) {
                reload();
            }
        });
    }

    private JPanel createHero() {
        JPanel hero = new JPanel(new BorderLayout(20, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D p = (Graphics2D) g.create();
                p.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                p.setPaint(new GradientPaint(0, 0, new Color(10, 122, 107),
                        getWidth(), getHeight(), new Color(120, 235, 207)));
                p.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                p.setColor(new Color(255, 255, 255, 22));
                p.fillOval(getWidth() / 4, -105, 320, 230);
                p.dispose();
                super.paintComponent(g);
            }
        };
        hero.setOpaque(false);
        hero.setBorder(new EmptyBorder(22, 23, 22, 23));
        hero.setPreferredSize(new Dimension(700, 153));
        hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 153));

        JPanel intro = new JPanel();
        intro.setOpaque(false);
        intro.setLayout(new BoxLayout(intro, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Quản lý khách hàng");
        title.setFont(AppFonts.bold(23f));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel caption = new JLabel("Hồ sơ, lịch đặt sân và thanh toán");
        caption.setFont(AppFonts.regular(12.5f));
        caption.setForeground(new Color(232, 255, 246));
        caption.setAlignmentX(Component.LEFT_ALIGNMENT);
        revenueValue.setFont(AppFonts.bold(13f));
        revenueValue.setForeground(Color.WHITE);
        revenueValue.setAlignmentX(Component.LEFT_ALIGNMENT);
        intro.add(Box.createVerticalGlue());
        intro.add(title);
        intro.add(Box.createVerticalStrut(5));
        intro.add(caption);
        intro.add(Box.createVerticalStrut(13));
        intro.add(revenueValue);
        intro.add(Box.createVerticalGlue());
        hero.add(intro, BorderLayout.CENTER);

        JPanel stats = new JPanel(new GridLayout(1, 3, 9, 0));
        stats.setOpaque(false);
        stats.add(statCard("Tổng khách hàng", totalValue));
        stats.add(statCard("Có tài khoản", accountValue));
        stats.add(statCard("Khách", guestValue));
        stats.setPreferredSize(new Dimension(414, 103));
        hero.add(stats, BorderLayout.EAST);
        return hero;
    }

    private JPanel statCard(String title, JLabel number) {
        JPanel card = roundCard(Color.WHITE, 18);
        card.setLayout(new BorderLayout(0, 7));
        card.setBorder(new EmptyBorder(15, 13, 13, 12));
        JLabel label = new JLabel(title);
        label.setFont(AppFonts.medium(11f));
        label.setForeground(MUTED);
        card.add(label, BorderLayout.NORTH);
        card.add(number, BorderLayout.CENTER);
        return card;
    }

    private static JLabel statValue(String value) {
        JLabel label = new JLabel(value);
        label.setFont(AppFonts.bold(25f));
        label.setForeground(DARK);
        return label;
    }

    private JPanel createToolbar() {
        JPanel bar = roundCard(Color.WHITE, 16);
        bar.setLayout(new BorderLayout(10, 0));
        bar.setBorder(new EmptyBorder(12, 14, 12, 14));
        bar.setPreferredSize(new Dimension(500, 67));

        search.setFont(AppFonts.regular(13f));
        search.setToolTipText("Tìm theo họ tên, số điện thoại hoặc email");
        search.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(7, 12, 7, 9)));
        search.setPreferredSize(new Dimension(220, 38));
        bar.add(search, BorderLayout.CENTER);

        JPanel options = new JPanel(new FlowLayout(FlowLayout.RIGHT, 9, 0));
        options.setOpaque(false);
        kind.setFont(AppFonts.regular(12.5f));
        kind.setPreferredSize(new Dimension(133, 38));
        kind.setBackground(Color.WHITE);
        options.add(kind);
        options.add(refresh);
        options.add(add);
        bar.add(options, BorderLayout.EAST);
        return bar;
    }

    private JPanel createTableCard() {
        JPanel card = roundCard(Color.WHITE, 16);
        card.setLayout(new BorderLayout(0, 0));
        card.setBorder(new EmptyBorder(8, 10, 8, 10));

        JScrollPane pane = new JScrollPane(table,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pane.setBorder(null);
        pane.getViewport().setBackground(Color.WHITE);
        pane.getVerticalScrollBar().setUnitIncrement(18);
        card.add(pane, BorderLayout.CENTER);
        card.setPreferredSize(new Dimension(750, 340));
        return card;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        count.setForeground(DARK);
        count.setFont(AppFonts.medium(12f));
        message.setForeground(MUTED);
        message.setFont(AppFonts.regular(11.5f));
        footer.add(count, BorderLayout.WEST);
        footer.add(message, BorderLayout.EAST);
        return footer;
    }

    private void configureTable() {
        table.setRowHeight(52);
        table.setFont(AppFonts.regular(12.5f));
        table.setGridColor(new Color(233, 242, 237));
        table.setShowVerticalLines(false);
        table.setSelectionBackground(new Color(228, 250, 242));
        table.setSelectionForeground(TEXT);
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setBackground(new Color(232, 246, 241));
        table.getTableHeader().setForeground(TEXT);
        table.getTableHeader().setFont(AppFonts.bold(11.5f));
        table.getTableHeader().setPreferredSize(new Dimension(0, 43));

        int[] widths = {56, 192, 123, 183, 109, 74, 130, 182};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        DefaultTableCellRenderer normal = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object value,
                            boolean selected, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, value, selected, focus, row, col);
                setBorder(new EmptyBorder(0, 9, 0, 7));
                setFont(AppFonts.regular(12f));
                setForeground(TEXT);
                if (!selected) setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 253, 251));
                return this;
            }
        };
        for (int c = 0; c <= 6; c++) table.getColumnModel().getColumn(c).setCellRenderer(normal);
        table.getColumnModel().getColumn(4).setCellRenderer(new TypeBadgeRenderer());
        table.getColumnModel().getColumn(6).setCellRenderer(new MoneyRenderer());
        table.getColumnModel().getColumn(7).setCellRenderer(new ActionRenderer());
        table.getColumnModel().getColumn(7).setCellEditor(new ActionEditor());
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.rowAtPoint(e.getPoint()) >= 0
                        && table.columnAtPoint(e.getPoint()) != 7) {
                    Summary row = atViewRow(table.rowAtPoint(e.getPoint()));
                    if (row != null) viewBookings(row);
                }
            }
        });
    }

    private void reload() {
        if (loading) return;
        loading = true;
        refresh.setEnabled(false);
        add.setEnabled(false);
        message.setText("Đang đọc MySQL...");
        new SwingWorker<List<Summary>, Void>() {
            @Override protected List<Summary> doInBackground() throws Exception {
                return service.listSummaries();
            }
            @Override protected void done() {
                loading = false;
                refresh.setEnabled(true);
                add.setEnabled(true);
                try {
                    List<Summary> rows = get();
                    model.setRows(rows);
                    int linked = 0;
                    BigDecimal paid = BigDecimal.ZERO;
                    for (Summary row : rows) {
                        if (row.hasAccount()) linked++;
                        paid = paid.add(row.paidTotal());
                    }
                    totalValue.setText(String.valueOf(rows.size()));
                    accountValue.setText(String.valueOf(linked));
                    guestValue.setText(String.valueOf(rows.size() - linked));
                    revenueValue.setText("Đã thanh toán: " + money(paid));
                    loaded = true;
                    message.setText("Dữ liệu trực tiếp từ MySQL");
                    updateFilter();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    showError("Đã gián đoạn tải dữ liệu.");
                } catch (ExecutionException ex) {
                    loaded = false;
                    message.setText("Không tải được dữ liệu");
                    showError("Không truy cập được database: " + readable(ex));
                }
            }
        }.execute();
    }

    private void updateFilter() {
        String term = search.getText().strip().toLowerCase(Locale.ROOT);
        int which = kind.getSelectedIndex();
        sorter.setRowFilter(new RowFilter<>() {
            @Override public boolean include(Entry<? extends CustomerTableModel, ? extends Integer> e) {
                Summary r = model.row(e.getIdentifier());
                if (which == 1 && !r.hasAccount()) return false;
                if (which == 2 && r.hasAccount()) return false;
                return (r.fullName() + " " + r.phone() + " "
                        + (r.email() == null ? "" : r.email()) + " " + r.id())
                        .toLowerCase(Locale.ROOT).contains(term);
            }
        });
        count.setText("Hiển thị " + table.getRowCount() + " / "
                + model.getRowCount() + " khách hàng");
    }

    private Summary atViewRow(int viewRow) {
        if (viewRow < 0 || viewRow >= table.getRowCount()) return null;
        return model.row(table.convertRowIndexToModel(viewRow));
    }

    private void showEditor(Summary selected) {
        JTextField name = field(selected == null ? "" : selected.fullName());
        JTextField phone = field(selected == null ? "" : selected.phone());
        JTextField email = field(selected == null || selected.email() == null ? "" : selected.email());
        JPanel form = new JPanel(new GridLayout(6, 1, 0, 7));
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(8, 10, 8, 10));
        form.add(formLabel("Họ và tên *")); form.add(name);
        form.add(formLabel("Số điện thoại *")); form.add(phone);
        form.add(formLabel("Email (không bắt buộc)")); form.add(email);

        String title = selected == null ? "Thêm khách hàng" : "Chỉnh sửa khách hàng #" + selected.id();
        int answer = JOptionPane.showConfirmDialog(this, form, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) return;
        String newName = name.getText(), newPhone = phone.getText(), newEmail = email.getText();
        runMutation(selected == null ? "Đã thêm khách hàng." : "Đã cập nhật khách hàng.", () -> {
            if (selected == null) {
                service.addCustomer(newName, newPhone, newEmail);
            } else if (!service.updateCustomer(selected.id(), newName, newPhone, newEmail)) {
                throw new IllegalStateException("Khách hàng không còn tồn tại.");
            }
        });
    }

    private void delete(Summary customer) {
        if (customer.hasAccount() || customer.bookingCount() > 0) {
            JOptionPane.showMessageDialog(this,
                    "Không thể xóa khách có tài khoản hoặc đã từng đặt sân.\n"
                    + "Bạn vẫn có thể sửa hồ sơ và xem lịch sử đặt.",
                    "Không được phép xóa", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this,
                "Xóa khách '" + customer.fullName() + "'?\nThao tác này không thể hoàn tác.",
                "Xác nhận xóa khách hàng", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) return;
        runMutation("Đã xóa khách hàng.", () -> {
            if (!service.deleteCustomer(customer.id())) {
                throw new IllegalStateException(
                        "Không thể xóa. Khách có tài khoản, lịch đặt sân hoặc đã bị xóa.");
            }
        });
    }

    private interface DatabaseAction { void execute() throws Exception; }

    private void runMutation(String success, DatabaseAction action) {
        if (loading) return;
        loading = true;
        add.setEnabled(false);
        refresh.setEnabled(false);
        message.setText("Đang lưu vào MySQL...");
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                action.execute();
                return null;
            }
            @Override protected void done() {
                loading = false;
                add.setEnabled(true);
                refresh.setEnabled(true);
                try {
                    get();
                    message.setText(success);
                    reload();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    showError("Thao tác bị gián đoạn.");
                } catch (ExecutionException ex) {
                    message.setText("Chưa lưu thay đổi");
                    showError(readable(ex));
                }
            }
        }.execute();
    }

    private void viewBookings(Summary customer) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, "Lịch sử đặt sân - " + customer.fullName(),
                Dialog.ModalityType.MODELESS);
        JPanel root = new JPanel(new BorderLayout(0, 13));
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(19, 20, 20, 20));
        JLabel head = new JLabel("Lịch sử đặt sân · " + customer.fullName());
        head.setFont(AppFonts.bold(19f));
        head.setForeground(TEXT);
        JLabel meta = new JLabel("Đang đọc dữ liệu...");
        meta.setFont(AppFonts.medium(12f));
        meta.setForeground(MUTED);
        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.setOpaque(false);
        top.add(head, BorderLayout.NORTH);
        top.add(meta, BorderLayout.SOUTH);
        root.add(top, BorderLayout.NORTH);

        String[] cols = {"Mã đặt", "Sân", "Ngày", "Giờ", "Trạng thái",
                "Giá/giờ hiện tại", "Ước tính hiện tại", "Đã thanh toán", "Phương thức"};
        javax.swing.table.DefaultTableModel historyModel = new javax.swing.table.DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable historyTable = new JTable(historyModel);
        historyTable.setFont(AppFonts.regular(12f));
        historyTable.getTableHeader().setFont(AppFonts.bold(11.5f));
        historyTable.getTableHeader().setBackground(new Color(230, 246, 241));
        historyTable.setRowHeight(38);
        historyTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] widths = {82, 125, 90, 112, 110, 125, 135, 125, 116};
        for (int i = 0; i < widths.length; i++)
            historyTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        JScrollPane scroll = new JScrollPane(historyTable);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        root.add(scroll, BorderLayout.CENTER);

        JLabel note = new JLabel("Ước tính dựa trên GIÁ SÂN HIỆN TẠI; số tiền đã thu lấy từ payments.");
        note.setFont(AppFonts.regular(12f));
        note.setForeground(MUTED);
        root.add(note, BorderLayout.SOUTH);
        dialog.setContentPane(root);
        dialog.setSize(990, 540);
        dialog.setMinimumSize(new Dimension(790, 410));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        new SwingWorker<List<BookingHistory>, Void>() {
            @Override protected List<BookingHistory> doInBackground() throws Exception {
                return service.listBookingHistory(customer.id());
            }
            @Override protected void done() {
                if (!dialog.isDisplayable()) return;
                try {
                    List<BookingHistory> bookings = get();
                    BigDecimal actualPaid = BigDecimal.ZERO;
                    for (BookingHistory b : bookings) {
                        if (b.actuallyPaid() != null) actualPaid = actualPaid.add(b.actuallyPaid());
                        historyModel.addRow(new Object[]{
                                b.id(), b.courtName(), b.date().format(DATE),
                                b.start().format(HOUR) + " - " + b.end().format(HOUR),
                                translateBookingStatus(b.status()),
                                money(b.currentHourlyPrice()), money(b.estimateAtCurrentPrice()),
                                b.actuallyPaid() == null ? "Chưa ghi nhận" : money(b.actuallyPaid()),
                                b.paymentMethod() == null ? "—" : b.paymentMethod()});
                    }
                    meta.setText(bookings.size() + " lượt đặt (gồm cả đơn hủy) · Đã thanh toán: "
                            + money(actualPaid));
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    meta.setText("Tải bị gián đoạn");
                } catch (ExecutionException ex) {
                    meta.setText("Không tải được lịch sử");
                    JOptionPane.showMessageDialog(dialog, readable(ex), "Lỗi database",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private static String translateBookingStatus(String status) {
        return switch (status == null ? "" : status.toUpperCase(Locale.ROOT)) {
            case "PENDING" -> "Chờ xác nhận";
            case "CONFIRMED" -> "Đã xác nhận";
            case "CANCELLED" -> "Đã hủy";
            default -> status;
        };
    }

    private static String money(BigDecimal amount) {
        NumberFormat number = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
        number.setMaximumFractionDigits(0);
        number.setMinimumFractionDigits(0);
        return number.format(amount == null ? BigDecimal.ZERO : amount) + " đ";
    }

    private static String readable(Throwable ex) {
        Throwable t = ex;
        while (t.getCause() != null && t instanceof ExecutionException) t = t.getCause();
        String value = t.getMessage();
        return value == null || value.isBlank() ? "Không thể hoàn tất thao tác." : value;
    }

    private void showError(String description) {
        JOptionPane.showMessageDialog(this, description, "TH - MINTON", JOptionPane.ERROR_MESSAGE);
    }

    private static JLabel formLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(AppFonts.medium(12.5f));
        label.setForeground(TEXT);
        return label;
    }

    private static JTextField field(String text) {
        JTextField result = new JTextField(text);
        result.setFont(AppFonts.regular(13.5f));
        result.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(7, 10, 7, 10)));
        return result;
    }

    private static JButton button(String text, Color background, Color foreground) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D p = (Graphics2D) g.create();
                p.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                int inset = getModel().isPressed() ? 3 : getModel().isRollover() ? 2 : 0;
                Color fill = getModel().isRollover() && background.equals(MINT)
                        ? new Color(100, 233, 202) : background;
                p.setColor(fill);
                p.fillRoundRect(inset, inset, getWidth() - 2 * inset,
                        getHeight() - 2 * inset, 14, 14);
                if (background.equals(Color.WHITE)) {
                    p.setColor(BORDER);
                    p.drawRoundRect(inset, inset, getWidth() - 2 * inset - 1,
                            getHeight() - 2 * inset - 1, 14, 14);
                }
                p.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(AppFonts.bold(12f));
        b.setForeground(foreground);
        b.setBackground(background);
        b.setRolloverEnabled(true);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(9, 13, 9, 13));
        b.setPreferredSize(new Dimension(text.startsWith("+") ? 166 : 104, 38));
        return b;
    }

    private static final class HintField extends JTextField {
        private final String hint;
        HintField(String hint) { this.hint = hint; }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (!getText().isEmpty() || isFocusOwner()) return;
            Graphics2D p = (Graphics2D) g.create();
            p.setColor(MUTED);
            p.setFont(AppFonts.regular(12f));
            int y = (getHeight() + p.getFontMetrics().getAscent()
                    - p.getFontMetrics().getDescent()) / 2;
            p.drawString(hint, 12, y);
            p.dispose();
        }
    }

    private static JPanel roundCard(Color color, int radius) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D p = (Graphics2D) g.create();
                p.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                p.setColor(color);
                p.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
                p.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        return card;
    }

    private final class CustomerTableModel extends AbstractTableModel {
        private final String[] columns = {"ID", "Họ và tên", "Số điện thoại", "Email",
                "Loại khách", "Lượt đặt", "Đã thanh toán", "Thao tác"};
        private List<Summary> rows = List.of();
        @Override public int getColumnCount() { return columns.length; }
        @Override public int getRowCount() { return rows.size(); }
        @Override public String getColumnName(int column) { return columns[column]; }
        @Override public boolean isCellEditable(int row, int column) { return column == 7; }
        @Override public Class<?> getColumnClass(int column) {
            return switch (column) {
                case 0, 5 -> Long.class;
                case 6 -> BigDecimal.class;
                default -> String.class;
            };
        }
        public Summary row(int index) { return rows.get(index); }
        void setRows(List<Summary> list) {
            rows = new ArrayList<>(list);
            fireTableDataChanged();
        }
        @Override public Object getValueAt(int rowIndex, int columnIndex) {
            Summary r = rows.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> r.id();
                case 1 -> r.fullName();
                case 2 -> r.phone();
                case 3 -> r.email() == null ? "—" : r.email();
                case 4 -> r.hasAccount() ? "Có tài khoản" : "Khách";
                case 5 -> r.bookingCount();
                case 6 -> r.paidTotal();
                default -> "";
            };
        }
    }

    private static final class TypeBadgeRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable table, Object value,
                   boolean selected, boolean focus, int row, int column) {
            JPanel wrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 13));
            if (!selected) wrap.setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 253, 251));
            else wrap.setBackground(table.getSelectionBackground());
            JLabel badge = new JLabel(String.valueOf(value));
            badge.setOpaque(true);
            badge.setFont(AppFonts.medium(11f));
            badge.setBorder(new EmptyBorder(5, 9, 5, 9));
            boolean account = "Có tài khoản".equals(value);
            badge.setBackground(account ? new Color(218, 249, 231) : new Color(237, 243, 245));
            badge.setForeground(account ? new Color(4, 125, 80) : MUTED);
            wrap.add(badge);
            return wrap;
        }
    }

    private static final class MoneyRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable table, Object value,
                   boolean selected, boolean focus, int row, int column) {
            super.getTableCellRendererComponent(table, money((BigDecimal)value), selected, focus, row, column);
            setHorizontalAlignment(SwingConstants.RIGHT);
            setBorder(new EmptyBorder(0, 6, 0, 10));
            setFont(AppFonts.bold(11.5f));
            setForeground(DARK);
            if (!selected) setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 253, 251));
            return this;
        }
    }

    // Action buttons with vector icons; no extra image files or libraries needed.
    // The button name remains Xem/Sửa/Xóa, so the existing database actions are preserved.
    private static JPanel actionsPanel(boolean editMode) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 7, 8));
        panel.setOpaque(false);
        for (String action : new String[]{"Xem", "Sửa", "Xóa"}) {
            ActionIconButton button = new ActionIconButton(action);
            if (editMode) {
                button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
            panel.add(button);
        }
        return panel;
    }

    private static final class ActionIconButton extends JButton {
        private final String action;
        private final Color ink;
        private final Color normal;
        private final Color rollover;

        ActionIconButton(String action) {
            super();
            this.action = action;
            setName(action); // ActionEditor uses this to dispatch the original action.
            switch (action) {
                case "Xem" -> {
                    ink = new Color(13, 105, 97);
                    normal = new Color(224, 247, 242);
                    rollover = new Color(184, 239, 228);
                    setToolTipText("Xem lịch sử đặt sân và thanh toán");
                }
                case "Sửa" -> {
                    ink = new Color(7, 126, 105);
                    normal = new Color(218, 249, 235);
                    rollover = new Color(165, 237, 205);
                    setToolTipText("Chỉnh sửa thông tin khách hàng");
                }
                default -> {
                    ink = new Color(192, 60, 72);
                    normal = new Color(255, 234, 236);
                    rollover = new Color(255, 203, 210);
                    setToolTipText("Xóa khách hàng (khi được phép)");
                }
            }
            getAccessibleContext().setAccessibleName(action + " khách hàng");
            setPreferredSize(new Dimension(36, 36));
            setMinimumSize(new Dimension(36, 36));
            setMaximumSize(new Dimension(36, 36));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setBorder(new EmptyBorder(0, 0, 0, 0));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D p = (Graphics2D) g.create();
            p.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            boolean hovered = getModel().isRollover();
            boolean pressed = getModel().isPressed();
            int inset = pressed ? 4 : hovered ? 2 : 1;
            int w = Math.max(1, getWidth() - inset * 2);
            int h = Math.max(1, getHeight() - inset * 2);
            p.setColor(hovered ? rollover : normal);
            p.fillRoundRect(inset, inset, w, h, 11, 11);
            if (hovered || isFocusOwner()) {
                p.setColor(new Color(ink.getRed(), ink.getGreen(), ink.getBlue(), 85));
                p.setStroke(new BasicStroke(1.1f));
                p.drawRoundRect(inset, inset, w - 1, h - 1, 11, 11);
            }

            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            p.setColor(ink);
            p.setStroke(new BasicStroke(1.9f, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND));
            switch (action) {
                case "Xem" -> {
                    java.awt.geom.Path2D eye = new java.awt.geom.Path2D.Float();
                    eye.moveTo(cx - 10, cy);
                    eye.curveTo(cx - 5, cy - 7, cx + 5, cy - 7, cx + 10, cy);
                    eye.curveTo(cx + 5, cy + 7, cx - 5, cy + 7, cx - 10, cy);
                    p.draw(eye);
                    p.drawOval(cx - 2, cy - 2, 4, 4);
                }
                case "Sửa" -> {
                    java.awt.geom.Path2D pencil = new java.awt.geom.Path2D.Float();
                    pencil.moveTo(cx - 7, cy + 5);
                    pencil.lineTo(cx + 4, cy - 6);
                    pencil.lineTo(cx + 8, cy - 2);
                    pencil.lineTo(cx - 3, cy + 9);
                    pencil.lineTo(cx - 8, cy + 10);
                    pencil.closePath();
                    p.draw(pencil);
                    p.drawLine(cx + 2, cy - 4, cx + 6, cy);
                }
                case "Xóa" -> {
                    p.drawLine(cx - 8, cy - 6, cx + 8, cy - 6);
                    p.drawLine(cx - 3, cy - 9, cx + 3, cy - 9);
                    p.drawRoundRect(cx - 6, cy - 4, 12, 14, 2, 2);
                    p.drawLine(cx - 2, cy - 1, cx - 2, cy + 6);
                    p.drawLine(cx + 2, cy - 1, cx + 2, cy + 6);
                }
                default -> { }
            }
            p.dispose();
        }
    }

    private static class ActionRenderer implements TableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable table, Object value,
                         boolean selected, boolean focus, int row, int col) {
            JPanel p = actionsPanel(false);
            p.setBackground(selected ? table.getSelectionBackground() : Color.WHITE);
            p.setOpaque(true);
            return p;
        }
    }

    private final class ActionEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel = actionsPanel(true);
        private int editingRow = -1;
        ActionEditor() {
            for (Component component : panel.getComponents()) {
                JButton b = (JButton) component;
                b.addActionListener(e -> {
                    int row = editingRow;
                    fireEditingStopped();
                    Summary selected = atViewRow(row);
                    if (selected == null) return;
                    SwingUtilities.invokeLater(() -> {
                        if (loading) return;
                        switch (b.getName()) {
                            case "Xem" -> viewBookings(selected);
                            case "Sửa" -> showEditor(selected);
                            case "Xóa" -> delete(selected);
                            default -> { }
                        }
                    });
                });
            }
        }
        @Override public Object getCellEditorValue() { return ""; }
        @Override public Component getTableCellEditorComponent(JTable table, Object value,
                         boolean selected, int row, int column) {
            editingRow = row;
            panel.setBackground(table.getSelectionBackground());
            panel.setOpaque(true);
            return panel;
        }
    }
}
