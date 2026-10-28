
package com.badminton.view;

import com.badminton.model.Booking;
import com.badminton.model.Court;
import com.badminton.service.CourtService;
import com.badminton.service.ScheduleService;
import com.badminton.util.AppFonts;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

/**
 * Lich dat san - giao dien dang the theo san (mau 3).
 * Chi thay giao dien; khong thay doi BookingDAO, CourtDAO hay database.
 */
public class SchedulePanel extends JPanel {

    private static final Color BG = new Color(243, 248, 247);
    private static final Color TEXT = new Color(17, 57, 51);
    private static final Color MUTED = new Color(111, 137, 131);
    private static final Color TEAL = new Color(24, 156, 139);
    private static final Color CONFIRMED = new Color(170, 242, 217);
    private static final Color PENDING = new Color(255, 228, 156);
    private static final Color CANCELLED = new Color(255, 196, 192);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private final ScheduleService scheduleService = new ScheduleService();
    private final CourtService courtService = new CourtService();
    private final JPanel cardList = new ScrollableList();
    private final JLabel countLabel = new JLabel("Đang tải lịch...");
    private final JButton viewButton = new RoundedViewButton("Xem lịch");
    private RoundedDatePicker dateSpinner;
    private SwingWorker<ScheduleData, Void> worker;

