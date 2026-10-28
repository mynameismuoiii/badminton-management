package com.badminton.view;

import com.badminton.ui.MintDialogs;

import com.badminton.dao.BookingDAO;
import com.badminton.dao.CourtDAO;
import com.badminton.model.Booking;
import com.badminton.model.Court;
import com.badminton.util.TimeSlotUtil;
import com.badminton.util.AppFonts;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParseException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Locale;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * Kham pha san - mau 2: bo loc bo goc va the san co anh.
 * Chi goi SuggestionService hien tai, khong thay doi DAO/DB.
 */
public class SuggestionPanel extends JPanel {

    private static final Color BG = new Color(243, 249, 247);
    private static final Color WHITE = Color.WHITE;
    private static final Color TEXT = new Color(10, 58, 52);
    private static final Color MUTED = new Color(109, 137, 131);
    private static final Color GREEN = new Color(8, 93, 79);
    private static final Color MINT = new Color(29, 170, 143);
    private static final Color PALE = new Color(226, 248, 242);
    private static final Color BORDER = new Color(217, 235, 230);

    private final CourtDAO courtDAO = new CourtDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    private final Consumer<Long> onBookCourt;
    private final RoundedDatePicker datePicker = new RoundedDatePicker();
    private final TimeSelector fromPicker = new TimeSelector("Bắt đầu");
    private final TimeSelector toPicker = new TimeSelector("Kết thúc");
    private final JLabel resultLabel = new JLabel("Chọn ngày, giờ để tìm sân phù hợp");
    private final JPanel cards = new ScrollableCards();
    private final ActionButton searchButton = new ActionButton("Tìm sân", true);
    private SwingWorker<List<CourtAvailability>, Void> searchWorker;

    public SuggestionPanel() {
        this(null);
    }

    /**
     * onBookCourt: MainFrame truyen vao callback chon ID san va mo tab Dat san.
     */
    public SuggestionPanel(Consumer<Long> onBookCourt) {
        this.onBookCourt = onBookCourt;
        setBackground(BG);
        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(27, 30, 28, 30));

        add(createTop(), BorderLayout.NORTH);
        datePicker.addChangeListener(e -> markFilterChanged());
        fromPicker.setOnSelection(this::markFilterChanged);
        toPicker.setOnSelection(this::markFilterChanged);

        cards.setOpaque(false);
        cards.setLayout(new BoxLayout(cards, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(cards,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        add(scroll, BorderLayout.CENTER);

        showMessage("Tìm sân cho trận đấu của bạn",
                "Chọn ngày và khung giờ rồi nhấn Tìm sân.", false);
        // Tai du lieu sau khi UI hoan tat (khong khoa giao dien khi truy van MySQL).
        SwingUtilities.invokeLater(this::searchCourts);
    }

    private JPanel createTop() {
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JLabel eyebrow = text("TH - MINTON  /  KHÁM PHÁ SÂN", 11f, MINT, true);
        JLabel title = text("Tìm sân trống phù hợp", 26f, TEXT, true);
        JLabel description = text("Khám phá sân đang còn trống và đặt lịch chỉ với một cú nhấp.",
                13f, MUTED, false);
        eyebrow.setAlignmentX(LEFT_ALIGNMENT);
        title.setAlignmentX(LEFT_ALIGNMENT);
        description.setAlignmentX(LEFT_ALIGNMENT);
        top.add(eyebrow);
        top.add(Box.createVerticalStrut(8));
        top.add(title);
        top.add(Box.createVerticalStrut(7));
        top.add(description);
        top.add(Box.createVerticalStrut(22));

        RoundedCard filterCard = new RoundedCard(WHITE, 22);
        filterCard.setLayout(new BorderLayout());
        filterCard.setBorder(new EmptyBorder(17, 19, 18, 19));
        filterCard.setAlignmentX(LEFT_ALIGNMENT);
        filterCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 115));
        filterCard.setPreferredSize(new Dimension(670, 115));

        JPanel fields = new JPanel(new FlowLayout(FlowLayout.LEFT, 13, 1));
        fields.setOpaque(false);
        datePicker.setPreferredSize(new Dimension(180, 43));
        fields.add(field("Ngày đặt sân", datePicker));

        fromPicker.setTime(LocalTime.of(18, 0));
        toPicker.setTime(LocalTime.of(19, 0));
        fields.add(field("Bắt đầu", fromPicker));
        fields.add(field("Kết thúc", toPicker));

        JPanel searchField = new JPanel(new BorderLayout());
        searchField.setOpaque(false);
        searchField.setBorder(new EmptyBorder(17, 0, 0, 0));
        searchButton.setPreferredSize(new Dimension(136, 43));
        searchButton.addActionListener(e -> searchCourts());
        searchField.add(searchButton, BorderLayout.CENTER);
        fields.add(searchField);
        filterCard.add(fields, BorderLayout.CENTER);
        top.add(filterCard);
        top.add(Box.createVerticalStrut(21));

        JPanel resultRow = new JPanel(new BorderLayout());
        resultRow.setOpaque(false);
        resultRow.setAlignmentX(LEFT_ALIGNMENT);
        JLabel resultTitle = text("Sân gợi ý dành cho bạn", 18.5f, TEXT, true);
        resultLabel.setFont(AppFonts.medium(12f));
        resultLabel.setForeground(MUTED);
        resultRow.add(resultTitle, BorderLayout.WEST);
        resultRow.add(resultLabel, BorderLayout.EAST);
        top.add(resultRow);
        return top;
    }

