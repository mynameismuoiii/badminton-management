package com.badminton.view;

import com.badminton.dao.BookingDAO;
import com.badminton.dao.CustomerDAO;
import com.badminton.model.Booking;
import com.badminton.model.Court;
import com.badminton.model.Customer;
import com.badminton.model.User;
import com.badminton.service.BookingService;
import com.badminton.service.CourtService;
import com.badminton.util.AppFonts;
import com.badminton.util.PriceCalculator;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * TH-MINTON | Dat san - Mau 2.
 * Giu nguyen API su dung boi MainFrame:
 *   new BookingPanel(user, onPayment), selectCourt(id), refreshData().
 * Khong thay doi BookingDAO / BookingService / database.
 */
public class BookingPanel extends JPanel implements Scrollable {

    private static final Color BG = new Color(240, 248, 245);
    private static final Color FOREST = new Color(7, 57, 51);
    private static final Color MINT = new Color(47, 201, 174);
    private static final Color TEAL = new Color(12, 143, 123);
    private static final Color TEXT = new Color(18, 62, 55);
    private static final Color MUTED = new Color(107, 133, 127);
    private static final Color PALE = new Color(225, 247, 241);
    private static final Color LINE = new Color(227, 238, 235);
    private static final Color RED = new Color(205, 92, 104);
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", Locale.forLanguageTag("vi-VN"));
    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm");

    private final User user;
    private final Consumer<Long> goToPayment;
    private final BookingService bookingService = new BookingService();
    private final BookingDAO bookingDAO = new BookingDAO();
    private final CourtService courtService = new CourtService();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final NumberFormat money = NumberFormat.getNumberInstance(
            Locale.forLanguageTag("vi-VN"));

    private final List<Court> courts = new ArrayList<>();
    private final List<Customer> customers = new ArrayList<>();
    private final List<Booking> visibleBookings = new ArrayList<>();
    private List<Booking> dayBookings = List.of();
    private Court selectedCourt;
    private LocalDate selectedDate = LocalDate.now();
    private LocalTime selectedStart;
    private LocalTime selectedEnd;
    private long pendingCourtId = -1;
    private int requestVersion = 0;
    private boolean busy;
    private boolean checkingSlots;
    private boolean slotsFailed;
    private boolean initialLoading = true;

    private final JPanel courtList = new JPanel();
    private final JPanel slotGrid = new JPanel(new GridLayout(0, 3, 8, 8));
    private final JLabel courtCountLabel = new JLabel("Đang tải sân...");
    private final JLabel slotNotice = new JLabel("Chọn sân để xem lịch trống");
    private final JLabel selectedCourtLabel = new JLabel("Chưa chọn sân");
    private final JLabel selectedPriceLabel = new JLabel("-- đ / giờ");
    private final JLabel selectedDateLabel = new JLabel("--");
    private final JLabel selectedTimeLabel = new JLabel("Chưa chọn khung giờ");
    private final JLabel selectedDurationLabel = new JLabel("--");
    private final JLabel totalLabel = new JLabel("0 đ");
    private final JLabel historyCountLabel = new JLabel("Đơn đặt của bạn");
    private final JComboBox<Customer> customerBox = new JComboBox<>();
    private final JComboBox<String> durationBox = new JComboBox<>(
            new String[]{"60 phút", "30 phút"});
    private final JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
    private final CourtPicture summaryPicture = new CourtPicture();
    private final PillButton bookButton = new PillButton("Đặt sân & thanh toán", true);
    private final PillButton refreshButton = new PillButton("Làm mới", false);
    private final PillButton payButton = new PillButton("Thanh toán đơn chọn", false);
    private final PillButton cancelButton = new PillButton("Hủy đơn đã chọn", false);

