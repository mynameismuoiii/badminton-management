package com.badminton.view;

import com.badminton.model.Court;
import com.badminton.service.CourtService;
import com.badminton.ui.MintDialogs;
import com.badminton.util.AppFonts;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

/** Giao dien quan ly san - mau 3: bo goc, icon Java2D, bang gon gang. */
public class CourtPanel extends JPanel {
    private static final Color BG = new Color(243, 248, 247);
    private static final Color WHITE = Color.WHITE;
    private static final Color TEXT = new Color(22, 54, 50);
    private static final Color MUTED = new Color(112, 132, 130);
    private static final Color TEAL = new Color(19, 156, 138);
    private static final Color BORDER = new Color(222, 236, 231);
    private static final Color PALE = new Color(230, 249, 243);
    private static final Color BLUE = new Color(43, 111, 198);
    private static final Color ORANGE = new Color(177, 116, 35);
    private static final Color RED = new Color(190, 65, 71);
    private static final Color LINE = new Color(237, 244, 241);
    private static final Locale VI = new Locale("vi", "VN");

    private final CourtService courtService = new CourtService();
    private final List<Court> courts = new ArrayList<>();
    private final CourtTableModel model = new CourtTableModel();
    private final JTextField search = new HintField("Tìm kiếm tên sân, mã sân...");
    private final JComboBox<String> statusFilter = new RoundedCombo(new String[]{
            "Tất cả trạng thái", "Đang hoạt động", "Ngừng hoạt động"
    });
    private final JComboBox<String> sortFilter = new RoundedCombo(new String[]{
            "Mặc định (ID)", "Giá: thấp -> cao", "Giá: cao -> thấp", "Tên sân: A -> Z"
    });
    private final JLabel resultLabel = new JLabel("Đang tải danh sách sân...");
    private final JTable table = new JTable(model);
    private final JButton refreshButton = new PillButton("Làm mới", PALE, TEAL);
    private final JScrollPane tableScroll = new JScrollPane(table);
    private SwingWorker<List<Court>, Void> loader;

