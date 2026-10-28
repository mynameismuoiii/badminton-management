package com.badminton.view;

import com.badminton.ui.MintDialogs;

import com.badminton.dao.BookingDAO;
import com.badminton.dao.CourtDAO;
import com.badminton.dao.PaymentDAO;
import com.badminton.model.Booking;
import com.badminton.model.Payment;
import com.badminton.util.AppFonts;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

/**
 * TH-MINTON - thong ke & bao cao.
 * Thay doi giao dien: rounded dropdown/button, painted icons, colored KPI cards.
 * Chi DOC du lieu tu BookingDAO, CourtDAO, PaymentDAO; khong sua logic DB.
 * Bao gom bieu do luot dat va doanh thu theo ngay.
 */
public class StatisticsPanel extends JPanel {

    private static final Color BG = new Color(242, 249, 247);
    private static final Color TEXT = new Color(8, 55, 48);
    private static final Color MUTED = new Color(102, 134, 127);
    private static final Color GREEN = new Color(14, 154, 130);
    private static final Color GREEN_DARK = new Color(4, 116, 99);
    private static final Color BLUE = new Color(47, 116, 214);
    private static final Color PURPLE = new Color(132, 73, 200);
    private static final Color ORANGE = new Color(204, 121, 22);
    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("dd/MM");
    private static final NumberFormat VI = NumberFormat.getIntegerInstance(new Locale("vi", "VN"));

    private final BookingDAO bookingDAO = new BookingDAO();
    private final CourtDAO courtDAO = new CourtDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();

    private final JLabel bookingsValue = new JLabel("—");
    private final JLabel courtsValue = new JLabel("—");
    private final JLabel paymentsValue = new JLabel("—");
    private final JLabel revenueValue = new JLabel("—");
    private final JLabel statusLabel = new JLabel("Đang tải thống kê...");
    private final JComboBox<String> rangeCombo = new JComboBox<>(new String[] {
            "7 ngày gần đây", "30 ngày gần đây", "90 ngày gần đây", "Tất cả thời gian"
    });
    private final RoundedActionButton refreshButton = new RoundedActionButton("Làm mới");
    private final ChartPanel bookingsChart = new ChartPanel(false);
    private final ChartPanel revenueChart = new ChartPanel(true);

    private List<Booking> cachedBookings = List.of();
    private List<Payment> cachedPayments = List.of();
    private long cachedActiveCourts;
    private boolean hasData;
    private SwingWorker<Snapshot, Void> loadingTask;

    public StatisticsPanel() {
        this(true);
    }

    // Package-private for UI preview without a running database.
    StatisticsPanel(boolean autoLoad) {
        setLayout(new BorderLayout());
        setBackground(BG);
        setBorder(new EmptyBorder(24, 30, 22, 30));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.add(createHeader());
        content.add(Box.createVerticalStrut(22));
        content.add(createKpiRow());
        content.add(Box.createVerticalStrut(24));
        content.add(createChartsRow());
        content.add(Box.createVerticalStrut(14));

        statusLabel.setFont(AppFonts.regular(11f));
        statusLabel.setForeground(MUTED);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(statusLabel);

        JScrollPane scroll = new JScrollPane(content, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(22);
        add(scroll, BorderLayout.CENTER);
        MintDropdown.install(rangeCombo);

        rangeCombo.addActionListener(e -> { if (hasData) renderCached(); });
        refreshButton.addActionListener(e -> loadStatistics());
        if (autoLoad) loadStatistics();
    }

    private JPanel createHeader() {
        JPanel header = transparent(new BorderLayout(16, 0));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));
        header.setPreferredSize(new Dimension(940, 82));

        JPanel identity = transparent(new BorderLayout(14, 0));
        StatIcon icon = new StatIcon(Kind.CHART, GREEN, new Color(213, 248, 238));
        icon.setPreferredSize(new Dimension(59, 59));
        identity.add(icon, BorderLayout.WEST);