    private static JPanel field(String label, JComponent control) {
        JPanel p = new JPanel(new BorderLayout(0, 7));
        p.setOpaque(false);
        JLabel heading = text(label, 11.5f, MUTED, true);
        p.add(heading, BorderLayout.NORTH);
        p.add(control, BorderLayout.CENTER);
        return p;
    }

    /** Never leave old booking buttons active when the user changes filters. */
    private void markFilterChanged() {
        if (searchWorker != null && !searchWorker.isDone()) searchWorker.cancel(true);
        searchButton.setEnabled(true);
        searchButton.setText("Tìm sân");
        resultLabel.setText("Nhấn Tìm sân để cập nhật");
        showMessage("Đã đổi thời gian tìm kiếm",
                "Nhấn Tìm sân để xem trạng thái đặt sân theo khung giờ mới.", false);
    }

    private void searchCourts() {
        final LocalDate day;
        final LocalTime from;
        final LocalTime to;
        try {
            datePicker.commitEdit();
            Date date = (Date) datePicker.getValue();
            day = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            from = fromPicker.getTime();
            to = toPicker.getTime();
            if (!TimeSlotUtil.isValidTimeSlot(from, to)) {
                throw new IllegalArgumentException("Giờ bắt đầu phải nhỏ hơn giờ kết thúc.");
            }
            if (!TimeSlotUtil.isWithinOpeningHours(from, to)) {
                throw new IllegalArgumentException("Giờ đặt sân trong khoảng 06:00 - 22:00.");
            }
        } catch (ParseException ex) {
            showMessage("Ngày không hợp lệ", "Vui lòng nhập ngày theo dạng dd/MM/yyyy.", true);
            resultLabel.setText("Kiểm tra ngày đã nhập");
            return;
        } catch (IllegalArgumentException ex) {
            showMessage("Khung giờ chưa hợp lệ", ex.getMessage(), true);
            resultLabel.setText("Vui lòng kiểm tra bộ lọc");
            return;
        }

        if (searchWorker != null && !searchWorker.isDone()) searchWorker.cancel(true);
        searchButton.setEnabled(false);
        searchButton.setText("Đang tìm...");
        resultLabel.setText("Đang tìm sân trống...");
        showMessage("Đang kiểm tra lịch sân", "Đang tìm sân trống theo ngày và giờ bạn chọn.", false);

        SwingWorker<List<CourtAvailability>, Void> worker = new SwingWorker<>() {
            @Override protected List<CourtAvailability> doInBackground() throws SQLException {
                return loadAvailability(day, from, to);
            }
            @Override protected void done() {
                if (this != searchWorker) return;
                searchButton.setEnabled(true);
                searchButton.setText("Tìm sân");
                try {
                    List<CourtAvailability> found = get();
                    long free = found.stream().filter(CourtAvailability::canBook).count();
                    resultLabel.setText(free + " sân còn trống / " + found.size() + " sân");
                    showCourts(found, day, from, to);
                } catch (CancellationException ignored) {
                    // Tim kiem moi da thay the ket qua nay.
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    resultLabel.setText("Tìm kiếm bị gián đoạn");
                    showMessage("Không thể tìm sân", "Hãy thử tìm lại.", true);
                } catch (ExecutionException ex) {
                    resultLabel.setText("Không thể tải dữ liệu");
                    String detail = ex.getCause() instanceof IllegalArgumentException
                            ? ex.getCause().getMessage()
                            : "Không kết nối được dữ liệu sân. Kiểm tra MySQL rồi thử lại.";
                    showMessage("Không thể tìm sân trống", detail, true);
                }
            }
        };
        searchWorker = worker;
        worker.execute();
    }

