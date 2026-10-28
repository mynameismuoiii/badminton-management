package com.badminton.view;

import com.badminton.model.Court;
import com.badminton.service.CourtService;
import com.badminton.util.AppFonts;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * Danh sach san cho khach hang (va admin), dung du lieu CourtService san co.
 * Sidebar do MainFrame quan ly; panel nay chi ve noi dung ben phai.
 */
public final class CustomerCourtListPanel extends JPanel implements Scrollable {

    private static final Color BG = new Color(243, 248, 247);
    private static final Color TEXT = new Color(22, 58, 52);
    private static final Color MUTED = new Color(109, 135, 128);
    private static final Color DARK = new Color(7, 57, 51);
    private static final Color MINT = new Color(47, 201, 174);
    private static final Color LIGHT_MINT = new Color(224, 248, 242);

    private final CourtService courtService = new CourtService();
    private final Consumer<Long> onBookCourt;
    private final JPanel cardGrid = new JPanel();
    private final JPanel results = new JPanel(new BorderLayout());
    private final JLabel countLabel = new JLabel("Đang tải danh sách sân...");
    private final JTextField searchField = new SearchField();
    private final JButton refreshButton = new SoftButton("Làm mới");
    private final List<Court> activeCourts = new ArrayList<>();

    private SwingWorker<List<Court>, Void> worker;
    private int columns = -1;
    private boolean loading = true;
    private boolean loadError = false;

    public CustomerCourtListPanel() {
        this(null);
    }