    public SchedulePanel() {
        setLayout(new BorderLayout(0, 22));
        setBackground(BG);
        setBorder(new EmptyBorder(27, 30, 28, 30));

        add(createHeader(), BorderLayout.NORTH);
        cardList.setLayout(new BoxLayout(cardList, BoxLayout.Y_AXIS));
        cardList.setOpaque(false);

        JScrollPane scroll = new JScrollPane(cardList,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(22);
        add(scroll, BorderLayout.CENTER);

        loadSchedule();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel eyebrow = label("THEO DÕI LỊCH ĐẶT SÂN", 12f, true, TEAL);
        JLabel title = label("Lịch sân", 28f, true, TEXT);
        JLabel subtitle = label("Theo dõi lịch đặt sân theo ngày và từng khung giờ.", 12.5f, false, MUTED);

        header.add(eyebrow);
        header.add(Box.createVerticalStrut(7));
        header.add(title);
        header.add(Box.createVerticalStrut(5));
        header.add(subtitle);
        header.add(Box.createVerticalStrut(22));

        JPanel controls = new JPanel(new BorderLayout(16, 0));
        controls.setOpaque(false);
        controls.setAlignmentX(Component.LEFT_ALIGNMENT);
        controls.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        JLabel dateLabel = label("Ngày:", 13f, true, TEXT);

        dateSpinner = new RoundedDatePicker();
        dateSpinner.setPreferredSize(new Dimension(194, 42));

        viewButton.setFont(AppFonts.medium(13f));
        viewButton.setPreferredSize(new Dimension(112, 42));
        viewButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        viewButton.addActionListener(e -> loadSchedule());
        // Date changes take effect when the user presses Xem lich.

        countLabel.setFont(AppFonts.medium(12f));
        countLabel.setForeground(MUTED);
        left.add(dateLabel);
        left.add(dateSpinner);
        left.add(viewButton);
        left.add(Box.createHorizontalStrut(5));
        left.add(countLabel);
        controls.add(left, BorderLayout.WEST);

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        legend.setOpaque(false);
        legend.add(legendItem("Đã xác nhận", new Color(19, 166, 116)));
        legend.add(legendItem("Đang chờ", new Color(238, 174, 30)));
        legend.add(legendItem("Đã hủy", new Color(227, 93, 87)));
        controls.add(legend, BorderLayout.EAST);
        header.add(controls);
        return header;
    }

    /** "Xem lịch" uses antialiased round corners, not the native square button. */
    private static final class RoundedViewButton extends JButton {
        RoundedViewButton(String text) {
            super(text);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setRolloverEnabled(true);
            setBorder(new EmptyBorder(8, 15, 8, 15));
            setForeground(Color.WHITE);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill;
            if (!isEnabled()) fill = new Color(163, 202, 193);
            else if (getModel().isPressed()) fill = new Color(7, 109, 96);
            else if (getModel().isRollover()) fill = new Color(12, 132, 116);
            else fill = TEAL;
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static JLabel label(String text, float size, boolean bold, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(bold ? AppFonts.bold(size) : AppFonts.regular(size));
        label.setForeground(color);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private static JPanel legendItem(String text, Color dotColor) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        item.setOpaque(false);
        JComponent dot = new JComponent() {
            @Override public Dimension getPreferredSize() { return new Dimension(9, 16); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(dotColor);
                g2.fillOval(0, 4, 8, 8);
                g2.dispose();
            }
        };
        item.add(dot);
        item.add(label(text, 11f, false, MUTED));
        return item;
    }

    private LocalDate getSelectedDate() {
        Date selected = (Date) dateSpinner.getValue();
        return selected.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void loadSchedule() {
        final LocalDate selectedDate = getSelectedDate();
        if (worker != null && !worker.isDone()) worker.cancel(true);
        countLabel.setText("Đang tải lịch...");
        viewButton.setEnabled(false);
        showMessage("Đang tải lịch sân...", "Vui lòng kiểm tra kết nối MySQL nếu tải quá lâu.");

        SwingWorker<ScheduleData, Void> task = new SwingWorker<>() {
            @Override protected ScheduleData doInBackground() {
                // Keep all current service/database behavior unchanged.
                List<Court> courts = courtService.listCourts();
                List<Booking> bookings = scheduleService.schedule(selectedDate);
                return new ScheduleData(courts, bookings);
            }

            @Override protected void done() {
                if (worker != this) return; // Do not overwrite with stale results.
                viewButton.setEnabled(true);
                try {
                    ScheduleData data = get();
                    renderSchedule(data);
                } catch (CancellationException ignored) {
                    return;
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    countLabel.setText("Tải lịch bị gián đoạn");
                    showMessage("Không tải được lịch sân", "Thử nhấn Xem lịch một lần nữa.");
                } catch (ExecutionException ex) {
                    countLabel.setText("Không tải được dữ liệu");
                    showMessage("Không thể tải lịch sân", "Kiểm tra kết nối MySQL rồi nhấn Xem lịch.");
                }
            }
        };
        worker = task;
        task.execute();
    }

    private void renderSchedule(ScheduleData data) {
        Map<Long, List<Booking>> grouped = new HashMap<>();
        for (Booking booking : data.bookings()) {
            grouped.computeIfAbsent(booking.courtId(), key -> new ArrayList<>())
                    .add(booking);
        }

        Map<Long, CourtInfo> courtOrder = new LinkedHashMap<>();
        for (Court court : data.courts()) {
            courtOrder.put(court.id(), new CourtInfo(court.id(), court.name(), court.active()));
        }
        // Historical bookings may refer to a court no longer in the court list.
        for (Booking booking : data.bookings()) {
            courtOrder.putIfAbsent(booking.courtId(),
                    new CourtInfo(booking.courtId(), "Sân " + booking.courtId(), false));
        }

        int total = data.bookings().size();
        countLabel.setText(total + " lịch đặt · " + courtOrder.size() + " sân");
        cardList.removeAll();
        if (courtOrder.isEmpty()) {
            showMessage("Chưa có sân nào", "Hãy thêm sân trong mục Quản lý sân trước.");
            return;
        }

        int startHour = 7;
        int endHour = 23;
        for (Booking b : data.bookings()) {
            startHour = Math.min(startHour, b.startTime().getHour());
            endHour = Math.max(endHour, b.endTime().getHour()
                    + (b.endTime().getMinute() > 0 || b.endTime().getSecond() > 0 ? 1 : 0));
        }
        endHour = Math.max(startHour + 1, endHour);

        for (CourtInfo court : courtOrder.values()) {
            List<Booking> items = grouped.getOrDefault(court.id(), List.of());
            List<Booking> sorted = new ArrayList<>(items);
            sorted.sort(Comparator.comparing(Booking::startTime).thenComparingLong(Booking::id));
            JPanel card = createCourtCard(court, sorted, startHour, endHour);
            card.setAlignmentX(Component.LEFT_ALIGNMENT);
            cardList.add(card);
            cardList.add(Box.createVerticalStrut(14));
        }
        cardList.revalidate();
        cardList.repaint();
    }

    private JPanel createCourtCard(CourtInfo court, List<Booking> bookings,
                                   int startHour, int endHour) {
        Timeline timeline = new Timeline(bookings, startHour, endHour);
        int cardHeight = Math.max(155, timeline.getPreferredSize().height + 30);
        JPanel card = new RoundedCard();
        card.setLayout(new BorderLayout(18, 0));
        card.setBorder(new EmptyBorder(16, 18, 20, 18));
        card.setPreferredSize(new Dimension(760, cardHeight));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, cardHeight));

        JPanel courtInfo = new JPanel(new BorderLayout(13, 0));
        courtInfo.setOpaque(false);
        courtInfo.setPreferredSize(new Dimension(300, 114));

        CourtImage image = new CourtImage(court.id());
        image.setPreferredSize(new Dimension(108, 108));
        image.setMinimumSize(new Dimension(108, 108));
        JPanel photoWrap = new JPanel(new GridBagLayout());
        photoWrap.setOpaque(false);
        photoWrap.add(image);
        photoWrap.setPreferredSize(new Dimension(108, 114));
        courtInfo.add(photoWrap, BorderLayout.WEST);

        JPanel infoText = new JPanel();
        infoText.setOpaque(false);
        infoText.setLayout(new BoxLayout(infoText, BoxLayout.Y_AXIS));
        infoText.add(Box.createVerticalGlue());
        JLabel name = label(court.name(), 14f, true, TEXT);
        name.setToolTipText(court.name());
        infoText.add(name);
        infoText.add(Box.createVerticalStrut(5));
        infoText.add(label("Mã sân: #" + court.id(), 10.5f, false, MUTED));
        infoText.add(Box.createVerticalStrut(7));
        String summary = bookings.isEmpty() ? "Trống lịch" : bookings.size() + " lượt đặt";
        infoText.add(label(summary, 11f, true, bookings.isEmpty() ? MUTED : TEAL));
        if (!court.active()) {
            infoText.add(Box.createVerticalStrut(4));
            infoText.add(label("Ngừng hoạt động", 10f, false, new Color(188, 95, 79)));
        }
        infoText.add(Box.createVerticalGlue());
        courtInfo.add(infoText, BorderLayout.CENTER);

        card.add(courtInfo, BorderLayout.WEST);
        // Center the clock strip vertically beside the fixed-size court photo.
        // GridBag keeps its height natural while stretching the timeline width.
        JPanel timelineWrap = new JPanel(new GridBagLayout());
        timelineWrap.setOpaque(false);
        timelineWrap.setBorder(new EmptyBorder(0, 14, 0, 0));
        GridBagConstraints timelineBounds = new GridBagConstraints();
        timelineBounds.gridx = 0;
        timelineBounds.gridy = 0;
        timelineBounds.weightx = 1;
        timelineBounds.weighty = 1;
        timelineBounds.fill = GridBagConstraints.HORIZONTAL;
        timelineBounds.anchor = GridBagConstraints.CENTER;
        timelineWrap.add(timeline, timelineBounds);
        card.add(timelineWrap, BorderLayout.CENTER);
        return card;
    }

    private void showMessage(String title, String description) {
        cardList.removeAll();
        JPanel box = new RoundedCard();
        box.setLayout(new GridBagLayout());
        box.setPreferredSize(new Dimension(650, 230));
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 230));
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        JLabel heading = label(title, 17f, true, TEXT);
        JLabel detail = label(description, 12f, false, MUTED);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);
        detail.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(heading);
        content.add(Box.createVerticalStrut(10));
        content.add(detail);
        box.add(content);
        cardList.add(box);
        cardList.revalidate();
        cardList.repaint();
    }

    private record ScheduleData(List<Court> courts, List<Booking> bookings) {}
    private record CourtInfo(long id, String name, boolean active) {}

    /** The scroll area stretches cards to the available width. */
    private static final class ScrollableList extends JPanel implements Scrollable {
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle visible, int orientation, int direction) {
            return 24;
        }
        @Override public int getScrollableBlockIncrement(Rectangle visible, int orientation, int direction) {
            return Math.max(100, visible.height - 50);
        }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    private static final class RoundedCard extends JPanel {
        RoundedCard() { setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(227, 237, 234));
            g2.fillRoundRect(0, 3, getWidth() - 1, getHeight() - 4, 22, 22);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 5, 22, 22);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Shows images from src/main/resources/images/courts/court-<ID>.jpg/png. */
    private static final class CourtImage extends JComponent {
        private static final Map<Long, BufferedImage> CACHE = new HashMap<>();
        private final BufferedImage image;

        CourtImage(long id) {
            this.image = CACHE.computeIfAbsent(id, CourtImage::readImage);
        }

        private static BufferedImage readImage(long id) {
            String[] candidates = {
                    "/images/courts/court-" + id + ".jpg",
                    "/images/courts/court-" + id + ".png",
                    "/images/courts/default.jpg",
                    "/images/courts/default.png"
            };
            for (String path : candidates) {
                URL url = CourtImage.class.getResource(path);
                if (url == null) continue;
                try {
                    BufferedImage result = ImageIO.read(url);
                    if (result != null) return result;
                } catch (IOException ignored) {
                    // Try the next image, then use the fallback drawing.
                }
            }
            return null;
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            if (w <= 0 || h <= 0) { g2.dispose(); return; }
            g2.setClip(new RoundRectangle2D.Float(0, 0, w, h, 15, 15));
            if (image != null) {
                double factor = Math.max((double) w / image.getWidth(), (double) h / image.getHeight());
                int dw = (int) Math.ceil(image.getWidth() * factor);
                int dh = (int) Math.ceil(image.getHeight() * factor);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.drawImage(image, (w - dw) / 2, (h - dh) / 2, dw, dh, null);
            } else {
                g2.setPaint(new GradientPaint(0, 0, new Color(4, 55, 51),
                        w, h, new Color(32, 149, 120)));
                g2.fillRect(0, 0, w, h);
                g2.setColor(new Color(46, 178, 139));
                g2.fillPolygon(new int[]{w / 2 - 18, w / 2 + 18, w - 6, 6},
                        new int[]{12, 12, h - 7, h - 7}, 4);
                g2.setColor(new Color(231, 255, 245));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawPolygon(new int[]{w / 2 - 18, w / 2 + 18, w - 6, 6},
                        new int[]{12, 12, h - 7, h - 7}, 4);
                g2.drawLine(w / 2, 12, w / 2, h - 7);
                g2.drawLine(7, h / 2 + 5, w - 7, h / 2 + 5);
            }
            g2.dispose();
        }
    }

    /** Paints a real time-scaled schedule, including overlapping/cancelled bookings. */
    private static final class Timeline extends JComponent {
        private static final int TOP = 12;
        private static final int LANE_H = 38;
        private final int fromHour;
        private final int toHour;
        private final List<PlacedBooking> placed = new ArrayList<>();
        private final List<HitBox> hitBoxes = new ArrayList<>();
        private final int lanes;

        Timeline(List<Booking> bookings, int fromHour, int toHour) {
            this.fromHour = fromHour;
            this.toHour = toHour;
            List<LocalTime> laneEnds = new ArrayList<>();
            for (Booking b : bookings) {
                int lane = 0;
                while (lane < laneEnds.size() && b.startTime().isBefore(laneEnds.get(lane))) lane++;
                if (lane == laneEnds.size()) laneEnds.add(b.endTime());
                else laneEnds.set(lane, b.endTime());
                placed.add(new PlacedBooking(b, lane));
            }
            lanes = Math.max(1, laneEnds.size());
            setPreferredSize(new Dimension(480, TOP + lanes * LANE_H + 35));
            setOpaque(false);
            setToolTipText(" ");
        }

        @Override public String getToolTipText(MouseEvent event) {
            for (HitBox item : hitBoxes) {
                if (item.bounds().contains(event.getPoint())) {
                    Booking b = item.booking();
                    return "Đơn #" + b.id() + " | KH #" + b.customerId() + " | "
                            + b.startTime().format(TIME) + " - " + b.endTime().format(TIME)
                            + " | " + status(b.status());
                }
            }
            return null;
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int x0 = 4, w = Math.max(1, getWidth() - 9);
            int trackHeight = lanes * LANE_H + 6;
            g2.setColor(new Color(246, 249, 248));
            g2.fillRoundRect(x0, TOP, w, trackHeight, 13, 13);
            g2.setColor(new Color(227, 236, 233));
            g2.setStroke(new BasicStroke(1f));

            int hours = Math.max(1, toHour - fromHour);
            int labelEvery = w / hours < 43 ? 4 : w / hours < 73 ? 2 : 1;
            for (int hour = fromHour; hour <= toHour; hour++) {
                int x = x0 + (int) Math.round(w * (hour - fromHour) / (double) hours);
                g2.drawLine(x, TOP, x, TOP + trackHeight);
                if ((hour - fromHour) % labelEvery == 0 || hour == toHour) {
                    String time = String.format("%02d:00", hour);
                    g2.setFont(AppFonts.regular(10f));
                    g2.setColor(MUTED);
                    FontMetrics fm = g2.getFontMetrics();
                    int labelX = Math.max(0, Math.min(getWidth() - fm.stringWidth(time),
                            x - fm.stringWidth(time) / 2));
                    g2.drawString(time, labelX, TOP + trackHeight + 21);
                    g2.setColor(new Color(227, 236, 233));
                }
            }

            hitBoxes.clear();
            if (placed.isEmpty()) {
                g2.setColor(MUTED);
                g2.setFont(AppFonts.regular(12f));
                String empty = "Chưa có lịch đặt sân";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(empty, x0 + Math.max(0, (w - fm.stringWidth(empty)) / 2),
                        TOP + 27);
            }

            for (PlacedBooking item : placed) {
                Booking b = item.booking();
                double start = minutes(b.startTime()) - fromHour * 60.0;
                double finish = minutes(b.endTime()) - fromHour * 60.0;
                int bx = x0 + (int) Math.round(w * start / (hours * 60.0));
                int ex = x0 + (int) Math.round(w * finish / (hours * 60.0));
                int bw = Math.max(3, ex - bx);
                int by = TOP + 3 + item.lane() * LANE_H;
                int bh = LANE_H - 6;
                g2.setColor(colorFor(b.status()));
                g2.fillRoundRect(bx, by, bw, bh, 10, 10);
                hitBoxes.add(new HitBox(new Rectangle(bx, by, bw, bh), b));

                Shape old = g2.getClip();
                g2.clip(new RoundRectangle2D.Float(bx + 2, by, Math.max(1, bw - 4), bh, 10, 10));
                g2.setColor(TEXT);
                g2.setFont(AppFonts.bold(10f));
                String who = "KH #" + b.customerId();
                FontMetrics fm = g2.getFontMetrics();
                if (fm.stringWidth(who) + 7 <= bw) {
                    g2.drawString(who, bx + (bw - fm.stringWidth(who)) / 2, by + 13);
                }
                String time = b.startTime().format(TIME) + " - " + b.endTime().format(TIME);
                g2.setFont(AppFonts.regular(9f));
                fm = g2.getFontMetrics();
                if (fm.stringWidth(time) + 7 <= bw) {
                    g2.drawString(time, bx + (bw - fm.stringWidth(time)) / 2, by + 26);
                }
                g2.setClip(old);
            }
            g2.dispose();
        }

        private static int minutes(LocalTime time) {
            return time.getHour() * 60 + time.getMinute();
        }
        private static Color colorFor(String status) {
            if ("CONFIRMED".equalsIgnoreCase(status)) return CONFIRMED;
            if ("PENDING".equalsIgnoreCase(status)) return PENDING;
            if ("CANCELLED".equalsIgnoreCase(status)) return CANCELLED;
            return new Color(218, 232, 228);
        }
        private static String status(String status) {
            if ("CONFIRMED".equalsIgnoreCase(status)) return "Đã xác nhận";
            if ("PENDING".equalsIgnoreCase(status)) return "Đang chờ";
            if ("CANCELLED".equalsIgnoreCase(status)) return "Đã hủy";
            return status == null ? "" : status;
        }
        private record PlacedBooking(Booking booking, int lane) {}
        private record HitBox(Rectangle bounds, Booking booking) {}
    }
}