    /** Query the same bookings table used by BookingPanel; no DAO/service change. */
    private List<CourtAvailability> loadAvailability(LocalDate day, LocalTime from,
                                                      LocalTime to) throws SQLException {
        List<Court> allCourts = courtDAO.findAll();
        List<Booking> bookings = bookingDAO.findByDate(day);
        Set<Long> occupiedIds = new HashSet<>();
        for (Booking booking : bookings) {
            if ("CANCELLED".equalsIgnoreCase(booking.status())) continue;
            if (TimeSlotUtil.isOverlap(from, to, booking.startTime(), booking.endTime())) {
                occupiedIds.add(booking.courtId());
            }
        }
        boolean past = day.atTime(from).isBefore(LocalDateTime.now());
        List<CourtAvailability> result = new ArrayList<>();
        for (Court court : allCourts) {
            if (court.active()) result.add(
                    new CourtAvailability(court, occupiedIds.contains(court.id()), past));
        }
        // Available courts first; sort by price within each status.
        result.sort(Comparator.comparing(CourtAvailability::canBook).reversed()
                .thenComparing(x -> x.court().pricePerHour(),
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(x -> x.court().name(),
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
        return result;
    }

    private record CourtAvailability(Court court, boolean occupied, boolean past) {
        boolean canBook() { return !occupied && !past; }
    }

    private void showCourts(List<CourtAvailability> results, LocalDate day,
                            LocalTime from, LocalTime to) {
        cards.removeAll();
        if (results.isEmpty()) {
            showMessage("Chưa có sân hoạt động",
                    "Hiện chưa có sân để hiển thị.", false);
            return;
        }
        for (CourtAvailability result : results) {
            JPanel card = createCourtCard(result, day, from, to);
            card.setAlignmentX(LEFT_ALIGNMENT);
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 184));
            card.setPreferredSize(new Dimension(700, 184));
            cards.add(card);
            cards.add(Box.createVerticalStrut(14));
        }
        cards.revalidate();
        cards.repaint();
    }

    private JPanel createCourtCard(CourtAvailability result, LocalDate day,
                                   LocalTime from, LocalTime to) {
        Court court = result.court();
        RoundedCard card = new RoundedCard(WHITE, 24);
        card.setLayout(new BorderLayout(18, 0));
        card.setBorder(new EmptyBorder(15, 16, 18, 17));

        CourtImage image = new CourtImage(court.id());
        image.setPreferredSize(new Dimension(182, 145));
        card.add(image, BorderLayout.WEST);

        JPanel details = new JPanel();
        details.setOpaque(false);
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
        JLabel name = text(court.name() == null ? "Sân cầu lông" : court.name(),
                17f, TEXT, true);
        name.setToolTipText(court.name());
        name.setAlignmentX(LEFT_ALIGNMENT);
        JLabel number = new PillLabel("ID: " + court.id(), PALE, GREEN);
        number.setAlignmentX(LEFT_ALIGNMENT);
        JLabel price = text(formatMoney(court.pricePerHour()) + " / giờ", 14f, GREEN, true);
        price.setAlignmentX(LEFT_ALIGNMENT);
        String statusText = result.occupied() ? "●  Đã hết"
                : result.past() ? "●  Đã qua" : "●  Còn trống";
        Color statusBg = result.occupied() ? new Color(255, 239, 235)
                : result.past() ? new Color(244, 242, 239) : new Color(228, 250, 234);
        Color statusFg = result.occupied() ? new Color(179, 81, 65)
                : result.past() ? new Color(115, 112, 107) : new Color(26, 136, 78);
        JLabel status = new PillLabel(statusText, statusBg, statusFg);
        status.setAlignmentX(LEFT_ALIGNMENT);
        details.add(Box.createVerticalStrut(9));
        details.add(name);
        details.add(Box.createVerticalStrut(11));
        details.add(number);
        details.add(Box.createVerticalStrut(10));
        details.add(price);
        details.add(Box.createVerticalStrut(8));
        details.add(status);
        card.add(details, BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridBagLayout());
        actions.setOpaque(false);
        ActionButton book = new ActionButton(result.occupied() ? "Đã hết"
                : result.past() ? "Đã qua" : "Đặt sân", true);
        book.setPreferredSize(new Dimension(123, 43));
        book.setEnabled(result.canBook());
        book.setToolTipText(result.occupied()
                ? "Sân này đã có lịch đặt trùng khung giờ"
                : result.past() ? "Khung giờ đã qua, không thể đặt"
                : "Chuyển sang Đặt sân");
        if (result.canBook()) {
            book.addActionListener(e -> verifyAndBook(book, court.id(), day, from, to));
        }
        actions.add(book);
        card.add(actions, BorderLayout.EAST);
        return card;
    }

    /** Verify current booking records before navigating, in case availability changed. */
    private void verifyAndBook(ActionButton book, long courtId, LocalDate day,
                               LocalTime from, LocalTime to) {
        book.setEnabled(false);
        book.setText("Đang kiểm tra...");
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() throws SQLException {
                if (day.atTime(from).isBefore(LocalDateTime.now())) return false;
                for (Booking b : bookingDAO.findByCourtAndDate(courtId, day)) {
                    if (!"CANCELLED".equalsIgnoreCase(b.status())
                            && TimeSlotUtil.isOverlap(from, to, b.startTime(), b.endTime())) {
                        return false;
                    }
                }
                return true;
            }
            @Override protected void done() {
                try {
                    if (get()) {
                        openBooking(courtId);
                    } else {
                        MintDialogs.showMessageDialog(SuggestionPanel.this,
                                "Khung giờ này không còn đặt được. Danh sách sẽ cập nhật.",
                                "Lịch sân đã thay đổi", JOptionPane.INFORMATION_MESSAGE);
                        searchCourts();
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    book.setEnabled(true);
                    book.setText("Đặt sân");
                } catch (ExecutionException ex) {
                    book.setEnabled(true);
                    book.setText("Đặt sân");
                    MintDialogs.showMessageDialog(SuggestionPanel.this,
                            "Không kiểm tra được lịch sân. Vui lòng thử lại.",
                            "Lỗi kết nối", JOptionPane.WARNING_MESSAGE);
                }
            }
        }.execute();
    }

    private void openBooking(long courtId) {
        if (onBookCourt != null) {
            onBookCourt.accept(courtId);
            return;
        }
        // Compatibility with older MainFrame instances that call new SuggestionPanel().
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof MainFrame mainFrame) {
            mainFrame.openBookingCourt(courtId);
        } else {
            MintDialogs.showMessageDialog(this,
                    "Cần cập nhật MainFrame.java để kết nối trang Đặt sân.");
        }
    }