    private final DefaultTableModel historyModel = new DefaultTableModel(
            new String[]{"Mã đơn", "Sân", "Ngày", "Bắt đầu", "Kết thúc", "Trạng thái"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable historyTable = new JTable(historyModel);

    public BookingPanel() {
        this(null, id -> {});
    }

    public BookingPanel(User user, Consumer<Long> goToPayment) {
        this.user = user;
        this.goToPayment = goToPayment == null ? id -> {} : goToPayment;
        setBackground(BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(24, 23, 35, 23));

        JPanel page = new JPanel();
        page.setOpaque(false);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        add(page, BorderLayout.NORTH);

        JPanel heading = createHeading();
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        page.add(heading);
        page.add(Box.createVerticalStrut(18));

        JPanel steps = createSteps();
        steps.setAlignmentX(Component.LEFT_ALIGNMENT);
        page.add(steps);
        page.add(Box.createVerticalStrut(19));

        JPanel triple = new JPanel(new BorderLayout(13, 0));
        triple.setOpaque(false);
        triple.setAlignmentX(Component.LEFT_ALIGNMENT);
        triple.setPreferredSize(new Dimension(930, 556));
        triple.setMaximumSize(new Dimension(Integer.MAX_VALUE, 556));
        triple.add(createCourtColumn(), BorderLayout.WEST);
        triple.add(createTimeColumn(), BorderLayout.CENTER);
        triple.add(createSummaryColumn(), BorderLayout.EAST);
        page.add(triple);
        page.add(Box.createVerticalStrut(22));

        JPanel history = createHistory();
        history.setAlignmentX(Component.LEFT_ALIGNMENT);
        history.setPreferredSize(new Dimension(930, 290));
        history.setMaximumSize(new Dimension(Integer.MAX_VALUE, 290));
        page.add(history);

        dateSpinner.setValue(Date.from(selectedDate.atStartOfDay(
                ZoneId.systemDefault()).toInstant()));
        dateSpinner.addChangeListener(e -> {
            LocalDate date = ((Date) dateSpinner.getValue()).toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
            if (!selectedDate.equals(date)) {
                selectedDate = date;
                clearTimeSelection();
                loadAvailability();
            }
        });
        durationBox.addActionListener(e -> {
            clearTimeSelection();
            drawSlots();
        });
        refreshButton.addActionListener(e -> refreshData());
        bookButton.addActionListener(e -> createBooking());
        payButton.addActionListener(e -> paySelected());
        cancelButton.addActionListener(e -> cancelSelected());
        historyTable.getSelectionModel().addListSelectionListener(e -> updateButtons());

        updateSummary();
        updateButtons();
        if (user != null) refreshData();
        else {
            courtCountLabel.setText("Đăng nhập để xem sân");
            slotNotice.setText("Vui lòng đăng nhập trước khi đặt sân");
        }
    }

    // ======= HEADER =======
    private JPanel createHeading() {
        JPanel p = transparent(new BorderLayout());
        JPanel left = transparent();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(label("TRẢI NGHIỆM TH-MINTON", AppFonts.bold(11f), TEAL));
        left.add(Box.createVerticalStrut(8));
        left.add(label("Đặt sân cầu lông", AppFonts.bold(27f), TEXT));
        left.add(Box.createVerticalStrut(6));
        left.add(label("Chọn sân, chọn giờ và xác nhận lịch chơi của bạn.",
                AppFonts.regular(12f), MUTED));
        p.add(left, BorderLayout.WEST);
        refreshButton.setPreferredSize(new Dimension(112, 39));
        JPanel right = transparent(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        right.add(refreshButton);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    private JPanel createSteps() {
        JPanel p = transparent(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.add(step("1", "Chọn sân", true));
        p.add(label("────────", AppFonts.regular(11f), LINE));
        p.add(step("2", "Chọn thời gian", true));
        p.add(label("────────", AppFonts.regular(11f), LINE));
        p.add(step("3", "Xác nhận", false));
        return p;
    }

    private JPanel step(String number, String name, boolean active) {
        JPanel p = transparent(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JLabel circle = label(number, AppFonts.bold(11f), active ? FOREST : MUTED);
        circle.setOpaque(true);
        circle.setBackground(active ? PALE : new Color(232, 240, 238));
        circle.setHorizontalAlignment(SwingConstants.CENTER);
        circle.setPreferredSize(new Dimension(25, 25));
        p.add(circle);
        p.add(label(name, AppFonts.medium(11f), active ? TEXT : MUTED));
        return p;
    }

    // ======= LEFT: SAN =======
    private JPanel createCourtColumn() {
        RoundedCard shell = new RoundedCard(Color.WHITE, 21);
        shell.setPreferredSize(new Dimension(196, 550));
        shell.setLayout(new BorderLayout(0, 14));
        shell.setBorder(new EmptyBorder(18, 13, 15, 13));

        JPanel title = transparent();
        title.setLayout(new BoxLayout(title, BoxLayout.Y_AXIS));
        title.add(label("1. Chọn sân", AppFonts.bold(15f), TEXT));
        title.add(Box.createVerticalStrut(5));
        courtCountLabel.setFont(AppFonts.regular(10.5f));
        courtCountLabel.setForeground(MUTED);
        title.add(courtCountLabel);
        shell.add(title, BorderLayout.NORTH);

        courtList.setOpaque(false);
        courtList.setLayout(new BoxLayout(courtList, BoxLayout.Y_AXIS));
        JScrollPane scroll = plainScroll(courtList);
        shell.add(scroll, BorderLayout.CENTER);
        return shell;
    }

    private void drawCourts() {
        courtList.removeAll();
        courtCountLabel.setText(courts.size() + " sân đang hoạt động");
        for (Court c : courts) {
            CourtTile tile = new CourtTile(c, selectedCourt != null && selectedCourt.id() == c.id());
            tile.setAlignmentX(Component.LEFT_ALIGNMENT);
            tile.setMaximumSize(new Dimension(Integer.MAX_VALUE, 93));
            tile.setPreferredSize(new Dimension(168, 93));
            tile.addActionListener(e -> chooseCourt(c));
            courtList.add(tile);
            courtList.add(Box.createVerticalStrut(8));
        }
        if (courts.isEmpty()) {
            JLabel empty = label(initialLoading ? "Đang tải dữ liệu..." : "Không có sân hoạt động",
                    AppFonts.regular(11f), MUTED);
            courtList.add(empty);
        }
        courtList.revalidate();
        courtList.repaint();
    }

    private void chooseCourt(Court c) {
        if (c == null || busy) return;
        selectedCourt = c;
        clearTimeSelection();
        drawCourts();
        updateSummary();
        loadAvailability();
    }

    // ======= CENTER: NGAY & GIO =======
    private JPanel createTimeColumn() {
        RoundedCard shell = new RoundedCard(Color.WHITE, 21);
        shell.setLayout(new BorderLayout(0, 10));
        shell.setBorder(new EmptyBorder(18, 15, 17, 15));

        JPanel header = transparent();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(label("2. Chọn thời gian", AppFonts.bold(15f), TEXT));
        header.add(Box.createVerticalStrut(11));

        JPanel dateRow = transparent(new BorderLayout(6, 0));
        JButton previous = dateArrow("‹");
        JButton next = dateArrow("›");
        previous.addActionListener(e -> shiftDate(-1));
        next.addActionListener(e -> shiftDate(1));
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "dd/MM/yyyy"));
        dateSpinner.setFont(AppFonts.medium(13f));
        dateSpinner.setBorder(BorderFactory.createLineBorder(LINE));
        dateRow.add(previous, BorderLayout.WEST);
        dateRow.add(dateSpinner, BorderLayout.CENTER);
        dateRow.add(next, BorderLayout.EAST);
        header.add(dateRow);
        header.add(Box.createVerticalStrut(10));

        JPanel intervalRow = transparent(new BorderLayout());
        intervalRow.add(label("Chọn giờ trống bên dưới", AppFonts.regular(11f), MUTED),
                BorderLayout.WEST);
        durationBox.setFont(AppFonts.medium(11f));
        durationBox.setPreferredSize(new Dimension(100, 29));
        intervalRow.add(durationBox, BorderLayout.EAST);
        header.add(intervalRow);
        shell.add(header, BorderLayout.NORTH);

        slotGrid.setOpaque(false);
        JPanel slotWrap = transparent(new BorderLayout());
        slotWrap.setBorder(new EmptyBorder(4, 0, 5, 0));
        slotWrap.add(slotGrid, BorderLayout.NORTH);
        JScrollPane slotScroll = plainScroll(slotWrap);
        slotScroll.getViewport().addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                int available = slotScroll.getViewport().getWidth();
                int count = available < 350 ? 2 : 3;
                GridLayout layout = (GridLayout) slotGrid.getLayout();
                if (layout.getColumns() != count) {
                    slotGrid.setLayout(new GridLayout(0, count, 8, 8));
                    slotGrid.revalidate();
                }
            }
        });
        shell.add(slotScroll, BorderLayout.CENTER);

        JPanel legend = transparent();
        legend.setLayout(new BoxLayout(legend, BoxLayout.Y_AXIS));
        JPanel dots = transparent(new FlowLayout(FlowLayout.LEFT, 11, 0));
        dots.add(legendItem("●", "Còn trống", TEAL));
        dots.add(legendItem("●", "Đã đặt", RED));
        dots.add(legendItem("●", "Đang chọn", MINT));
        legend.add(dots);
        legend.add(Box.createVerticalStrut(7));
        slotNotice.setFont(AppFonts.regular(10.5f));
        slotNotice.setForeground(MUTED);
        legend.add(slotNotice);
        shell.add(legend, BorderLayout.SOUTH);
        drawSlots();
        return shell;
    }

    private JPanel legendItem(String symbol, String title, Color color) {
        JPanel p = transparent(new FlowLayout(FlowLayout.LEFT, 3, 0));
        p.add(label(symbol, AppFonts.bold(13f), color));
        p.add(label(title, AppFonts.regular(10f), MUTED));
        return p;
    }

    private JButton dateArrow(String symbol) {
        JButton b = new JButton(symbol);
        b.setPreferredSize(new Dimension(32, 33));
        b.setFont(AppFonts.bold(19f));
        b.setForeground(FOREST);
        b.setBackground(PALE);
        b.setBorder(BorderFactory.createEmptyBorder());
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void shiftDate(int days) {
        LocalDate date = ((Date) dateSpinner.getValue()).toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate().plusDays(days);
        dateSpinner.setValue(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    private int intervalMinutes() {
        return durationBox.getSelectedIndex() == 1 ? 30 : 60;
    }

    private boolean isBlocked(LocalTime from, LocalTime to) {
        for (Booking b : dayBookings) {
            if (!"CANCELLED".equalsIgnoreCase(b.status())
                    && from.isBefore(b.endTime()) && to.isAfter(b.startTime())) return true;
        }
        return false;
    }

    private boolean isPast(LocalTime from) {
        return selectedDate.atTime(from).isBefore(LocalDateTime.now());
    }

    private boolean insideSelection(LocalTime from, LocalTime to) {
        return selectedStart != null && selectedEnd != null
                && !from.isBefore(selectedStart) && !to.isAfter(selectedEnd);
    }

    private void drawSlots() {
        slotGrid.removeAll();
        int minutes = intervalMinutes();
        for (LocalTime from = LocalTime.of(6, 0);
             from.isBefore(LocalTime.of(23, 0));
             from = from.plusMinutes(minutes)) {
            LocalTime to = from.plusMinutes(minutes);
            boolean booked = isBlocked(from, to);
            boolean past = isPast(from);
            boolean picked = insideSelection(from, to);
            SlotButton button = new SlotButton(from.format(TIME_FMT) + " - " + to.format(TIME_FMT),
                    picked, booked, past);
            button.setEnabled(selectedCourt != null && !busy && !checkingSlots
                    && !slotsFailed && !booked && !past);
            LocalTime fromSlot = from;
            LocalTime toSlot = to;
            button.addActionListener(e -> pickSlot(fromSlot, toSlot));
            slotGrid.add(button);
        }
        slotGrid.revalidate();
        slotGrid.repaint();
        updateButtons();
    }

    private void pickSlot(LocalTime from, LocalTime to) {
        if (selectedCourt == null || busy || checkingSlots || slotsFailed) return;
        if (selectedStart == null || selectedEnd == null) {
            selectedStart = from;
            selectedEnd = to;
        } else if (from.equals(selectedEnd)) {
            selectedEnd = to;
        } else if (to.equals(selectedStart)) {
            selectedStart = from;
        } else {
            selectedStart = from;
            selectedEnd = to;
        }
        updateSummary();
        drawSlots();
    }

    private void clearTimeSelection() {
        selectedStart = null;
        selectedEnd = null;
        updateSummary();
    }

    private void loadAvailability() {
        int version = ++requestVersion;
        clearTimeSelection();
        dayBookings = List.of();
        slotsFailed = false;
        if (selectedCourt == null || user == null) {
            checkingSlots = false;
            slotNotice.setText("Hãy chọn một sân trước");
            drawSlots();
            return;
        }
        checkingSlots = true;
        slotNotice.setText("Đang kiểm tra khung giờ đã đặt...");
        drawSlots();
        long courtId = selectedCourt.id();
        LocalDate date = selectedDate;
        new SwingWorker<List<Booking>, Void>() {
            @Override protected List<Booking> doInBackground() throws Exception {
                return bookingDAO.findByCourtAndDate(courtId, date);
            }
            @Override protected void done() {
                if (version != requestVersion) return;
                checkingSlots = false;
                try {
                    dayBookings = get();
                    slotNotice.setText("Bấm các ô liền kề để đặt nhiều giờ liên tiếp.");
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    slotsFailed = true;
                    slotNotice.setText("Lỗi khi tải lịch. Hãy nhấn Làm mới.");
                } catch (ExecutionException | CancellationException ex) {
                    slotsFailed = true;
                    slotNotice.setText("Không đọc được lịch trống. Hãy nhấn Làm mới.");
                }
                drawSlots();
                updateSummary();
            }
        }.execute();
    }

    // ======= RIGHT: TOM TAT =======
    // Can giua tieu de, anh san, ten san va gia; giu thong tin lich can trai.
    private JPanel createSummaryColumn() {
        RoundedCard shell = new RoundedCard(Color.WHITE, 21);
        shell.setPreferredSize(new Dimension(254, 550));
        shell.setLayout(new BorderLayout());
        shell.setBorder(new EmptyBorder(18, 14, 15, 14));

        JPanel content = transparent();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        // Tieu de nam giua toan bo cot, khong bi day lech/cat chu.
        JPanel titleRow = transparent(new BorderLayout());
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        JLabel summaryTitle = label("Thông tin đặt sân", AppFonts.bold(15f), TEXT);
        summaryTitle.setHorizontalAlignment(SwingConstants.CENTER);
        titleRow.add(summaryTitle, BorderLayout.CENTER);
        content.add(titleRow);
        content.add(Box.createVerticalStrut(13));

        // Anh san can giua, co le deu hai ben.
        JPanel pictureRow = transparent(new BorderLayout());
        pictureRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        pictureRow.setBorder(new EmptyBorder(0, 14, 0, 14));
        pictureRow.setPreferredSize(new Dimension(220, 140));
        pictureRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        summaryPicture.setPreferredSize(new Dimension(192, 140));
        pictureRow.add(summaryPicture, BorderLayout.CENTER);
        content.add(pictureRow);
        content.add(Box.createVerticalStrut(13));

        // Ten va gia san o giua, can doc voi anh.
        selectedCourtLabel.setFont(AppFonts.bold(17f));
        selectedCourtLabel.setForeground(TEXT);
        selectedCourtLabel.setHorizontalAlignment(SwingConstants.CENTER);
        selectedCourtLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        selectedCourtLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        content.add(selectedCourtLabel);
        content.add(Box.createVerticalStrut(4));

        selectedPriceLabel.setFont(AppFonts.bold(13f));
        selectedPriceLabel.setForeground(TEAL);
        selectedPriceLabel.setHorizontalAlignment(SwingConstants.CENTER);
        selectedPriceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        selectedPriceLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        content.add(selectedPriceLabel);
        content.add(Box.createVerticalStrut(15));

        JSeparator middleRule = new JSeparator();
        middleRule.setAlignmentX(Component.LEFT_ALIGNMENT);
        middleRule.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        middleRule.setForeground(LINE);
        content.add(middleRule);
        content.add(Box.createVerticalStrut(12));

        // Thong tin ngay/gio can trai de de doc.
        JPanel dateInfo = summaryLine("Ngày đặt", selectedDateLabel);
        dateInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(dateInfo);
        content.add(Box.createVerticalStrut(10));
        JPanel timeInfo = summaryLine("Khung giờ", selectedTimeLabel);
        timeInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(timeInfo);
        content.add(Box.createVerticalStrut(10));
        JPanel durationInfo = summaryLine("Thời lượng", selectedDurationLabel);
        durationInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(durationInfo);

        if (user != null && "ADMIN".equalsIgnoreCase(user.role())) {
            content.add(Box.createVerticalStrut(12));
            JLabel customerTitle = label("Đặt cho khách hàng", AppFonts.medium(11f), MUTED);
            customerTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
            customerTitle.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
            content.add(customerTitle);
            content.add(Box.createVerticalStrut(5));

            customerBox.setFont(AppFonts.regular(11f));
            customerBox.setAlignmentX(Component.LEFT_ALIGNMENT);
            customerBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 31));
            customerBox.setRenderer(new DefaultListCellRenderer() {
                @Override public Component getListCellRendererComponent(JList<?> list, Object value,
                        int index, boolean selected, boolean focus) {
                    super.getListCellRendererComponent(list, value, index, selected, focus);
                    if (value instanceof Customer c) setText("#" + c.id() + " - " + c.fullName());
                    return this;
                }
            });
            content.add(customerBox);
        }
        shell.add(content, BorderLayout.CENTER);

        // Chan cot giu nguyen chuc nang tinh tien va dat san.
        JPanel bottom = transparent();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        JSeparator bottomRule = new JSeparator();
        bottomRule.setAlignmentX(Component.LEFT_ALIGNMENT);
        bottomRule.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        bottomRule.setForeground(LINE);
        bottom.add(bottomRule);
        bottom.add(Box.createVerticalStrut(11));
        JPanel totalRow = transparent(new BorderLayout());
        totalRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        totalRow.add(label("Tổng tiền", AppFonts.bold(12f), TEXT), BorderLayout.WEST);
        totalLabel.setFont(AppFonts.bold(17f));
        totalLabel.setForeground(TEAL);
        totalRow.add(totalLabel, BorderLayout.EAST);
        bottom.add(totalRow);
        bottom.add(Box.createVerticalStrut(10));
        bookButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        bookButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        bookButton.setPreferredSize(new Dimension(210, 44));
        bottom.add(bookButton);
        shell.add(bottom, BorderLayout.SOUTH);
        return shell;
    }

    private JPanel summaryLine(String caption, JLabel value) {
        JPanel p = transparent(new BorderLayout(0, 3));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JLabel k = label(caption, AppFonts.regular(10.5f), MUTED);
        value.setFont(AppFonts.medium(11.2f));
        value.setForeground(TEXT);
        p.add(k, BorderLayout.NORTH);
        p.add(value, BorderLayout.CENTER);
        return p;
    }

    private void updateSummary() {
        selectedDateLabel.setText(selectedDate.format(DATE_FMT));
        selectedCourtLabel.setText(selectedCourt == null ? "Chưa chọn sân" : selectedCourt.name());
        selectedPriceLabel.setText(selectedCourt == null ? "-- đ / giờ"
                : formatMoney(selectedCourt.pricePerHour()) + " / giờ");
        summaryPicture.setCourtId(selectedCourt == null ? -1 : selectedCourt.id());
        if (selectedStart == null || selectedEnd == null) {
            selectedTimeLabel.setText("Chưa chọn khung giờ");
            selectedDurationLabel.setText("--");
            totalLabel.setText("0 đ");
        } else {
            long duration = Duration.between(selectedStart, selectedEnd).toMinutes();
            selectedTimeLabel.setText(selectedStart.format(TIME_FMT) + " - "
                    + selectedEnd.format(TIME_FMT));
            selectedDurationLabel.setText(duration + " phút");
            totalLabel.setText(selectedCourt == null ? "0 đ" : formatMoney(
                    PriceCalculator.calculate(selectedCourt.pricePerHour(), selectedStart, selectedEnd)));
        }
        updateButtons();
    }

    private String formatMoney(BigDecimal amount) {
        return amount == null ? "--" : money.format(amount) + " đ";
    }

    // ======= HISTORY =======
    private JPanel createHistory() {
        RoundedCard card = new RoundedCard(Color.WHITE, 21);
        card.setLayout(new BorderLayout(0, 13));
        card.setBorder(new EmptyBorder(17, 17, 16, 17));
        JPanel h = transparent(new BorderLayout());
        h.add(label("Đơn đặt sân của bạn", AppFonts.bold(17f), TEXT), BorderLayout.WEST);
        historyCountLabel.setFont(AppFonts.regular(11f));
        historyCountLabel.setForeground(MUTED);
        h.add(historyCountLabel, BorderLayout.EAST);
        card.add(h, BorderLayout.NORTH);

        historyTable.setFont(AppFonts.regular(11f));
        historyTable.setRowHeight(33);
        historyTable.setGridColor(LINE);
        historyTable.setShowVerticalLines(false);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.setAutoCreateRowSorter(true);
        historyTable.getTableHeader().setFont(AppFonts.bold(11f));
        historyTable.getTableHeader().setBackground(PALE);
        historyTable.getTableHeader().setForeground(FOREST);
        historyTable.getTableHeader().setReorderingAllowed(false);
        historyTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean selected, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, v, selected, focus, row, col);
                setBorder(new EmptyBorder(0, 8, 0, 8));
                setBackground(selected ? PALE : (row % 2 == 0 ? Color.WHITE : new Color(250, 253, 252)));
                setForeground(TEXT);
                return this;
            }
        });
        card.add(plainScroll(historyTable), BorderLayout.CENTER);
        JPanel actions = transparent(new FlowLayout(FlowLayout.RIGHT, 9, 0));
        cancelButton.setPreferredSize(new Dimension(157, 37));
        payButton.setPreferredSize(new Dimension(178, 37));
        actions.add(cancelButton);
        actions.add(payButton);
        card.add(actions, BorderLayout.SOUTH);
        return card;
    }

    // ======= DATA / INTEGRATION =======
    /** Called from Danh sach san: preselect the clicked court by ID. */
    public void selectCourt(long courtId) {
        if (courtId <= 0) return;
        pendingCourtId = courtId;
        if (!busy && !initialLoading) {
            for (Court c : courts) {
                if (c.id() == courtId) {
                    pendingCourtId = -1;
                    chooseCourt(c);
                    return;
                }
            }
            refreshData();
        }
    }

    /** Refreshes real courts, user booking history and availability from MySQL. */
    public void refreshData() {
        if (user == null || busy) return;
        busy = true;
        updateButtons();
        long oldCourtId = selectedCourt == null ? -1 : selectedCourt.id();
        new SwingWorker<LoadedData, Void>() {
            @Override protected LoadedData doInBackground() throws Exception {
                List<Court> allCourts = courtService.listCourts();
                List<Customer> adminCustomers = "ADMIN".equalsIgnoreCase(user.role())
                        ? customerDAO.findAll() : List.of();
                List<Booking> bookings = bookingService.listVisible(user);
                return new LoadedData(allCourts, adminCustomers, bookings);
            }
            @Override protected void done() {
                busy = false;
                initialLoading = false;
                try {
                    LoadedData data = get();
                    courts.clear();
                    for (Court c : data.courts()) if (c.active()) courts.add(c);
                    customers.clear();
                    customers.addAll(data.customers());
                    Customer oldCustomer = (Customer) customerBox.getSelectedItem();
                    customerBox.removeAllItems();
                    for (Customer c : customers) customerBox.addItem(c);
                    if (oldCustomer != null) {
                        for (int i = 0; i < customerBox.getItemCount(); i++) {
                            if (customerBox.getItemAt(i).id() == oldCustomer.id()) {
                                customerBox.setSelectedIndex(i);
                                break;
                            }
                        }
                    }
                    visibleBookings.clear();
                    visibleBookings.addAll(data.bookings());
                    drawHistory();
                    long targetId = pendingCourtId > 0 ? pendingCourtId : oldCourtId;
                    selectedCourt = null;
                    for (Court c : courts) {
                        if (c.id() == targetId) { selectedCourt = c; break; }
                    }
                    if (selectedCourt == null && pendingCourtId <= 0 && !courts.isEmpty())
                        selectedCourt = courts.get(0);
                    boolean missing = pendingCourtId > 0 && selectedCourt == null;
                    pendingCourtId = -1;
                    drawCourts();
                    updateSummary();
                    loadAvailability();
                    if (missing) JOptionPane.showMessageDialog(BookingPanel.this,
                            "Sân vừa chọn không còn hoạt động, vui lòng chọn sân khác.");
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    showError(ex.getCause());
                    courtCountLabel.setText("Không tải được dữ liệu");
                    slotNotice.setText("Kiểm tra kết nối MySQL rồi nhấn Làm mới.");
                }
                updateButtons();
            }
        }.execute();
    }

    private void drawHistory() {
        historyModel.setRowCount(0);
        historyCountLabel.setText(visibleBookings.size() + " đơn");
        for (Booking b : visibleBookings) {
            String name = courts.stream().filter(c -> c.id() == b.courtId())
                    .map(Court::name).findFirst().orElse("Sân #" + b.courtId());
            historyModel.addRow(new Object[]{b.id(), name, b.bookingDate(),
                    b.startTime(), b.endTime(), b.status()});
        }
        updateButtons();
    }

    private void createBooking() {
        if (user == null || busy || checkingSlots || slotsFailed) return;
        try {
            dateSpinner.commitEdit();
            if (selectedCourt == null) throw new IllegalArgumentException("Hãy chọn sân.");
            if (selectedStart == null || selectedEnd == null)
                throw new IllegalArgumentException("Hãy chọn ít nhất một khung giờ trống.");
            if (selectedDate.atTime(selectedStart).isBefore(LocalDateTime.now()))
                throw new IllegalArgumentException("Không thể đặt sân vào thời điểm đã qua.");
            long customerId = -1;
            if ("ADMIN".equalsIgnoreCase(user.role())) {
                Customer customer = (Customer) customerBox.getSelectedItem();
                if (customer == null) throw new IllegalArgumentException("Hãy chọn khách hàng.");
                customerId = customer.id();
            }
            final Booking booking = new Booking(0, selectedCourt.id(), customerId,
                    selectedDate, selectedStart, selectedEnd, "PENDING");
            BigDecimal amount = PriceCalculator.calculate(
                    selectedCourt.pricePerHour(), selectedStart, selectedEnd);
            int answer = JOptionPane.showConfirmDialog(this,
                    "Sân: " + selectedCourt.name() + "\nNgày: " + selectedDate.format(DATE_FMT)
                            + "\nGiờ: " + selectedStart + " - " + selectedEnd
                            + "\nTổng tiền: " + formatMoney(amount)
                            + "\n\nXác nhận đặt sân và chuyển sang thanh toán?",
                    "TH-MINTON | Xác nhận đặt sân", JOptionPane.YES_NO_OPTION);
            if (answer != JOptionPane.YES_OPTION) return;
            busy = true;
            updateButtons();
            new SwingWorker<Long, Void>() {
                @Override protected Long doInBackground() throws Exception {
                    long resolvedCustomerId = "ADMIN".equalsIgnoreCase(user.role())
                            ? booking.customerId() : bookingService.customerId(user);
                    Booking finalBooking = new Booking(0, booking.courtId(), resolvedCustomerId,
                            booking.bookingDate(), booking.startTime(), booking.endTime(), "PENDING");
                    // BookingService -> BookingDAO van kiem tra trung lich trong transaction.
                    return bookingService.createBooking(user, finalBooking);
                }
                @Override protected void done() {
                    busy = false;
                    updateButtons();
                    try {
                        long id = get();
                        JOptionPane.showMessageDialog(BookingPanel.this,
                                "Đặt sân thành công! Mã đơn #" + id + ".");
                        refreshData();
                        goToPayment.accept(id);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    } catch (ExecutionException ex) {
                        showError(ex.getCause());
                        loadAvailability(); // Lam moi lich neu nguoi khac vua dat khung gio.
                    }
                }
            }.execute();
        } catch (java.text.ParseException ex) {
            JOptionPane.showMessageDialog(this, "Ngày không hợp lệ. Hãy nhập dd/MM/yyyy.");
        } catch (Exception ex) { showError(ex); }
    }

    private Booking pickedBooking() {
        int row = historyTable.getSelectedRow();
        if (row < 0) return null;
        int index = historyTable.convertRowIndexToModel(row);
        return index >= 0 && index < visibleBookings.size() ? visibleBookings.get(index) : null;
    }

    private void paySelected() {
        Booking booking = pickedBooking();
        if (booking == null) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một đơn trong bảng.");
            return;
        }
        if ("CANCELLED".equalsIgnoreCase(booking.status())) {
            JOptionPane.showMessageDialog(this, "Đơn này đã hủy.");
            return;
        }
        goToPayment.accept(booking.id());
    }

    private void cancelSelected() {
        if (busy || user == null) return;
        Booking booking = pickedBooking();
        if (booking == null) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một đơn trong bảng.");
            return;
        }
        if (!"PENDING".equalsIgnoreCase(booking.status())) {
            JOptionPane.showMessageDialog(this, "Chỉ được hủy đơn đang chờ thanh toán.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Hủy đơn #" + booking.id() + "?",
                "Xác nhận hủy", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        busy = true;
        updateButtons();
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() throws Exception {
                return bookingService.cancelBooking(user, booking.id());
            }
            @Override protected void done() {
                busy = false;
                try {
                    if (get()) JOptionPane.showMessageDialog(BookingPanel.this, "Đã hủy đơn.");
                    refreshData();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) { showError(ex.getCause()); }
            }
        }.execute();
    }

    private void updateButtons() {
        bookButton.setEnabled(user != null && !busy && !checkingSlots && !slotsFailed
                && selectedCourt != null && selectedStart != null && selectedEnd != null);
        refreshButton.setEnabled(user != null && !busy);
        Booking picked = pickedBooking();
        payButton.setEnabled(user != null && !busy && picked != null
                && !"CANCELLED".equalsIgnoreCase(picked.status()));
        cancelButton.setEnabled(user != null && !busy && picked != null
                && "PENDING".equalsIgnoreCase(picked.status()));
    }

    private void showError(Throwable ex) {
        String msg = ex == null ? "Lỗi không xác định" : ex.getMessage();
        JOptionPane.showMessageDialog(this,
                (ex instanceof SQLException ? "Lỗi MySQL: " : "Không thể thực hiện: ") + msg,
                "TH-MINTON", JOptionPane.ERROR_MESSAGE);
    }

    private static JPanel transparent() {
        JPanel p = new JPanel(); p.setOpaque(false); return p;
    }
    private static JPanel transparent(LayoutManager layout) {
        JPanel p = new JPanel(layout); p.setOpaque(false); return p;
    }
    private static JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text); l.setFont(font); l.setForeground(color); return l;
    }
    private static JScrollPane plainScroll(Component content) {
        JScrollPane scroll = new JScrollPane(content,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(7, 0));
        return scroll;
    }

    @Override public Dimension getPreferredScrollableViewportSize() { return new Dimension(930, 850); }
    @Override public int getScrollableUnitIncrement(Rectangle visible, int orientation, int direction) { return 21; }
    @Override public int getScrollableBlockIncrement(Rectangle visible, int orientation, int direction) { return 170; }
    @Override public boolean getScrollableTracksViewportWidth() { return true; }
    @Override public boolean getScrollableTracksViewportHeight() { return false; }

    private record LoadedData(List<Court> courts, List<Customer> customers,
                              List<Booking> bookings) {}

    private static class RoundedCard extends JPanel {
        private final Color fill;
        private final int arc;
        RoundedCard(Color fill, int arc) { this.fill = fill; this.arc = arc; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D p = (Graphics2D) g.create();
            p.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            p.setColor(new Color(14, 77, 60, 11));
            p.fillRoundRect(1, 3, Math.max(0, getWidth()-2), Math.max(0, getHeight()-4), arc, arc);
            p.setColor(fill);
            p.fillRoundRect(0, 0, Math.max(0, getWidth()-1), Math.max(0, getHeight()-5), arc, arc);
            p.dispose();
            super.paintComponent(g);
        }
    }

    private static class PillButton extends JButton {
        private final boolean primary;
        private boolean hovered;
        PillButton(String text, boolean primary) {
            super(text);
            this.primary = primary;
            setFont(AppFonts.bold(11.5f));
            setForeground(primary ? Color.WHITE : FOREST);
            setBorder(new EmptyBorder(0, 8, 0, 8));
            setBorderPainted(false); setContentAreaFilled(false);
            setFocusPainted(false); setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
            });
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D p = (Graphics2D) g.create();
            p.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int dx = hovered && isEnabled() ? 4 : 1;
            int dy = hovered && isEnabled() ? 3 : 1;
            p.setColor(!isEnabled() ? new Color(213, 225, 221)
                    : primary ? (hovered ? TEAL : FOREST) : (hovered ? new Color(205, 244, 234) : PALE));
            p.fillRoundRect(dx, dy, Math.max(1,getWidth()-2*dx),
                    Math.max(1,getHeight()-2*dy), 18, 18);
            p.dispose();
            super.paintComponent(g);
        }
    }

    private class CourtTile extends JButton {
        private final Court court;
        private final boolean picked;
        private final Image picture;
        CourtTile(Court court, boolean picked) {
            this.court = court; this.picked = picked; this.picture = loadPhoto(court.id());
            setBorder(null); setBorderPainted(false);
            setContentAreaFilled(false); setFocusPainted(false); setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText(court.name() + " - " + formatMoney(court.pricePerHour()) + " / giờ");
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D p = (Graphics2D) g.create();
            p.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w=getWidth(), h=getHeight()-2;
            p.setColor(picked ? PALE : (getModel().isRollover() ? new Color(245, 252, 249) : new Color(248, 251, 250)));
            p.fillRoundRect(0,0,w-1,h-1,15,15);
            p.setColor(picked ? MINT : LINE);
            p.setStroke(new BasicStroke(picked ? 1.8f : 1f));
            p.drawRoundRect(1,1,w-3,h-3,15,15);
            p.setClip(new RoundRectangle2D.Float(8, 11, 65, 65, 11, 11));
            if (picture != null) drawCover(p, picture, 8, 11, 65, 65);
            else drawCourtArt(p, 8, 11, 65, 65);
            p.setClip(null);
            int x=81;
            p.setFont(AppFonts.bold(12f)); p.setColor(TEXT);
            String name = court.name() == null ? "Sân #" + court.id() : court.name();
            FontMetrics fm=p.getFontMetrics();
            while (fm.stringWidth(name) > Math.max(35, w-x-8) && name.length()>3)
                name = name.substring(0,name.length()-2) + "…";
            p.drawString(name,x,32);
            p.setFont(AppFonts.medium(10.5f)); p.setColor(TEAL);
            p.drawString(money.format(court.pricePerHour())+" đ",x,51);
            p.setFont(AppFonts.regular(9.5f)); p.setColor(MUTED);
            p.drawString(picked ? "● Đang chọn" : "● Còn hoạt động",x,69);
            p.dispose();
        }
    }

    private static class SlotButton extends JButton {
        private final boolean picked, booked, past;
        SlotButton(String text, boolean picked, boolean booked, boolean past) {
            super(text); this.picked=picked; this.booked=booked; this.past=past;
            setFont(AppFonts.medium(10f));
            setBorder(new EmptyBorder(2, 1, 2, 1));
            setBorderPainted(false); setContentAreaFilled(false); setOpaque(false);
            setFocusPainted(false); setPreferredSize(new Dimension(98, 43));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D p=(Graphics2D)g.create();
            p.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill = picked ? MINT : booked ? new Color(254, 232, 235)
                    : past ? new Color(240, 243, 242)
                    : getModel().isRollover() && isEnabled() ? PALE : new Color(245, 250, 248);
            p.setColor(fill);
            int d=(getModel().isRollover() && isEnabled()) ? 2 : 0;
            p.fillRoundRect(d,d,Math.max(1,getWidth()-1-d*2),Math.max(1,getHeight()-1-d*2),12,12);
            p.setFont(getFont());
            p.setColor(picked ? FOREST : booked ? RED : past ? new Color(160,173,168) : TEXT);
            FontMetrics fm=p.getFontMetrics();
            p.drawString(getText(),Math.max(2,(getWidth()-fm.stringWidth(getText()))/2),
                    (getHeight()-fm.getHeight())/2+fm.getAscent());
            p.dispose();
        }
    }

    private static class CourtPicture extends JPanel {
        private long courtId = -1;
        private Image image;
        CourtPicture() { setOpaque(false); }
        void setCourtId(long id) {
            if (courtId == id) return;
            courtId=id;
            image=loadPhoto(id);
            repaint();
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D p=(Graphics2D)g.create();
            p.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            p.setClip(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),16,16));
            if (image != null) drawCover(p,image,0,0,getWidth(),getHeight());
            else drawCourtArt(p,0,0,getWidth(),getHeight());
            p.dispose();
        }
    }

    private static Image loadPhoto(long id) {
        if (id <= 0) return null;
        for (String name : new String[]{"court-"+id+".jpg", "court-"+id+".png", "default.jpg", "default.png"}) {
            URL url = BookingPanel.class.getResource("/images/courts/"+name);
            if (url != null) {
                try { BufferedImage img=ImageIO.read(url); if (img!=null) return img; }
                catch (Exception ignored) { }
            }
        }
        return null;
    }

    private static void drawCover(Graphics2D p, Image image,int x,int y,int w,int h) {
        int iw=image.getWidth(null), ih=image.getHeight(null);
        if (iw<=0||ih<=0||w<=0||h<=0) return;
        double s=Math.max(w/(double)iw,h/(double)ih);
        int dw=(int)Math.ceil(iw*s),dh=(int)Math.ceil(ih*s);
        p.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        p.drawImage(image,x+(w-dw)/2,y+(h-dh)/2,dw,dh,null);
    }

    private static void drawCourtArt(Graphics2D p,int x,int y,int w,int h) {
        p.setPaint(new GradientPaint(x,y,new Color(6,58,56),x+w,y+h,new Color(33,150,121)));
        p.fillRect(x,y,w,h);
        int pad=Math.max(6,Math.min(w,h)/8);
        int tx=x+pad,ty=y+pad,tw=w-pad*2,th=h-pad*2;
        p.setColor(new Color(41,176,143)); p.fillRect(tx,ty,tw,th);
        p.setColor(new Color(235,255,245,210));
        p.setStroke(new BasicStroke(Math.max(1f,w/140f)));
        p.drawRect(tx,ty,tw,th);
        p.drawLine(tx+tw/2,ty,tx+tw/2,ty+th);
        p.drawLine(tx,ty+th/2,tx+tw,ty+th/2);
        p.drawRect(tx+tw/5,ty+th/5,tw*3/5,th*3/5);
    }
}