    public CustomerCourtListPanel(Consumer<Long> onBookCourt) {
        this.onBookCourt = onBookCourt;
        setLayout(new BorderLayout(0, 24));
        setBackground(BG);
        setBorder(new EmptyBorder(28, 30, 38, 30));

        add(createTopSection(), BorderLayout.NORTH);

        cardGrid.setOpaque(false);
        results.setOpaque(false);
        add(results, BorderLayout.CENTER);

        refreshButton.addActionListener(e -> loadCourts());
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { renderCourts(); }
            @Override public void removeUpdate(DocumentEvent e) { renderCourts(); }
            @Override public void changedUpdate(DocumentEvent e) { renderCourts(); }
        });

        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) { updateColumns(); }
        });

        showState("Đang tải sân...", "Đang lấy danh sách sân từ hệ thống.", false);
        loadCourts();
    }

    private JPanel createTopSection() {
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JLabel eyebrow = new JLabel("KHÁM PHÁ & ĐẶT SÂN");
        eyebrow.setFont(AppFonts.bold(12f));
        eyebrow.setForeground(new Color(24, 156, 139));
        eyebrow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Tìm sân cho trận đấu của bạn");
        title.setFont(AppFonts.bold(25f));
        title.setForeground(TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel intro = new JLabel("Lựa chọn sân phù hợp, xem mức giá và đặt lịch chỉ với một cú nhấp.");
        intro.setFont(AppFonts.regular(12.5f));
        intro.setForeground(MUTED);
        intro.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel controls = new JPanel(new BorderLayout(12, 0));
        controls.setOpaque(false);
        controls.setAlignmentX(Component.LEFT_ALIGNMENT);
        controls.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        controls.setPreferredSize(new Dimension(600, 46));

        RoundSurface searchSurface = new RoundSurface(Color.WHITE, 18);
        searchSurface.setLayout(new BorderLayout(10, 0));
        searchSurface.setBorder(new EmptyBorder(0, 16, 0, 16));
        JLabel magnifier = new JLabel(new Icon() {
            @Override public int getIconWidth() { return 18; }
            @Override public int getIconHeight() { return 18; }
            @Override public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(MUTED);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.drawOval(x + 2, y + 2, 10, 10);
                g2.drawLine(x + 11, y + 11, x + 16, y + 16);
                g2.dispose();
            }
        });
        searchSurface.add(magnifier, BorderLayout.WEST);

        searchField.setBorder(null);
        searchField.setOpaque(false);
        searchField.setForeground(TEXT);
        searchField.setCaretColor(DARK);
        searchField.setFont(AppFonts.regular(13f));
        searchField.setToolTipText("Nhập tên sân để tìm kiếm");
        searchSurface.add(searchField, BorderLayout.CENTER);

        controls.add(searchSurface, BorderLayout.CENTER);
        controls.add(refreshButton, BorderLayout.EAST);

        JPanel countRow = new JPanel(new BorderLayout());
        countRow.setOpaque(false);
        countRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        countLabel.setFont(AppFonts.medium(12.5f));
        countLabel.setForeground(MUTED);
        countRow.add(countLabel, BorderLayout.WEST);

        top.add(eyebrow);
        top.add(Box.createVerticalStrut(8));
        top.add(title);
        top.add(Box.createVerticalStrut(8));
        top.add(intro);
        top.add(Box.createVerticalStrut(22));
        top.add(controls);
        top.add(Box.createVerticalStrut(20));
        top.add(countRow);
        return top;
    }

    private void loadCourts() {
        if (worker != null && !worker.isDone()) worker.cancel(true);
        loading = true;
        loadError = false;
        refreshButton.setEnabled(false);
        countLabel.setText("Đang tải danh sách sân...");
        showState("Đang tải sân...", "Đang lấy danh sách sân từ hệ thống.", false);

        SwingWorker<List<Court>, Void> request = new SwingWorker<>() {
            @Override
            protected List<Court> doInBackground() {
                return courtService.listCourts();
            }

            @Override
            protected void done() {
                if (worker != this) return; // Khong de ket qua cu ghi de ket qua moi.
                refreshButton.setEnabled(true);
                try {
                    List<Court> data = get();
                    activeCourts.clear();
                    for (Court court : data) {
                        if (court != null && court.active()) activeCourts.add(court);
                    }
                    loadError = false;
                } catch (CancellationException ignored) {
                    return;
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    loadError = true;
                } catch (ExecutionException ex) {
                    loadError = true;
                }
                loading = false;
                renderCourts();
            }
        };
        worker = request;
        request.execute();
    }

    private void renderCourts() {
        if (loading) return;
        if (loadError) {
            countLabel.setText("Không tải được dữ liệu");
            showState("Không thể tải danh sách sân", "Kiểm tra kết nối MySQL rồi nhấn Làm mới.", true);
            return;
        }

        String query = searchField.getText().strip().toLowerCase(Locale.ROOT);
        List<Court> filtered = new ArrayList<>();
        for (Court court : activeCourts) {
            if (court.name() != null && court.name().toLowerCase(Locale.ROOT).contains(query)) {
                filtered.add(court);
            }
        }

        countLabel.setText(query.isEmpty()
                ? activeCourts.size() + " sân đang hoạt động"
                : "Tìm thấy " + filtered.size() + " / " + activeCourts.size() + " sân");

        if (filtered.isEmpty()) {
            showState(activeCourts.isEmpty() ? "Chưa có sân nào" : "Không tìm thấy sân phù hợp",
                    activeCourts.isEmpty() ? "Danh sách sân đang được cập nhật."
                            : "Thử tìm với tên sân khác nhé.", false);
            return;
        }

        cardGrid.removeAll();
        updateColumns();
        for (Court court : filtered) cardGrid.add(new CourtCard(court));
        results.removeAll();
        results.add(cardGrid, BorderLayout.NORTH);
        results.revalidate();
        results.repaint();
    }

    private void updateColumns() {
        int available = getWidth() > 0 ? getWidth() - getInsets().left - getInsets().right : 950;
        int wanted = available >= 920 ? 3 : available >= 610 ? 2 : 1;
        if (wanted == columns) return;
        columns = wanted;
        cardGrid.setLayout(new GridLayout(0, columns, 19, 20));
        cardGrid.revalidate();
        cardGrid.repaint();
    }

    private void showState(String title, String details, boolean isError) {
        results.removeAll();
        RoundSurface empty = new RoundSurface(Color.WHITE, 24);
        empty.setLayout(new GridBagLayout());
        empty.setPreferredSize(new Dimension(500, 270));
        JPanel message = new JPanel();
        message.setOpaque(false);
        message.setLayout(new BoxLayout(message, BoxLayout.Y_AXIS));
        JLabel symbol = new JLabel(isError ? "!" : "⌁", SwingConstants.CENTER);
        symbol.setOpaque(true);
        symbol.setBackground(LIGHT_MINT);
        symbol.setForeground(DARK);
        symbol.setFont(AppFonts.bold(29f));
        symbol.setPreferredSize(new Dimension(56, 56));
        symbol.setMaximumSize(new Dimension(56, 56));
        symbol.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel heading = new JLabel(title);
        heading.setFont(AppFonts.bold(18f));
        heading.setForeground(TEXT);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel subtitle = new JLabel(details);
        subtitle.setFont(AppFonts.regular(12.5f));
        subtitle.setForeground(MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        message.add(symbol);
        message.add(Box.createVerticalStrut(15));
        message.add(heading);
        message.add(Box.createVerticalStrut(7));
        message.add(subtitle);
        empty.add(message);
        results.add(empty, BorderLayout.NORTH);
        results.revalidate();
        results.repaint();
    }

    private final class CourtCard extends RoundSurface {
        CourtCard(Court court) {
            super(Color.WHITE, 24);
            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(250, 347));
            setBorder(new EmptyBorder(0, 0, 6, 0));

            CourtPhoto photo = new CourtPhoto(court.id());
            photo.setPreferredSize(new Dimension(250, 165));
            add(photo, BorderLayout.NORTH);

            JPanel info = new JPanel();
            info.setOpaque(false);
            info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
            info.setBorder(new EmptyBorder(15, 17, 14, 17));

            JLabel activeChip = new JLabel("●  Đang hoạt động");
            activeChip.setFont(AppFonts.medium(10.5f));
            activeChip.setForeground(new Color(19, 139, 107));
            activeChip.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel name = new JLabel(court.name() == null ? "Sân cầu lông" : court.name());
            name.setFont(AppFonts.bold(16.5f));
            name.setForeground(TEXT);
            name.setToolTipText(court.name());
            name.setAlignmentX(Component.LEFT_ALIGNMENT);
            // Khong de ten san qua dai lam giãn vo the.
            name.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

            JLabel id = new JLabel("Mã sân: #" + court.id());
            id.setFont(AppFonts.regular(11f));
            id.setForeground(MUTED);
            id.setAlignmentX(Component.LEFT_ALIGNMENT);

            JPanel footer = new JPanel(new BorderLayout(6, 0));
            footer.setOpaque(false);
            footer.setAlignmentX(Component.LEFT_ALIGNMENT);
            footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 49));

            JPanel priceStack = new JPanel();
            priceStack.setOpaque(false);
            priceStack.setLayout(new BoxLayout(priceStack, BoxLayout.Y_AXIS));
            JLabel priceTitle = new JLabel("Giá thuê / giờ");
            priceTitle.setFont(AppFonts.regular(10.5f));
            priceTitle.setForeground(MUTED);
            JLabel priceValue = new JLabel(formatPrice(court.pricePerHour()));
            priceValue.setFont(AppFonts.bold(14.5f));
            priceValue.setForeground(new Color(15, 143, 121));
            priceStack.add(priceTitle);
            priceStack.add(Box.createVerticalStrut(3));
            priceStack.add(priceValue);

            HoverBookButton book = new HoverBookButton();
            book.setEnabled(onBookCourt != null);
            book.addActionListener(e -> {
                if (onBookCourt != null) onBookCourt.accept(court.id());
            });

            footer.add(priceStack, BorderLayout.CENTER);
            footer.add(book, BorderLayout.EAST);

            info.add(activeChip);
            info.add(Box.createVerticalStrut(9));
            info.add(name);
            info.add(Box.createVerticalStrut(4));
            info.add(id);
            info.add(Box.createVerticalGlue());
            info.add(footer);
            add(info, BorderLayout.CENTER);
        }
    }

    private static String formatPrice(BigDecimal value) {
        if (value == null) return "Liên hệ";
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
        formatter.setMaximumFractionDigits(0);
        return formatter.format(value) + " đ";
    }

    private static class RoundSurface extends JPanel {
        private final Color fill;
        private final int radius;
        RoundSurface(Color fill, int radius) {
            this.fill = fill;
            this.radius = radius;
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            if (w > 0 && h > 0) {
                g2.setColor(new Color(10, 73, 61, 13));
                g2.fillRoundRect(1, 3, w - 2, h - 3, radius, radius);
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, w - 1, h - 4, radius, radius);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Plain Swing khong ho tro placeholderText: tu ve goi y khi o rong.
    private static final class SearchField extends JTextField {
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (!getText().isEmpty()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(MUTED);
            g2.setFont(getFont());
            FontMetrics metrics = g2.getFontMetrics();
            g2.drawString("Tìm theo tên sân...", 1,
                    (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent());
            g2.dispose();
        }
    }

    private static final class SoftButton extends JButton {
        SoftButton(String text) {
            super(text);
            setPreferredSize(new Dimension(100, 46));
            setFont(AppFonts.medium(12f));
            setForeground(DARK);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setOpaque(false);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isEnabled() ? (getModel().isRollover() ? new Color(197, 241, 232) : LIGHT_MINT)
                    : new Color(233, 240, 238));
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 3, 18, 18);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Nút có hiệu ứng thu nhỏ nhẹ khi rê chuột, không dịch chuyển layout. */
    private static final class HoverBookButton extends JButton {
        private float hover = 0f;
        private float target = 0f;
        private final Timer animation;

        HoverBookButton() {
            super("Đặt sân  ›");
            setPreferredSize(new Dimension(116, 43));
            setMinimumSize(new Dimension(116, 43));
            setFont(AppFonts.medium(12f));
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            animation = new Timer(15, e -> {
                hover += (target - hover) * 0.28f;
                if (Math.abs(target - hover) < 0.025f) {
                    hover = target;
                    ((Timer) e.getSource()).stop();
                }
                repaint();
            });
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { animateTo(1f); }
                @Override public void mouseExited(MouseEvent e) { animateTo(0f); }
            });
        }
        private void animateTo(float value) {
            if (!isEnabled()) return;
            target = value;
            if (!animation.isRunning()) animation.start();
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int insetX = Math.round(hover * 5);
            int insetY = Math.round(hover * 3);
            int w = getWidth() - insetX * 2 - 1;
            int h = getHeight() - insetY * 2 - 1;
            Color c = new Color(
                    Math.round(DARK.getRed() * (1 - hover) + 28 * hover),
                    Math.round(DARK.getGreen() * (1 - hover) + 175 * hover),
                    Math.round(DARK.getBlue() * (1 - hover) + 151 * hover));
            g2.setColor(isEnabled() ? c : new Color(155, 172, 166));
            g2.fillRoundRect(insetX, insetY, w, h, 16, 16);
            g2.setFont(getFont());
            g2.setColor(Color.WHITE);
            FontMetrics fm = g2.getFontMetrics();
            String label = getText();
            g2.drawString(label, (getWidth() - fm.stringWidth(label)) / 2,
                    (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    /**
     * Anh san: them src/main/resources/images/courts/court-<id>.jpg (hoac png).
     * Khi khong co anh se ve san cau long minh hoa, KHONG phai anh cua san that.
     */
    private static final class CourtPhoto extends JPanel {
        private final Image image;
        private final long courtId;
        CourtPhoto(long courtId) {
            this.courtId = courtId;
            this.image = loadImage(courtId);
            setOpaque(false);
        }
        private static Image loadImage(long id) {
            String base = "/images/courts/";
            for (String path : new String[]{base + "court-" + id + ".jpg",
                    base + "court-" + id + ".png", base + "default.jpg", base + "default.png"}) {
                URL url = CourtPhoto.class.getResource(path);
                if (url == null) continue;
                try {
                    BufferedImage loaded = ImageIO.read(url);
                    if (loaded != null) return loaded;
                } catch (IOException ignored) { /* Thu anh ke tiep. */ }
            }
            return null;
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            if (w < 2 || h < 2) { g2.dispose(); return; }
            g2.setClip(new RoundRectangle2D.Float(0, 0, w, h + 18, 23, 23));
            if (image != null) {
                int iw = image.getWidth(null), ih = image.getHeight(null);
                if (iw > 0 && ih > 0) {
                    double scale = Math.max((double) w / iw, (double) h / ih);
                    int sw = (int) Math.ceil(iw * scale), sh = (int) Math.ceil(ih * scale);
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2.drawImage(image, (w - sw) / 2, (h - sh) / 2, sw, sh, null);
                }
            } else {
                // Minh hoa nen san neu chua co anh; khong phai du lieu san that.
                g2.setPaint(new GradientPaint(0, 0, new Color(5, 49, 53), w, h, new Color(28, 122, 102)));
                g2.fillRect(0, 0, w, h);
                g2.setColor(new Color(77, 180, 156, 40));
                for (int i = 0; i < 8; i++) g2.drawLine(i * 60, 0, i * 60 - 60, h);
                Polygon floor = new Polygon(
                        new int[]{w / 2 - 65, w / 2 + 65, w - 8, 8},
                        new int[]{34, 34, h - 8, h - 8}, 4);
                g2.setColor(new Color(31, 152, 117));
                g2.fillPolygon(floor);
                g2.setStroke(new BasicStroke(2.2f));
                g2.setColor(new Color(223, 255, 244, 210));
                g2.drawPolygon(floor);
                g2.drawLine(w / 2, 34, w / 2, h - 8);
                g2.drawLine(w / 2 - 90, h / 2 + 6, w / 2 + 90, h / 2 + 6);
                g2.drawLine(w / 2 - 43, 58, w / 2 + 43, 58);
                g2.setStroke(new BasicStroke(1f));
                g2.setColor(new Color(225, 255, 246, 140));
                g2.drawLine(5, h / 2 - 4, w - 5, h / 2 - 4);
                for (int x = 6; x < w; x += 11) g2.drawLine(x, h / 2 - 4, x, h / 2 + 9);
                g2.setFont(AppFonts.bold(10f));
                g2.setColor(new Color(235, 255, 248, 220));
                g2.drawString("TH-MINTON  ·  COURT #" + courtId, 14, 21);
            }
            g2.setClip(null);
            g2.dispose();
        }
    }

    // MainFrame da boc panel bang JScrollPane: khong tao JScrollPane thu hai.
    @Override public Dimension getPreferredScrollableViewportSize() { return new Dimension(950, 650); }
    @Override public int getScrollableUnitIncrement(Rectangle r, int orientation, int direction) { return 24; }
    @Override public int getScrollableBlockIncrement(Rectangle r, int orientation, int direction) { return 180; }
    @Override public boolean getScrollableTracksViewportWidth() { return true; }
    @Override public boolean getScrollableTracksViewportHeight() { return false; }
}