    private void showMessage(String title, String detail, boolean error) {
        cards.removeAll();
        RoundedCard state = new RoundedCard(WHITE, 24);
        state.setLayout(new GridBagLayout());
        state.setBorder(new EmptyBorder(35, 18, 35, 18));
        state.setAlignmentX(LEFT_ALIGNMENT);
        state.setPreferredSize(new Dimension(700, 235));
        state.setMaximumSize(new Dimension(Integer.MAX_VALUE, 235));
        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        JLabel mark = new JLabel(error ? "!" : "✓", SwingConstants.CENTER);
        mark.setFont(AppFonts.bold(21f));
        mark.setForeground(error ? new Color(192, 81, 78) : MINT);
        mark.setOpaque(true);
        mark.setBackground(error ? new Color(255, 235, 233) : PALE);
        mark.setPreferredSize(new Dimension(50, 50));
        mark.setMaximumSize(new Dimension(50, 50));
        mark.setAlignmentX(CENTER_ALIGNMENT);
        JLabel heading = text(title, 17f, TEXT, true);
        JLabel sub = text(detail, 12f, MUTED, false);
        heading.setAlignmentX(CENTER_ALIGNMENT);
        sub.setAlignmentX(CENTER_ALIGNMENT);
        inner.add(mark);
        inner.add(Box.createVerticalStrut(15));
        inner.add(heading);
        inner.add(Box.createVerticalStrut(7));
        inner.add(sub);
        state.add(inner);
        cards.add(state);
        cards.revalidate();
        cards.repaint();
    }

    private static JLabel text(String value, float size, Color color, boolean bold) {
        JLabel label = new JLabel(value);
        label.setFont(bold ? AppFonts.bold(size) : AppFonts.regular(size));
        label.setForeground(color);
        return label;
    }

    private static String formatMoney(BigDecimal amount) {
        if (amount == null) return "Liên hệ";
        NumberFormat f = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
        f.setMaximumFractionDigits(0);
        return f.format(amount) + " đ";
    }