        JPanel texts = transparent();
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.add(Box.createVerticalGlue());
        JLabel title = label("Thống kê & báo cáo", 22f, true, TEXT);
        JLabel description = label("Theo dõi và phân tích hoạt động của TH-MINTON", 12f, false, MUTED);
        texts.add(title);
        texts.add(Box.createVerticalStrut(7));
        texts.add(description);
        texts.add(Box.createVerticalGlue());
        identity.add(texts, BorderLayout.CENTER);
        header.add(identity, BorderLayout.CENTER);

        JPanel controls = transparent(new FlowLayout(FlowLayout.RIGHT, 12, 7));
        controls.add(createRoundedDropdown());
        refreshButton.setPreferredSize(new Dimension(139, 43));
        controls.add(refreshButton);
        header.add(controls, BorderLayout.EAST);
        return header;
    }

    private JComponent createRoundedDropdown() {
        RoundedDropdown surface = new RoundedDropdown(rangeCombo);
        surface.setPreferredSize(new Dimension(207, 43));
        return surface;
    }

    private JPanel createKpiRow() {
        JPanel row = transparent(new GridLayout(1, 4, 18, 0));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 183));
        row.setPreferredSize(new Dimension(940, 183));
        row.add(createKpi("Tổng lượt đặt sân", bookingsValue, "Lượt đặt trong khoảng chọn",
                Kind.BOOKING, GREEN, new Color(214, 248, 237), new Color(237, 255, 248)));
        row.add(createKpi("Sân đang hoạt động", courtsValue, "Sân có thể nhận đặt lịch",
                Kind.COURT, BLUE, new Color(220, 234, 255), new Color(243, 249, 255)));
        row.add(createKpi("Số thanh toán", paymentsValue, "Thanh toán trong khoảng chọn",
                Kind.PAYMENT, PURPLE, new Color(237, 223, 255), new Color(251, 245, 255)));
        row.add(createKpi("Tổng doanh thu", revenueValue, "Từ các khoản đã thanh toán",
                Kind.REVENUE, ORANGE, new Color(255, 237, 205), new Color(255, 250, 238)));
        return row;
    }

    private JComponent createKpi(String title, JLabel value, String subtitle,
                                 Kind kind, Color accent, Color start, Color end) {
        RoundedGradientCard card = new RoundedGradientCard(start, end, accent);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(17, 27, 17, 17));

        JPanel upper = transparent(new BorderLayout(5, 0));
        JLabel titleLabel = label(title, 12f, false, new Color(75, 104, 99));
        titleLabel.setVerticalAlignment(SwingConstants.CENTER);
        upper.add(titleLabel, BorderLayout.CENTER);
        StatIcon icon = new StatIcon(kind, accent, new Color(255, 255, 255, 170));
        icon.setPreferredSize(new Dimension(43, 43));
        upper.add(icon, BorderLayout.EAST);
        card.add(upper, BorderLayout.NORTH);

        JPanel lower = transparent();
        lower.setLayout(new BoxLayout(lower, BoxLayout.Y_AXIS));
        value.setFont(AppFonts.bold(25f));
        value.setForeground(accent);
        value.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel note = label(subtitle, 10f, false, new Color(90, 116, 109));
        lower.add(Box.createVerticalStrut(13));
        lower.add(value);
        lower.add(Box.createVerticalStrut(8));
        lower.add(note);
        card.add(lower, BorderLayout.CENTER);
        return card;
    }

    private JPanel createChartsRow() {
        JPanel row = transparent(new GridLayout(1, 2, 20, 0));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 416));
        row.setPreferredSize(new Dimension(940, 416));
        row.add(createChartCard("Lượt đặt sân theo ngày", "Số lượt đặt sân", GREEN, bookingsChart));
        row.add(createChartCard("Doanh thu theo ngày", "Doanh thu (VND)", BLUE, revenueChart));
        return row;
    }

    private JComponent createChartCard(String title, String legend, Color color, ChartPanel chart) {
        WhiteCard outer = new WhiteCard();
        outer.setLayout(new BorderLayout(0, 10));
        outer.setBorder(new EmptyBorder(22, 19, 12, 19));
        JLabel titleLabel = label(title, 13.5f, true, TEXT);
        outer.add(titleLabel, BorderLayout.NORTH);
        outer.add(chart, BorderLayout.CENTER);
        JPanel footer = transparent(new FlowLayout(FlowLayout.CENTER, 7, 0));
        footer.add(new ColorDot(color));
        footer.add(label(legend, 11f, false, MUTED));
        outer.add(footer, BorderLayout.SOUTH);
        return outer;
    }

    private void loadStatistics() {
        if (loadingTask != null && !loadingTask.isDone()) loadingTask.cancel(true);
        refreshButton.setEnabled(false);
        statusLabel.setText("Đang cập nhật dữ liệu thống kê...");
        SwingWorker<Snapshot, Void> task = new SwingWorker<>() {
            @Override protected Snapshot doInBackground() throws Exception {
                // Read-only operations. No SQL, service or database schema change.
                List<Booking> bookings = bookingDAO.findAll();
                List<Payment> payments = paymentDAO.findAll();
                long activeCourts = courtDAO.findAll().stream().filter(c -> c.active()).count();
                return new Snapshot(bookings, payments, activeCourts);
            }

            @Override protected void done() {
                if (loadingTask != this) return;
                refreshButton.setEnabled(true);
                try {
                    Snapshot data = get();
                    setDashboardData(data.bookings(), data.payments(), data.activeCourts());
                    statusLabel.setText("Dữ liệu thống kê từ hệ thống TH-MINTON");
                } catch (CancellationException ignored) {
                    // Another refresh was requested.
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    statusLabel.setText("Tải dữ liệu bị gián đoạn. Hãy thử Làm mới.");
                } catch (ExecutionException e) {
                    statusLabel.setText("Không thể tải dữ liệu. Kiểm tra kết nối MySQL rồi thử lại.");
                    MintDialogs.showMessageDialog(StatisticsPanel.this,
                            "Không thể tải thống kê. Vui lòng kiểm tra kết nối MySQL.",
                            "Lỗi tải thống kê", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        loadingTask = task;
        task.execute();
    }

    // Accessible to a local, no-DB UI preview; production uses loadStatistics().
    void setDashboardData(List<Booking> bookings, List<Payment> payments, long activeCourts) {
        cachedBookings = bookings == null ? List.of() : List.copyOf(bookings);
        cachedPayments = payments == null ? List.of() : List.copyOf(payments);
        cachedActiveCourts = activeCourts;
        hasData = true;
        renderCached();
        statusLabel.setText("Dữ liệu thống kê từ hệ thống TH-MINTON");
    }

    private void renderCached() {
        LocalDate today = LocalDate.now();
        int index = rangeCombo.getSelectedIndex();
        LocalDate from = switch (index) {
            case 0 -> today.minusDays(6);
            case 1 -> today.minusDays(29);
            case 2 -> today.minusDays(89);
            default -> null;
        };

        List<Booking> selectedBookings = new ArrayList<>();
        for (Booking b : cachedBookings) {
            if (b.bookingDate() != null && (from == null || !b.bookingDate().isBefore(from))
                    && !b.bookingDate().isAfter(today)) selectedBookings.add(b);
        }
        List<Payment> selectedPayments = new ArrayList<>();
        for (Payment p : cachedPayments) {
            if (p.paidAt() == null) {
                if (from == null) selectedPayments.add(p);
                continue;
            }
            LocalDate paidDay = p.paidAt().toLocalDate();
            if ((from == null || !paidDay.isBefore(from)) && !paidDay.isAfter(today)) selectedPayments.add(p);
        }
        BigDecimal revenue = BigDecimal.ZERO;
        for (Payment p : selectedPayments) if (p.amount() != null) revenue = revenue.add(p.amount());
        bookingsValue.setText(VI.format(selectedBookings.size()));
        courtsValue.setText(VI.format(cachedActiveCourts));
        paymentsValue.setText(VI.format(selectedPayments.size()));
        revenueValue.setText(VI.format(revenue) + " đ");

        if (from == null) {
            from = today.minusDays(6);
            for (Booking b : cachedBookings) if (b.bookingDate() != null && b.bookingDate().isBefore(from)) from = b.bookingDate();
            for (Payment p : cachedPayments) if (p.paidAt() != null && p.paidAt().toLocalDate().isBefore(from))
                from = p.paidAt().toLocalDate();
        }
        int days = Math.max(1, (int) (today.toEpochDay() - from.toEpochDay() + 1));
        int bucketDays = days <= 14 ? 1 : days <= 45 ? 3 : days <= 100 ? 9 : Math.max(1, (int) Math.ceil(days / 12.0));
        Map<LocalDate, Integer> bookingPerDay = new HashMap<>();
        for (Booking b : selectedBookings) bookingPerDay.merge(b.bookingDate(), 1, Integer::sum);
        Map<LocalDate, Double> revenuePerDay = new HashMap<>();
        for (Payment p : selectedPayments) if (p.paidAt() != null && p.amount() != null)
            revenuePerDay.merge(p.paidAt().toLocalDate(), p.amount().doubleValue(), Double::sum);
        List<ChartPoint> bookingPoints = new ArrayList<>();
        List<ChartPoint> revenuePoints = new ArrayList<>();
        for (LocalDate day = from; !day.isAfter(today); day = day.plusDays(bucketDays)) {
            LocalDate bucketEnd = day.plusDays(bucketDays - 1);
            if (bucketEnd.isAfter(today)) bucketEnd = today;
            int count = 0;
            double money = 0;
            for (LocalDate d = day; !d.isAfter(bucketEnd); d = d.plusDays(1)) {
                count += bookingPerDay.getOrDefault(d, 0);
                money += revenuePerDay.getOrDefault(d, 0.0);
            }
            String name = day.format(DAY_FORMAT);
            bookingPoints.add(new ChartPoint(name, day, count));
            revenuePoints.add(new ChartPoint(name, day, money));
        }
        bookingsChart.setPoints(bookingPoints);
        revenueChart.setPoints(revenuePoints);
    }

    private static JLabel label(String content, float size, boolean bold, Color color) {
        JLabel label = new JLabel(content);
        label.setFont(bold ? AppFonts.bold(size) : AppFonts.regular(size));
        label.setForeground(color);
        return label;
    }

    private static JPanel transparent() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        return p;
    }
    private static JPanel transparent(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setOpaque(false);
        return p;
    }

    private record Snapshot(List<Booking> bookings, List<Payment> payments, long activeCourts) {}
    private record ChartPoint(String title, LocalDate date, double value) {}
    private enum Kind { CHART, BOOKING, COURT, PAYMENT, REVENUE }

    private static final class ColorDot extends JComponent {
        private final Color color;
        ColorDot(Color color) { this.color = color; setPreferredSize(new Dimension(9, 15)); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color); g2.fillOval(1, 5, 7, 7); g2.dispose();
        }
    }

    private static final class RoundedDropdown extends JPanel {
        RoundedDropdown(JComboBox<String> combo) {
            super(new BorderLayout());
            setOpaque(false);
            setBorder(new EmptyBorder(2, 12, 2, 5));
            combo.setFont(AppFonts.medium(12f));
            combo.setForeground(TEXT);
            combo.setBackground(Color.WHITE);
            combo.setOpaque(false);
            combo.setBorder(null);
            combo.setFocusable(false);
            combo.setUI(new BasicComboBoxUI() {
                @Override protected JButton createArrowButton() {
                    JButton button = new JButton() {
                        @Override protected void paintComponent(Graphics g) {
                            Graphics2D g2 = (Graphics2D) g.create();
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            int cx = getWidth() / 2, cy = getHeight() / 2;
                            g2.setColor(GREEN_DARK);
                            Path2D triangle = new Path2D.Double();
                            triangle.moveTo(cx - 5, cy - 2); triangle.lineTo(cx + 5, cy - 2);
                            triangle.lineTo(cx, cy + 4); triangle.closePath();
                            g2.fill(triangle); g2.dispose();
                        }
                    };
                    button.setOpaque(false);
                    button.setContentAreaFilled(false);
                    button.setBorderPainted(false);
                    button.setFocusPainted(false);
                    button.setPreferredSize(new Dimension(27, 36));
                    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    return button;
                }
                @Override public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                    // The rounded parent paints the background.
                }
            });
            combo.setRenderer(new DefaultListCellRenderer() {
                @Override public Component getListCellRendererComponent(JList<?> list, Object value,
                                                                          int index, boolean selected, boolean focus) {
                    JLabel result = (JLabel) super.getListCellRendererComponent(list, value, index, selected, focus);
                    result.setFont(AppFonts.medium(12f));
                    result.setForeground(TEXT);
                    result.setBackground(selected ? new Color(218, 247, 239) : Color.WHITE);
                    result.setOpaque(index >= 0);
                    result.setBorder(new EmptyBorder(8, 5, 8, 5));
                    return result;
                }
            });
            add(combo, BorderLayout.CENTER);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 17, 17);
            g2.setColor(new Color(201, 226, 219));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 17, 17);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static final class RoundedActionButton extends JButton {
        RoundedActionButton(String text) {
            super(text, new RefreshIcon());
            setFont(AppFonts.bold(12f));
            setForeground(Color.WHITE);
            setIconTextGap(10);
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(new EmptyBorder(10, 12, 10, 12));
            setOpaque(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(!isEnabled() ? new Color(154, 198, 189) :
                    getModel().isPressed() ? GREEN_DARK : getModel().isRollover() ? new Color(8, 133, 111) : GREEN);
            g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 17, 17);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static final class RefreshIcon implements Icon {
        @Override public int getIconWidth() { return 16; }
        @Override public int getIconHeight() { return 16; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawArc(x + 3, y + 3, 10, 10, 65, 275);
            Path2D arrow = new Path2D.Double();
            arrow.moveTo(x + 12, y + 2); arrow.lineTo(x + 13, y + 7); arrow.lineTo(x + 8, y + 6);
            g2.fill(arrow); g2.dispose();
        }
    }

    /** Java2D vector icons avoid missing emoji, unsupported fonts and square symbols. */
    private static final class StatIcon extends JComponent {
        private final Kind kind;
        private final Color ink;
        private final Color bg;
        StatIcon(Kind kind, Color ink, Color bg) { this.kind = kind; this.ink = ink; this.bg = bg; }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg); g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 17, 17);
            g2.translate((getWidth()-26)/2.0, (getHeight()-26)/2.0);
            g2.setColor(ink);
            g2.setStroke(new BasicStroke(2.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            switch (kind) {
                case CHART -> {
                    g2.drawLine(2, 23, 24, 23); g2.fillRoundRect(4, 13, 4, 10, 2, 2);
                    g2.fillRoundRect(11, 7, 4, 16, 2, 2); g2.fillRoundRect(18, 3, 4, 20, 2, 2);
                }
                case BOOKING -> {
                    g2.drawRoundRect(3, 5, 20, 18, 3, 3); g2.drawLine(3, 10, 23, 10);
                    g2.drawLine(8, 2, 8, 8); g2.drawLine(18, 2, 18, 8);
                    g2.fillOval(7, 13, 4, 4); g2.fillOval(15, 13, 4, 4);
                }
                case COURT -> {
                    g2.drawRoundRect(2, 5, 22, 16, 2, 2); g2.drawLine(13, 5, 13, 21);
                    g2.drawLine(2, 13, 24, 13); g2.drawLine(6, 9, 10, 9); g2.drawLine(16, 17, 20, 17);
                }
                case PAYMENT -> {
                    g2.drawRoundRect(2, 5, 22, 16, 3, 3); g2.fillRect(3, 10, 20, 4);
                    g2.drawLine(6, 18, 12, 18);
                }
                case REVENUE -> {
                    g2.drawOval(2, 2, 22, 22); g2.drawLine(13, 5, 13, 21);
                    g2.drawArc(7, 7, 12, 7, 0, 185); g2.drawArc(7, 13, 12, 7, 180, 180);
                }
            }
            g2.dispose();
        }
    }

    private static final class RoundedGradientCard extends JPanel {
        private final Color start, end, accent;
        RoundedGradientCard(Color start, Color end, Color accent) {
            this.start = start; this.end = end; this.accent = accent; setOpaque(false);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, start, getWidth(), getHeight(), end));
            g2.fillRoundRect(1, 1, getWidth()-3, getHeight()-5, 22, 22);
            g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 60));
            g2.setStroke(new BasicStroke(1.1f));
            g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-5, 22, 22);
            g2.setColor(accent);
            g2.fillRoundRect(13, 16, 4, 28, 4, 4);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static final class WhiteCard extends JPanel {
        WhiteCard() { setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(223, 235, 231));
            g2.fillRoundRect(0, 4, getWidth()-1, getHeight()-5, 21, 21);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-5, 21, 21);
            g2.setColor(new Color(221, 235, 231));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-5, 21, 21);
            g2.dispose(); super.paintComponent(g);
        }
    }

    private static final class ChartPanel extends JComponent {
        private final boolean bars;
        private List<ChartPoint> points = List.of();
        private List<Rectangle> hitBoxes = List.of();
        private int hoverIndex = -1;
        ChartPanel(boolean bars) {
            this.bars = bars;
            setOpaque(false);
            setToolTipText(" ");
            addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
                @Override public void mouseMoved(MouseEvent e) {
                    int newHover = -1;
                    for (int i = 0; i < hitBoxes.size(); i++) {
                        if (hitBoxes.get(i).contains(e.getPoint())) { newHover = i; break; }
                    }
                    if (newHover != hoverIndex) { hoverIndex = newHover; repaint(); }
                }
            });
        }
        void setPoints(List<ChartPoint> points) { this.points = List.copyOf(points); hoverIndex = -1; repaint(); }
        @Override public String getToolTipText(MouseEvent event) {
            if (hoverIndex < 0 || hoverIndex >= points.size()) return null;
            ChartPoint p = points.get(hoverIndex);
            return p.date().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ": " +
                    (bars ? VI.format(Math.round(p.value())) + " đ" : VI.format(Math.round(p.value())) + " lượt");
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            if (w < 90 || h < 90) { g2.dispose(); return; }
            int left = 52, right = 13, top = 16, bottom = 38;
            int width = Math.max(1, w-left-right), height = Math.max(1, h-top-bottom);
            double max = 1;
            for (ChartPoint p : points) max = Math.max(max, p.value());
            if (!bars) max = Math.max(1.0, Math.ceil(max * 1.25));
            else max = Math.ceil(max * 1.15 / 5000.0) * 5000.0;
            if (max <= 0) max = 1;

            g2.setFont(AppFonts.regular(10f));
            FontMetrics metrics = g2.getFontMetrics();
            for (int tick = 0; tick <= 4; tick++) {
                int y = top + (height * tick / 4);
                g2.setColor(new Color(226, 238, 235));
                g2.setStroke(new BasicStroke(1f));
                g2.drawLine(left, y, w-right, y);
                double value = max * (4-tick) / 4.0;
                String number = bars ? shortMoney(value) : (max <= 4 ? String.format(Locale.US,"%.1f",value) : VI.format(Math.round(value)));
                g2.setColor(MUTED);
                g2.drawString(number, left-9-metrics.stringWidth(number), y+4);
            }
            if (points.isEmpty()) {
                g2.setColor(MUTED);
                g2.drawString("Chưa có dữ liệu", left + width/2 - 45, top+height/2);
                g2.dispose(); return;
            }
            int n = points.size();
            List<Rectangle> newHits = new ArrayList<>();
            int[] xs = new int[n], ys = new int[n];
            for (int i=0; i<n; i++) {
                xs[i] = left + (n==1 ? width/2 : (int)Math.round(width*i/(double)(n-1)));
                ys[i] = top + height - (int)Math.round(Math.min(1.0, points.get(i).value()/max)*height);
            }
            if (bars) {
                int step = Math.max(1, width / n);
                int barW = Math.max(5, Math.min(35, step*3/5));
                for (int i=0;i<n;i++) {
                    int x = xs[i]-barW/2;
                    int barHeight = top+height-ys[i];
                    g2.setPaint(new GradientPaint(x, ys[i], new Color(86, 162, 230), x, top+height, BLUE));
                    if (barHeight > 0) g2.fillRoundRect(x, ys[i], barW, barHeight, 7, 7);
                    newHits.add(new Rectangle(xs[i]-Math.max(12,step/2), top,
                            Math.max(24,step), height+15));
                }
            } else {
                Path2D fill = new Path2D.Double();
                fill.moveTo(xs[0], top+height);
                for (int i=0;i<n;i++) fill.lineTo(xs[i], ys[i]);
                fill.lineTo(xs[n-1], top+height); fill.closePath();
                g2.setColor(new Color(42, 199, 158, 33));
                g2.fill(fill);
                g2.setColor(GREEN);
                g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                for (int i=1;i<n;i++) g2.drawLine(xs[i-1],ys[i-1],xs[i],ys[i]);
                for (int i=0;i<n;i++) {
                    g2.setColor(GREEN); g2.fillOval(xs[i]-4, ys[i]-4, 8, 8);
                    g2.setColor(Color.WHITE); g2.drawOval(xs[i]-4,ys[i]-4,8,8);
                    int step = Math.max(12,width/Math.max(1,n-1));
                    newHits.add(new Rectangle(xs[i]-step/2, top, step, height+15));
                }
            }
            hitBoxes = newHits;
            int every = Math.max(1, (int)Math.ceil(n/9.0));
            g2.setFont(AppFonts.regular(9.5f));
            metrics = g2.getFontMetrics();
            for (int i=0;i<n;i++) {
                if (i%every!=0 && i!=n-1) continue;
                String t = points.get(i).title();
                g2.setColor(MUTED);
                g2.drawString(t, Math.max(left-12,Math.min(w-right-metrics.stringWidth(t),xs[i]-metrics.stringWidth(t)/2)), top+height+19);
            }
            if (hoverIndex >= 0 && hoverIndex < n) {
                g2.setColor(new Color(13, 83, 76, 60));
                g2.setStroke(new BasicStroke(1f));
                g2.drawLine(xs[hoverIndex], top, xs[hoverIndex], top+height);
                g2.setColor(bars ? BLUE : GREEN);
                g2.fillOval(xs[hoverIndex]-5,ys[hoverIndex]-5,10,10);
            }
            g2.dispose();
        }
        private static String shortMoney(double value) {
            if (value >= 1_000_000) return String.format(Locale.US,"%.1fM",value/1_000_000.0);
            if (value >= 1000) return VI.format(Math.round(value/1000.0)) + "K";
            return VI.format(Math.round(value));
        }
    }
}