    public CourtPanel() {
        super(new BorderLayout());
        setBackground(BG);
        setBorder(new EmptyBorder(21, 25, 21, 25));

        JPanel content = clear(new BorderLayout(0, 17));
        content.add(header(), BorderLayout.NORTH);
        JPanel anchored = clear(new BorderLayout());
        anchored.add(tableCard(), BorderLayout.NORTH);
        content.add(anchored, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
        MintDropdown.install(statusFilter);
        MintDropdown.install(sortFilter);

        search.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });
        statusFilter.addActionListener(e -> applyFilters());
        sortFilter.addActionListener(e -> applyFilters());
        refreshButton.addActionListener(e -> loadCourts());
        loadCourts();
    }

    private JPanel header() {
        JPanel bar = clear(new BorderLayout(15, 0));
        bar.setPreferredSize(new Dimension(600, 65));

        JPanel left = clear(new BorderLayout(13, 0));
        JComponent symbol = new CourtSymbol();
        symbol.setPreferredSize(new Dimension(49, 49));
        left.add(symbol, BorderLayout.WEST);

        JPanel texts = clear(new GridLayout(2, 1, 0, 1));
        JLabel title = new JLabel("Quản lý sân");
        title.setFont(AppFonts.bold(23f));
        title.setForeground(TEXT);
        JLabel subtitle = new JLabel("Quản lý thông tin, giá thuê và trạng thái sân cầu lông");
        subtitle.setFont(AppFonts.regular(12f));
        subtitle.setForeground(MUTED);
        texts.add(title);
        texts.add(subtitle);
        left.add(texts, BorderLayout.CENTER);

        JButton add = new PillButton("+  Thêm sân", TEAL, WHITE);
        add.setPreferredSize(new Dimension(135, 37));
        add.addActionListener(e -> openEditor(null));
        JPanel right = clear(new FlowLayout(FlowLayout.RIGHT, 0, 13));
        right.add(add); // FlowLayout giu dung chieu cao, khong bi keo gian theo header.
        bar.add(left, BorderLayout.CENTER);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel tableCard() {
        RoundedCard card = new RoundedCard();
        card.setLayout(new BorderLayout(0, 13));
        card.setBorder(new EmptyBorder(17, 17, 16, 17));

        JPanel filters = clear(new BorderLayout(12, 0));
        RoundedSearch searchBox = new RoundedSearch(search);
        searchBox.setPreferredSize(new Dimension(200, 39));
        filters.add(searchBox, BorderLayout.CENTER);

        JPanel controls = clear(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        statusFilter.setPreferredSize(new Dimension(170, 39));
        sortFilter.setPreferredSize(new Dimension(172, 39));
        refreshButton.setPreferredSize(new Dimension(100, 39));
        controls.add(statusFilter);
        controls.add(sortFilter);
        controls.add(refreshButton);
        filters.add(controls, BorderLayout.EAST);
        card.add(filters, BorderLayout.NORTH);

        table.setRowHeight(76);
        table.setFont(AppFonts.medium(12f));
        table.setForeground(TEXT);
        table.setBackground(WHITE);
        table.setSelectionBackground(new Color(232, 249, 242));
        table.setSelectionForeground(TEXT);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setRowSelectionAllowed(true);
        table.getTableHeader().setFont(AppFonts.bold(12f));
        table.getTableHeader().setBackground(new Color(234, 247, 243));
        table.getTableHeader().setForeground(TEXT);
        table.getTableHeader().setPreferredSize(new Dimension(0, 47));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);
        table.getTableHeader().setDefaultRenderer(new HeaderRenderer());
        table.getColumnModel().getColumn(0).setPreferredWidth(54);
        table.getColumnModel().getColumn(0).setMinWidth(45);
        table.getColumnModel().getColumn(0).setMaxWidth(65);
        table.getColumnModel().getColumn(1).setPreferredWidth(245);
        table.getColumnModel().getColumn(2).setPreferredWidth(135);
        table.getColumnModel().getColumn(3).setPreferredWidth(155);
        table.getColumnModel().getColumn(4).setPreferredWidth(224);
        table.setDefaultRenderer(Object.class, new IdRenderer());
        table.getColumnModel().getColumn(1).setCellRenderer(new CourtRenderer());
        table.getColumnModel().getColumn(2).setCellRenderer(new PriceRenderer());
        table.getColumnModel().getColumn(3).setCellRenderer(new StatusRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new ActionsRenderer());

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row < 0 || table.columnAtPoint(e.getPoint()) != 4) return;
                Court court = model.getCourt(row);
                Rectangle r = table.getCellRect(row, 4, true);
                int part = Math.min(2, Math.max(0, (e.getX() - r.x) * 3 / Math.max(1, r.width)));
                if (part == 0) showDetails(court);
                if (part == 1) openEditor(court);
                if (part == 2) changeStatus(court);
            }
        });
        table.addMouseMotionListener(new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                table.setCursor(Cursor.getPredefinedCursor(
                        table.columnAtPoint(e.getPoint()) == 4 ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        });

        tableScroll.setBorder(null);
        tableScroll.setOpaque(false);
        tableScroll.getViewport().setBackground(WHITE);
        tableScroll.getVerticalScrollBar().setUnitIncrement(17);
        tableScroll.setPreferredSize(new Dimension(720, 47 + 76 * 3));
        RoundedTable rounded = new RoundedTable();
        rounded.setLayout(new BorderLayout());
        rounded.add(tableScroll, BorderLayout.CENTER);
        rounded.setPreferredSize(new Dimension(720, 47 + 76 * 3));
        card.add(rounded, BorderLayout.CENTER);

        resultLabel.setFont(AppFonts.regular(12f));
        resultLabel.setForeground(MUTED);
        resultLabel.setBorder(new EmptyBorder(3, 3, 0, 0));
        card.add(resultLabel, BorderLayout.SOUTH);
        return card;
    }

    private void loadCourts() {
        if (loader != null && !loader.isDone()) return;
        refreshButton.setEnabled(false);
        resultLabel.setText("Đang tải danh sách sân từ MySQL...");
        loader = new SwingWorker<>() {
            @Override protected List<Court> doInBackground() { return courtService.listCourts(); }
            @Override protected void done() {
                refreshButton.setEnabled(true);
                try {
                    courts.clear();
                    courts.addAll(get());
                    applyFilters();
                } catch (Exception ex) {
                    resultLabel.setText("Không thể tải danh sách sân");
                    showError("Không thể tải danh sách sân.", rootCause(ex));
                }
            }
        };
        loader.execute();
    }

    private void applyFilters() {
        String keyword = search.getText().trim().toLowerCase(VI);
        int mode = statusFilter.getSelectedIndex();
        List<Court> filtered = new ArrayList<>();
        for (Court court : courts) {
            if (!keyword.isEmpty() && !court.name().toLowerCase(VI).contains(keyword)
                    && !String.valueOf(court.id()).contains(keyword)) continue;
            if (mode == 1 && !court.active()) continue;
            if (mode == 2 && court.active()) continue;
            filtered.add(court);
        }
        switch (sortFilter.getSelectedIndex()) {
            case 1 -> filtered.sort(Comparator.comparing(Court::pricePerHour));
            case 2 -> filtered.sort(Comparator.comparing(Court::pricePerHour).reversed());
            case 3 -> filtered.sort(Comparator.comparing(Court::name, String.CASE_INSENSITIVE_ORDER));
            default -> filtered.sort(Comparator.comparingLong(Court::id));
        }
        model.setCourts(filtered);
        int rows = Math.max(3, Math.min(6, filtered.size()));
        Dimension size = new Dimension(720, 47 + 76 * rows);
        tableScroll.setPreferredSize(size);
        tableScroll.getParent().setPreferredSize(size);
        tableScroll.getParent().revalidate();
        resultLabel.setText("Hiển thị " + filtered.size() + "/" + courts.size() + " sân  |  "
                + courts.stream().filter(Court::active).count() + " sân đang hoạt động");
        revalidate();
        repaint();
    }

    private void showDetails(Court court) {
        JPanel body = clear(new BorderLayout(0, 12));
        JLabel photo = new JLabel(new CourtPhoto(court.id(), 380, 165));
        photo.setHorizontalAlignment(SwingConstants.CENTER);
        body.add(photo, BorderLayout.NORTH);
        JPanel fields = clear(new GridLayout(3, 1, 0, 9));
        fields.add(infoLine("Tên sân", court.name()));
        fields.add(infoLine("Giá thuê", money(court.pricePerHour()) + " / giờ"));
        fields.add(infoLine("Trạng thái", court.active() ? "Đang hoạt động" : "Ngừng hoạt động"));
        body.add(fields, BorderLayout.CENTER);
        MintDialogs.showMessageDialog(this, body, "Chi tiết sân #" + court.id(), JOptionPane.PLAIN_MESSAGE);
    }

    private JPanel infoLine(String name, String value) {
        JPanel row = clear(new BorderLayout());
        JLabel a = new JLabel(name);
        a.setForeground(MUTED);
        a.setFont(AppFonts.regular(13f));
        JLabel b = new JLabel(value);
        b.setForeground(TEXT);
        b.setFont(AppFonts.bold(13f));
        row.add(a, BorderLayout.WEST);
        row.add(b, BorderLayout.EAST);
        return row;
    }

    private void openEditor(Court court) {
        JTextField name = new JTextField(court == null ? "" : court.name(), 23);
        JTextField price = new JTextField(court == null ? "" :
                court.pricePerHour().stripTrailingZeros().toPlainString(), 23);
        JCheckBox active = new JCheckBox("Sân đang hoạt động", court == null || court.active());
        active.setOpaque(false);
        active.setFont(AppFonts.medium(13f));
        JPanel form = clear(new GridLayout(6, 1, 0, 8));
        form.add(fieldLabel("Tên sân"));
        form.add(name);
        form.add(fieldLabel("Giá thuê / giờ (VNĐ)"));
        form.add(price);
        form.add(fieldLabel("Trạng thái"));
        form.add(active);
        while (true) {
            int answer = MintDialogs.showConfirmDialog(this, form,
                    court == null ? "Thêm sân mới" : "Sửa thông tin sân",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (answer != JOptionPane.OK_OPTION) return;
            String nameValue = name.getText().trim();
            BigDecimal priceValue;
            try {
                if (nameValue.isEmpty()) throw new IllegalArgumentException("Tên sân không được để trống.");
                String raw = price.getText().trim().replace("₫", "").replace("đ", "")
                        .replace(" ", "").replace(".", "").replace(',', '.');
                priceValue = new BigDecimal(raw);
                if (priceValue.compareTo(BigDecimal.ZERO) <= 0)
                    throw new IllegalArgumentException("Giá sân phải lớn hơn 0.");
            } catch (NumberFormatException ex) {
                MintDialogs.showMessageDialog(this, "Giá sân không hợp lệ. Ví dụ: 50000 hoặc 50.000.",
                        "Kiểm tra dữ liệu", JOptionPane.WARNING_MESSAGE);
                continue;
            } catch (IllegalArgumentException ex) {
                MintDialogs.showMessageDialog(this, ex.getMessage(),
                        "Kiểm tra dữ liệu", JOptionPane.WARNING_MESSAGE);
                continue;
            }
            saveCourt(court, nameValue, priceValue, active.isSelected());
            return;
        }
    }

    private static JLabel fieldLabel(String value) {
        JLabel label = new JLabel(value);
        label.setFont(AppFonts.medium(12f));
        label.setForeground(TEXT);
        return label;
    }

    private void saveCourt(Court original, String name, BigDecimal price, boolean active) {
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() {
                if (original == null) {
                    courtService.addCourt(name, price, active);
                    return true;
                }
                return courtService.updateCourt(new Court(original.id(), name, price, active));
            }
            @Override protected void done() {
                try {
                    if (!get()) {
                        MintDialogs.showMessageDialog(CourtPanel.this, "Không tìm thấy sân để cập nhật.",
                                "Thông báo", JOptionPane.WARNING_MESSAGE);
                    } else {
                        MintDialogs.showMessageDialog(CourtPanel.this,
                                original == null ? "Thêm sân thành công." : "Cập nhật sân thành công.",
                                "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    }
                    loadCourts();
                } catch (Exception ex) {
                    showError("Không thể lưu thông tin sân.", rootCause(ex));
                }
            }
        }.execute();
    }

    private void changeStatus(Court court) {
        boolean toActive = !court.active();
        if (!toActive) {
            int choice = MintDialogs.showConfirmDialog(this,
                    "Ngừng hoạt động sân \"" + court.name() + "\"?\nSân sẽ được giữ trong cơ sở dữ liệu.",
                    "Xác nhận ngừng hoạt động", JOptionPane.YES_NO_OPTION);
            if (choice != JOptionPane.YES_OPTION) return;
        }
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() {
                if (!toActive) return courtService.deleteCourt(court.id());
                return courtService.updateCourt(new Court(court.id(), court.name(), court.pricePerHour(), true));
            }
            @Override protected void done() {
                try {
                    if (get()) {
                        MintDialogs.showMessageDialog(CourtPanel.this,
                                toActive ? "Đã kích hoạt lại sân." : "Đã ngừng hoạt động sân.",
                                "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        MintDialogs.showMessageDialog(CourtPanel.this, "Không tìm thấy sân.",
                                "Thông báo", JOptionPane.WARNING_MESSAGE);
                    }
                    loadCourts();
                } catch (Exception ex) {
                    showError("Không thể thay đổi trạng thái sân.", rootCause(ex));
                }
            }
        }.execute();
    }

    private void showError(String message, Throwable cause) {
        MintDialogs.showMessageDialog(this, message + "\n" + cause.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private static Throwable rootCause(Throwable ex) {
        if (ex instanceof ExecutionException && ex.getCause() != null) return ex.getCause();
        return ex;
    }

    private static String money(BigDecimal amount) {
        NumberFormat n = NumberFormat.getNumberInstance(VI);
        n.setMaximumFractionDigits(0);
        return n.format(amount) + " đ";
    }

    private static JPanel clear(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setOpaque(false);
        return p;
    }

    private static class CourtSymbol extends JComponent {
        @Override protected void paintComponent(Graphics g) {
            Graphics2D gg = (Graphics2D) g.create();
            gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int s = Math.min(getWidth(), getHeight());
            gg.setColor(PALE);
            gg.fillRoundRect(0, 0, s, s, 15, 15);
            gg.setColor(TEAL);
            gg.setStroke(new BasicStroke(1.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int x = (s - 28) / 2, y = (s - 26) / 2;
            gg.drawRoundRect(x, y, 28, 26, 4, 4);
            gg.drawLine(x + 14, y, x + 14, y + 26);
            gg.drawLine(x, y + 13, x + 28, y + 13);
            gg.drawLine(x + 5, y, x + 5, y + 26);
            gg.drawLine(x + 23, y, x + 23, y + 26);
            gg.dispose();
        }
    }

    private static class SearchIcon implements Icon {
        public int getIconWidth() { return 19; }
        public int getIconHeight() { return 19; }
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D gg = (Graphics2D) g.create();
            gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gg.setColor(MUTED);
            gg.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            gg.drawOval(x + 2, y + 2, 10, 10);
            gg.drawLine(x + 11, y + 12, x + 17, y + 18);
            gg.dispose();
        }
    }

    private static class HintField extends JTextField {
        private final String hint;
        HintField(String hint) {
            this.hint = hint;
            setFont(AppFonts.regular(12f));
            setForeground(TEXT);
            setOpaque(false);
            setBorder(new EmptyBorder(5, 0, 5, 2));
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (!getText().isEmpty() || isFocusOwner()) return;
            Graphics2D gg = (Graphics2D) g.create();
            gg.setColor(MUTED);
            gg.setFont(getFont());
            FontMetrics fm = gg.getFontMetrics();
            gg.drawString(hint, 0, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            gg.dispose();
        }
    }

    private static class RoundedSearch extends JPanel {
        RoundedSearch(JTextField field) {
            super(new BorderLayout(10, 0));
            setOpaque(false);
            setBorder(new EmptyBorder(4, 12, 4, 11));
            JLabel icon = new JLabel(new SearchIcon());
            add(icon, BorderLayout.WEST);
            add(field, BorderLayout.CENTER);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D gg = (Graphics2D) g.create();
            gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gg.setColor(WHITE);
            gg.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 13, 13);
            gg.setColor(BORDER);
            gg.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 13, 13);
            gg.dispose();
            super.paintComponent(g);
        }
    }

    private static class RoundedCombo extends JComboBox<String> {
        RoundedCombo(String[] values) {
            super(values);
            setFont(AppFonts.regular(12f));
            setForeground(TEXT);
            setBackground(WHITE);
            setOpaque(false);
            setBorder(new EmptyBorder(4, 11, 4, 5));
            setFocusable(false);
            setUI(new BasicComboBoxUI() {
                @Override public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                    // De trong suot de nen bo tron cua RoundedCombo hien ra.
                }
                @Override protected JButton createArrowButton() {
                    JButton b = new JButton() {
                        @Override protected void paintComponent(Graphics g) {
                            Graphics2D gg = (Graphics2D) g.create();
                            gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            gg.setColor(MUTED);
                            gg.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                            int x = getWidth() / 2, y = getHeight() / 2;
                            gg.drawLine(x - 4, y - 2, x, y + 2);
                            gg.drawLine(x, y + 2, x + 4, y - 2);
                            gg.dispose();
                        }
                    };
                    b.setOpaque(false);
                    b.setBorderPainted(false);
                    b.setContentAreaFilled(false);
                    b.setFocusable(false);
                    b.setPreferredSize(new Dimension(23, 29));
                    return b;
                }
            });
            setRenderer(new DefaultListCellRenderer() {
                @Override public Component getListCellRendererComponent(JList<?> list, Object value,
                        int index, boolean selected, boolean focus) {
                    JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, selected, focus);
                    label.setFont(AppFonts.regular(12f));
                    label.setBorder(new EmptyBorder(5, 5, 5, 3));
                    if (index < 0) {
                        label.setOpaque(false);
                        label.setForeground(TEXT);
                    }
                    return label;
                }
            });
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D gg = (Graphics2D) g.create();
            gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gg.setColor(WHITE);
            gg.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 13, 13);
            gg.setColor(BORDER);
            gg.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 13, 13);
            gg.dispose();
            super.paintComponent(g);
        }
    }

    private static class RoundedCard extends JPanel {
        RoundedCard() { setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D gg = (Graphics2D) g.create();
            gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gg.setColor(WHITE);
            gg.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            gg.setColor(BORDER);
            gg.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            gg.dispose();
            super.paintComponent(g);
        }
    }

    private static class RoundedTable extends JPanel {
        RoundedTable() { setOpaque(false); setBorder(new EmptyBorder(1, 1, 1, 1)); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D gg = (Graphics2D) g.create();
            gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gg.setColor(WHITE);
            gg.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            gg.dispose();
            super.paintComponent(g);
        }
        @Override protected void paintChildren(Graphics g) {
            Graphics2D gg = (Graphics2D) g.create();
            gg.setClip(new RoundRectangle2D.Double(1, 1, getWidth() - 2, getHeight() - 2, 13, 13));
            super.paintChildren(gg);
            gg.dispose();
        }
        @Override protected void paintBorder(Graphics g) {
            Graphics2D gg = (Graphics2D) g.create();
            gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gg.setColor(BORDER);
            gg.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            gg.dispose();
        }
    }

    private static class PillButton extends JButton {
        private final Color color;
        PillButton(String text, Color bg, Color fg) {
            super(text);
            color = bg;
            setFont(AppFonts.bold(12f));
            setForeground(fg);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(5, 13, 5, 13));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D gg = (Graphics2D) g.create();
            gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gg.setColor(isEnabled() ? color : BORDER);
            gg.fillRoundRect(0, 0, getWidth(), getHeight(), 13, 13);
            gg.dispose();
            super.paintComponent(g);
        }
    }

    private static class CourtPhoto implements Icon {
        private final int width, height;
        private final BufferedImage image;
        CourtPhoto(long courtId, int width, int height) {
            this.width = width;
            this.height = height;
            BufferedImage found = null;
            String base = "/images/courts/court-" + courtId;
            for (String ext : new String[]{".jpg", ".png", ".jpeg"}) {
                URL res = CourtPanel.class.getResource(base + ext);
                if (res == null) continue;
                try { found = ImageIO.read(res); break; }
                catch (Exception ignored) { /* hien placeholder neu khong doc duoc anh */ }
            }
            image = found;
        }
        public int getIconWidth() { return width; }
        public int getIconHeight() { return height; }
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D gg = (Graphics2D) g.create();
            gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gg.setClip(new RoundRectangle2D.Double(x, y, width, height, 12, 12));
            gg.setColor(new Color(219, 241, 236));
            gg.fillRect(x, y, width, height);
            if (image != null) {
                double scale = Math.max((double) width / image.getWidth(), (double) height / image.getHeight());
                int w = (int) Math.ceil(image.getWidth() * scale);
                int h = (int) Math.ceil(image.getHeight() * scale);
                gg.drawImage(image, x + (width - w) / 2, y + (height - h) / 2, w, h, null);
            } else {
                gg.setColor(new Color(38, 143, 119));
                gg.fillRoundRect(x + width / 8, y + height / 6,
                        width * 3 / 4, height * 2 / 3, 6, 6);
                gg.setColor(WHITE);
                gg.setStroke(new BasicStroke(1.5f));
                gg.drawRect(x + width / 5, y + height / 4, width * 3 / 5, height / 2);
                gg.drawLine(x + width / 2, y + height / 4, x + width / 2, y + height * 3 / 4);
                gg.drawLine(x + width / 5, y + height / 2, x + width * 4 / 5, y + height / 2);
            }
            gg.dispose();
        }
    }

    private static class CourtTableModel extends AbstractTableModel {
        private final String[] headers = {"#", "Sân", "Giá / giờ", "Trạng thái", "Thao tác"};
        private List<Court> visible = List.of();
        void setCourts(List<Court> list) { visible = new ArrayList<>(list); fireTableDataChanged(); }
        Court getCourt(int row) { return visible.get(row); }
        public int getRowCount() { return visible.size(); }
        public int getColumnCount() { return headers.length; }
        public String getColumnName(int col) { return headers[col]; }
        public boolean isCellEditable(int row, int col) { return false; }
        public Object getValueAt(int row, int col) {
            Court court = visible.get(row);
            return col == 0 ? court.id() : court;
        }
    }

    private static Color rowBG(JTable table, int row, boolean selected) {
        return selected ? table.getSelectionBackground() : (row % 2 == 0 ? WHITE : new Color(249, 252, 251));
    }

    private static class HeaderRenderer extends DefaultTableCellRenderer {
        HeaderRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(new EmptyBorder(0, 8, 0, 8));
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v,
                boolean selected, boolean focus, int row, int column) {
            super.getTableCellRendererComponent(t, v, selected, focus, row, column);
            setFont(AppFonts.bold(12f));
            setForeground(TEXT);
            setBackground(new Color(234, 247, 243));
            setHorizontalAlignment(column == 1 ? SwingConstants.LEFT : SwingConstants.CENTER);
            if (column == 1) setBorder(new EmptyBorder(0, 13, 0, 8));
            else setBorder(new EmptyBorder(0, 8, 0, 8));
            return this;
        }
    }

    private static class IdRenderer extends DefaultTableCellRenderer {
        IdRenderer() { setHorizontalAlignment(SwingConstants.CENTER); }
        @Override public Component getTableCellRendererComponent(JTable t, Object v,
                boolean selected, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, v, selected, focus, row, col);
            setFont(AppFonts.medium(12f));
            setForeground(MUTED);
            setBackground(rowBG(t, row, selected));
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, LINE));
            return this;
        }
    }

    private static class CourtRenderer implements TableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object value,
                boolean selected, boolean focus, int row, int col) {
            Court court = (Court) value;
            JPanel p = new JPanel(new BorderLayout(12, 0));
            p.setOpaque(true);
            p.setBackground(rowBG(t, row, selected));
            p.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, LINE), new EmptyBorder(9, 8, 9, 5)));
            JLabel image = new JLabel(new CourtPhoto(court.id(), 57, 53));
            image.setPreferredSize(new Dimension(57, 53));
            JPanel labels = clear(new GridLayout(2, 1, 0, 1));
            JLabel name = new JLabel(court.name());
            name.setFont(AppFonts.bold(12f));
            name.setForeground(TEXT);
            JLabel id = new JLabel("Mã sân #" + court.id());
            id.setFont(AppFonts.regular(10f));
            id.setForeground(MUTED);
            labels.add(name);
            labels.add(id);
            p.add(image, BorderLayout.WEST);
            p.add(labels, BorderLayout.CENTER);
            return p;
        }
    }

    private static class PriceRenderer extends DefaultTableCellRenderer {
        PriceRenderer() { setHorizontalAlignment(SwingConstants.CENTER); }
        @Override public Component getTableCellRendererComponent(JTable t, Object value,
                boolean selected, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, value, selected, focus, row, col);
            setText(money(((Court) value).pricePerHour()));
            setFont(AppFonts.bold(12f));
            setForeground(TEAL);
            setBackground(rowBG(t, row, selected));
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, LINE));
            return this;
        }
    }

    private static class StatusRenderer extends JPanel implements TableCellRenderer {
        private boolean active;
        StatusRenderer() { setOpaque(true); }
        @Override public Component getTableCellRendererComponent(JTable t, Object value,
                boolean selected, boolean focus, int row, int col) {
            active = ((Court) value).active();
            setBackground(rowBG(t, row, selected));
            return this;
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D gg = (Graphics2D) g.create();
            gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = Math.min(140, getWidth() - 14), h = 29;
            int x = (getWidth() - w) / 2, y = (getHeight() - h) / 2;
            gg.setColor(active ? new Color(226, 248, 235) : new Color(253, 241, 228));
            gg.fillRoundRect(x, y, w, h, 14, 14);
            gg.setColor(active ? new Color(39, 157, 100) : ORANGE);
            gg.fillOval(x + 10, y + 12, 6, 6);
            gg.setFont(AppFonts.medium(11f));
            gg.drawString(active ? "Đang hoạt động" : "Ngừng hoạt động", x + 23, y + 19);
            gg.setColor(LINE);
            gg.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            gg.dispose();
        }
    }

    private static class ActionsRenderer extends JPanel implements TableCellRenderer {
        private boolean active;
        ActionsRenderer() { setOpaque(true); }
        @Override public Component getTableCellRendererComponent(JTable t, Object value,
                boolean selected, boolean focus, int row, int col) {
            active = ((Court) value).active();
            setBackground(rowBG(t, row, selected));
            return this;
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D gg = (Graphics2D) g.create();
            gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            String[] labels = {"Xem", "Sửa", active ? "Ngừng" : "Bật lại"};
            Color[] ink = {BLUE, ORANGE, active ? RED : TEAL};
            Color[] pale = {new Color(230, 240, 253), new Color(255, 244, 226),
                    active ? new Color(254, 236, 237) : PALE};
            int third = getWidth() / 3;
            gg.setFont(AppFonts.bold(11f));
            FontMetrics fm = gg.getFontMetrics();
            for (int i = 0; i < 3; i++) {
                int x = i * third + 4;
                int w = (i == 2 ? getWidth() - i * third : third) - 8;
                int y = (getHeight() - 31) / 2;
                gg.setColor(pale[i]);
                gg.fillRoundRect(x, y, w, 31, 13, 13);
                gg.setColor(ink[i]);
                gg.drawString(labels[i], x + (w - fm.stringWidth(labels[i])) / 2, y + 21);
            }
            gg.setColor(LINE);
            gg.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            gg.dispose();
        }
    }
}