    /** ScrollPane chi cuon doc, cac the luon trai rong theo noi dung. */
    private static class ScrollableCards extends JPanel implements Scrollable {
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle rect, int axis, int direction) { return 18; }
        @Override public int getScrollableBlockIncrement(Rectangle rect, int axis, int direction) { return 160; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    private static class RoundedCard extends JPanel {
        private final Color fill;
        private final int round;
        RoundedCard(Color fill, int round) {
            this.fill = fill;
            this.round = round;
            setOpaque(false);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D p = (Graphics2D) g.create();
            p.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth() - 1, h = getHeight() - 1;
            if (w > 0 && h > 0) {
                p.setColor(fill);
                p.fillRoundRect(0, 0, w, h, round, round);
                p.setColor(BORDER);
                p.setStroke(new BasicStroke(1f));
                p.drawRoundRect(0, 0, w, h, round, round);
            }
            p.dispose();
            super.paintComponent(g);
        }
    }

    private static class PillLabel extends JLabel {
        private final Color fill;
        PillLabel(String value, Color fill, Color ink) {
            super(value);
            this.fill = fill;
            setForeground(ink);
            setFont(AppFonts.medium(11f));
            setOpaque(false);
            setBorder(new EmptyBorder(5, 11, 5, 11));
            Dimension size = getPreferredSize();
            setMaximumSize(size);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D p = (Graphics2D) g.create();
            p.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            p.setColor(fill);
            p.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
            p.dispose();
            super.paintComponent(g);
        }
    }

    private static class ActionButton extends JButton {
        private final boolean prominent;
        ActionButton(String label, boolean prominent) {
            super(label);
            this.prominent = prominent;
            setFont(AppFonts.medium(12.5f));
            setForeground(WHITE);
            setBorderPainted(false);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D p = (Graphics2D) g.create();
            p.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill = !isEnabled() ? new Color(167, 185, 180)
                    : getModel().isRollover() ? MINT : prominent ? GREEN : MINT;
            p.setColor(fill);
            p.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
            p.dispose();
            super.paintComponent(g);
        }
    }

    private static class ChevronIcon implements Icon {
        @Override public int getIconWidth() { return 16; }
        @Override public int getIconHeight() { return 16; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D p = (Graphics2D) g.create();
            p.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            p.setColor(GREEN);
            p.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            p.drawLine(x+4, y+6, x+8, y+10);
            p.drawLine(x+8, y+10, x+12, y+6);
            p.dispose();
        }
    }

    /** A compact, three-column popup instead of the native long JComboBox list. */
    private static final class TimeSelector extends JPanel {
        private final String title;
        private final TimeFieldButton button = new TimeFieldButton();
        private LocalTime selected = LocalTime.of(18, 0);
        private Runnable onSelection = () -> {};

        TimeSelector(String title) {
            this.title = title;
            setOpaque(false);
            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(135, 43));
            setMinimumSize(new Dimension(120, 43));
            add(button, BorderLayout.CENTER);
            setTime(selected);
            button.addActionListener(e -> showTimePopup());
        }

        void setOnSelection(Runnable handler) { onSelection = handler; }
        LocalTime getTime() { return selected; }
        void setTime(LocalTime time) {
            selected = time;
            button.setText(String.format("%02d:%02d", selected.getHour(), selected.getMinute()));
        }

        private void showTimePopup() {
            JPopupMenu popup = new JPopupMenu();
            popup.setOpaque(false);
            popup.setBorder(new EmptyBorder(0, 0, 0, 0));

            RoundedCard surface = new RoundedCard(WHITE, 21);
            surface.setLayout(new BorderLayout(0, 10));
            surface.setBorder(new EmptyBorder(14, 13, 13, 13));

            JPanel heading = new JPanel(new BorderLayout());
            heading.setOpaque(false);
            JLabel titleLabel = text("Chọn giờ " + title.toLowerCase(), 13f, TEXT, true);
            JLabel subtitle = text("06:00 - 22:00", 11f, MUTED, false);
            heading.add(titleLabel, BorderLayout.WEST);
            heading.add(subtitle, BorderLayout.EAST);
            surface.add(heading, BorderLayout.NORTH);

            JPanel grid = new JPanel(new GridLayout(0, 3, 7, 7));
            grid.setBackground(WHITE);
            int index = 0;
            int selectedIndex = 0;
            for (LocalTime t = LocalTime.of(6, 0);
                 !t.isAfter(LocalTime.of(22, 0)); t = t.plusMinutes(30)) {
                if (selected.equals(t)) selectedIndex = index;
                TimeChoiceButton choice = new TimeChoiceButton(
                        String.format("%02d:%02d", t.getHour(), t.getMinute()), selected.equals(t));
                LocalTime time = t;
                choice.addActionListener(e -> {
                    setTime(time);
                    popup.setVisible(false);
                    onSelection.run();
                });
                grid.add(choice);
                index++;
            }
            // Six visible rows with soft mint pills, scrolling only when needed.
            JScrollPane scroller = new JScrollPane(grid,
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scroller.setBorder(BorderFactory.createEmptyBorder());
            scroller.getViewport().setBackground(WHITE);
            scroller.setPreferredSize(new Dimension(283, 238));
            scroller.getVerticalScrollBar().setUnitIncrement(35);
            scroller.getVerticalScrollBar().setPreferredSize(new Dimension(7, 0));
            surface.add(scroller, BorderLayout.CENTER);
            popup.add(surface);
            popup.show(this, 0, getHeight() + 5);
            final int row = selectedIndex / 3;
            SwingUtilities.invokeLater(() -> scroller.getVerticalScrollBar().setValue(
                    Math.max(0, (row - 2) * 39)));
        }
    }

    private static final class TimeFieldButton extends JButton {
        TimeFieldButton() {
            setFont(AppFonts.medium(13f));
            setHorizontalAlignment(SwingConstants.LEFT);
            setForeground(TEXT);
            setBorder(new EmptyBorder(0, 14, 0, 12));
            setOpaque(false);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D p = (Graphics2D) g.create();
            p.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            p.setColor(getModel().isRollover() ? new Color(240, 251, 247) : WHITE);
            p.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);
            p.setColor(getModel().isRollover() ? MINT : BORDER);
            p.setStroke(new BasicStroke(1.1f));
            p.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 16, 16);
            p.setColor(GREEN);
            int x = getWidth()-22, y = getHeight()/2;
            p.setStroke(new BasicStroke(1.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            p.drawLine(x-4, y-2, x, y+2);
            p.drawLine(x, y+2, x+4, y-2);
            p.dispose();
            super.paintComponent(g);
        }
    }

    private static final class TimeChoiceButton extends JButton {
        private final boolean active;
        TimeChoiceButton(String value, boolean active) {
            super(value);
            this.active = active;
            setFont(AppFonts.medium(12.5f));
            setForeground(active ? WHITE : TEXT);
            setPreferredSize(new Dimension(80, 32));
            setBorder(new EmptyBorder(6, 2, 6, 2));
            setContentAreaFilled(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D p = (Graphics2D) g.create();
            p.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            p.setColor(active ? GREEN : getModel().isRollover() ? PALE : new Color(247, 251, 249));
            p.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
            p.dispose();
            super.paintComponent(g);
        }
    }

    private static class CourtImage extends JComponent {
        private final BufferedImage picture;
        CourtImage(long courtId) {
            setOpaque(false);
            picture = loadPicture(courtId);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D p = (Graphics2D) g.create();
            p.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            p.setClip(new RoundRectangle2D.Float(0, 0, w, h, 18, 18));
            if (picture != null) {
                double scale = Math.max(w / (double) picture.getWidth(),
                        h / (double) picture.getHeight());
                int dw = (int) Math.ceil(picture.getWidth() * scale);
                int dh = (int) Math.ceil(picture.getHeight() * scale);
                p.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                p.drawImage(picture, (w-dw)/2, (h-dh)/2, dw, dh, null);
            } else {
                p.setPaint(new GradientPaint(0, 0, new Color(9, 68, 62),
                        w, h, new Color(30, 159, 131)));
                p.fillRect(0, 0, w, h);
                p.setColor(new Color(235, 255, 245, 205));
                p.setStroke(new BasicStroke(2f));
                p.drawRect(17, 22, Math.max(0,w-34), Math.max(0,h-44));
                p.drawLine(w/2, 22, w/2, h-22);
                p.drawLine(17, h/2, w-17, h/2);
            }
            p.dispose();
        }
        private static BufferedImage loadPicture(long courtId) {
            for (String extension : new String[]{"jpg", "png"}) {
                URL url = SuggestionPanel.class.getResource(
                        "/images/courts/court-" + courtId + "." + extension);
                if (url != null) {
                    try { return ImageIO.read(url); } catch (Exception ignored) { }
                }
            }
            return null;
        }
    }
}
