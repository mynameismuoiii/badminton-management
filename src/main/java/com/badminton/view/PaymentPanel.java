package com.badminton.view;

import com.badminton.ui.MintDialogs;

import com.badminton.model.Booking;
import com.badminton.model.User;
import com.badminton.service.BookingService;
import com.badminton.service.PaymentService;
import com.badminton.util.AppFonts;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.awt.geom.RoundRectangle2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * TH-MINTON - thanh toan chuyen khoan 3 buoc.
 * Chi thay giao dien; giu nguyen BookingService va PaymentService.
 * Buoc 3 KHONG tu danh dau thanh toan thanh cong.
 */
public class PaymentPanel extends JPanel {
    private static final Color BG = new Color(242, 249, 247);
    private static final Color WHITE = Color.WHITE;
    private static final Color INK = new Color(17, 55, 52);
    private static final Color MUTED = new Color(106, 132, 127);
    private static final Color GREEN = new Color(4, 116, 88);
    private static final Color MINT = new Color(219, 248, 237);
    private static final Color LINE = new Color(222, 232, 229);
    private static final Color AMBER = new Color(252, 243, 217);

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private final User user;
    private final BookingService bookings = new BookingService();
    private final PaymentService payments = new PaymentService();
    private final NumberFormat vnd = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));

    private final RoundedComboBox<BookingChoice> orderBox = new RoundedComboBox<>();
    private final JLabel court1 = label("--", 21f, true, INK);
    private final JLabel date1 = label("--", 15f, false, INK);
    private final JLabel time1 = label("--", 15f, false, INK);
    private final JLabel amount1 = label("--", 24f, true, GREEN);
    private final JLabel status1 = label("--", 14f, false, MUTED);

    private final JLabel court2 = label("--", 20f, true, INK);
    private final JLabel date2 = label("--", 15f, false, INK);
    private final JLabel time2 = label("--", 15f, false, INK);
    private final JLabel amount2 = label("--", 24f, true, GREEN);
    private final JLabel transferNote = label("--", 16f, true, INK);
    private final JLabel qrImage = new JLabel("", SwingConstants.CENTER);

    private final JLabel resultIcon = label("...", 37f, true, GREEN);
    private final JLabel resultTitle = label("--", 23f, true, INK);
    private final JLabel resultText = label("--", 15f, false, MUTED);
    private final JLabel resultOrder = label("--", 15f, false, INK);
    private final JLabel amount3 = label("--", 22f, true, GREEN);

    private final NavButton nextButton = new NavButton("Tiếp tục thanh toán", true);
    private final NavButton back2 = new NavButton("Quay lại", false);
    private final NavButton reportButton = new NavButton("Tôi đã thanh toán", true);
    private final NavButton back3 = new NavButton("Xem thông tin đơn", false);
    private final NavButton confirmButton = new NavButton("Admin: Xác nhận đã nhận tiền", true);
    private final NavButton refreshButton = new NavButton("Làm mới trạng thái", false);
    private final NavButton copyButton = new NavButton("Sao chép", false);
    private final NavButton reloadButton = new NavButton("Tải lại danh sách", false);

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel pages = new JPanel(cardLayout) {
        @Override public Dimension getPreferredSize() {
            for (Component c : getComponents()) {
                if (c.isVisible()) return c.getPreferredSize();
            }
            return super.getPreferredSize();
        }
    };
    private final JLabel[] stepNumbers = new JLabel[3];
    private final JLabel[] stepTitles = new JLabel[3];

    private Set<Long> pending = Set.of();
    private Set<Long> paid = Set.of();
    private boolean busy;
    private boolean filling;
    private boolean refreshAgain;
    private long focusId = -1;
    private int step = 1;

    public PaymentPanel() { this(null); }

    public PaymentPanel(User user) {
        this.user = user;
        setLayout(new BorderLayout(0, 18));
        setBackground(BG);
        setBorder(new EmptyBorder(21, 23, 21, 23));

        JPanel heading = new JPanel(new BorderLayout(0, 16));
        heading.setOpaque(false);
        heading.add(label("THANH TOÁN ĐẶT SÂN", 24f, true, INK), BorderLayout.NORTH);
        heading.add(buildStepper(), BorderLayout.CENTER);
        add(heading, BorderLayout.NORTH);

        pages.setOpaque(false);
        pages.add(buildOrderStep(), "1");
        pages.add(buildTransferStep(), "2");
        pages.add(buildResultStep(), "3");
        JScrollPane scroll = new JScrollPane(pages);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        orderBox.setFont(AppFonts.medium(14f));
        orderBox.setBackground(WHITE);
        orderBox.addActionListener(e -> updateSelection());
        nextButton.addActionListener(e -> {
            if (selectedBooking() == null) return;
            setStep(isAwaitingOrPaid(selectedBooking()) ? 3 : 2);
        });
        back2.addActionListener(e -> setStep(1));
        back3.addActionListener(e -> setStep(1));
        reportButton.addActionListener(e -> reportTransfer());
        confirmButton.addActionListener(e -> confirmTransfer());
        refreshButton.addActionListener(e -> refreshData());
        reloadButton.addActionListener(e -> refreshData());
        copyButton.addActionListener(e -> copyTransferNote());

        confirmButton.setVisible(isAdmin());
        loadQR();
        qrImage.setToolTipText("Nhấn để phóng to mã QR");
        qrImage.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        qrImage.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { showLargeQR(); }
        });
        setStep(1);
        if (user != null) refreshData();
        else {
            status1.setText("Vui lòng đăng nhập để thanh toán.");
            updateButtons();
        }
    }

    /** MainFrame va RoleBasedMainFrame dang goi ham nay. */
    public void focusBooking(long bookingId) {
        focusId = bookingId;
        setStep(1);
        refreshData();
    }

    // Khoi thanh 3 buoc phia tren
    private JPanel buildStepper() {
        RoundedPanel rail = new RoundedPanel(WHITE, 18);
        rail.setLayout(new GridLayout(1, 3, 6, 0));
        rail.setBorder(new EmptyBorder(16, 18, 16, 18));
        String[] names = {"Thông tin đơn", "Chuyển khoản QR", "Xác nhận"};
        for (int i = 0; i < 3; i++) {
            JPanel cell = new JPanel(new FlowLayout(FlowLayout.CENTER, 9, 0));
            cell.setOpaque(false);
            JLabel no = new StepCircle(String.valueOf(i + 1));
            no.setPreferredSize(new Dimension(30, 30));
            no.setFont(AppFonts.bold(14f));
            JLabel caption = label(names[i], 14f, true, MUTED);
            stepNumbers[i] = no;
            stepTitles[i] = caption;
            cell.add(no);
            cell.add(caption);
            rail.add(cell);
        }
        return rail;
    }

    // BUOC 1: chon va kiem tra don
    private JPanel buildOrderStep() {
        JPanel row = columns(1, 1);
        RoundedPanel left = card();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(label("Thông tin đơn đặt sân", 20f, true, INK));
        left.add(vgap(17));
        left.add(label("Chọn đơn cần thanh toán", 13f, true, MUTED));
        left.add(vgap(8));
        orderBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 43));
        orderBox.setPreferredSize(new Dimension(300, 43));
        orderBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(orderBox);
        left.add(vgap(18));
        left.add(separator());
        left.add(vgap(15));
        left.add(label("SÂN ĐÃ CHỌN", 12f, true, MUTED));
        left.add(vgap(9));
        left.add(court1);
        left.add(vgap(15));
        left.add(infoLine("Ngày đặt", date1));
        left.add(vgap(8));
        left.add(infoLine("Giờ chơi", time1));
        left.add(vgap(15));
        left.add(separator());
        left.add(vgap(18));
        left.add(infoLine("Tổng thanh toán", amount1));
        left.add(vgap(10));
        left.add(status1);
        left.add(vgap(20));
        left.add(alignLeft(nextButton));

        RoundedPanel right = card();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.add(label("Quy trình thanh toán", 20f, true, INK));
        right.add(vgap(20));
        right.add(hint("01", "Kiểm tra đơn", "Xác nhận sân, ngày, giờ và số tiền."));
        right.add(vgap(17));
        right.add(hint("02", "Quét mã QR BIDV", "Dùng ứng dụng ngân hàng để chuyển khoản."));
        right.add(vgap(17));
        right.add(hint("03", "Chờ xác nhận", "Admin kiểm tra tiền về trước khi xác nhận."));
        right.add(vgap(18));
        right.add(alignLeft(reloadButton));
        row.add(left);
        row.add(right);
        return withScrollSpace(row);
    }

    // BUOC 2: QR va noi dung chuyen khoan
    private JPanel buildTransferStep() {
        JPanel row = columns(1, 1);
        RoundedPanel left = card();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(label("Thông tin đơn đặt sân", 20f, true, INK));
        left.add(vgap(20));
        left.add(court2);
        left.add(vgap(14));
        left.add(infoLine("Ngày đặt", date2));
        left.add(vgap(10));
        left.add(infoLine("Giờ chơi", time2));
        left.add(vgap(20));
        left.add(separator());
        left.add(vgap(16));
        left.add(infoLine("Tổng thanh toán", amount2));
        left.add(vgap(21));
        left.add(infoBox("Lưu ý", "Kiểm tra chính xác người nhận, số tiền và nội dung trên ứng dụng ngân hàng trước khi chuyển."));
        left.add(vgap(22));
        left.add(alignLeft(back2));

        RoundedPanel right = card();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.add(label("Quét mã QR để chuyển khoản", 20f, true, INK));
        right.add(vgap(9));
        right.add(label("Chuyển khoản ngân hàng BIDV / VietQR", 13f, false, MUTED));
        right.add(vgap(13));
        qrImage.setAlignmentX(Component.CENTER_ALIGNMENT);
        qrImage.setPreferredSize(new Dimension(202, 202));
        qrImage.setMinimumSize(new Dimension(202, 202));
        qrImage.setMaximumSize(new Dimension(220, 202));
        right.add(qrImage);
        right.add(vgap(14));
        right.add(label("Nội dung chuyển khoản", 13f, false, MUTED));
        right.add(vgap(5));
        right.add(transferNote);
        right.add(vgap(10));
        right.add(alignLeft(copyButton));
        right.add(vgap(9));
        right.add(infoBox("Sau khi chuyển khoản", "Bấm 'Tôi đã thanh toán' để gửi thông báo cho Admin đối chiếu."));
        right.add(vgap(10));
        right.add(alignLeft(reportButton));
        row.add(left);
        row.add(right);
        return withScrollSpace(row);
    }

    // BUOC 3: dang doi / da xac nhan
    private JPanel buildResultStep() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setOpaque(false);
        RoundedPanel result = card();
        result.setLayout(new BoxLayout(result, BoxLayout.Y_AXIS));
        result.setPreferredSize(new Dimension(690, 450));
        resultIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        resultTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        resultText.setAlignmentX(Component.CENTER_ALIGNMENT);
        resultOrder.setAlignmentX(Component.CENTER_ALIGNMENT);
        amount3.setAlignmentX(Component.CENTER_ALIGNMENT);
        result.add(vgap(20));
        result.add(resultIcon);
        result.add(vgap(17));
        result.add(resultTitle);
        result.add(vgap(14));
        result.add(resultText);
        result.add(vgap(25));
        result.add(separator());
        result.add(vgap(22));
        result.add(resultOrder);
        result.add(vgap(12));
        result.add(amount3);
        result.add(Box.createVerticalGlue());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 9, 0));
        buttons.setOpaque(false);
        buttons.add(back3);
        buttons.add(refreshButton);
        buttons.add(confirmButton);
        result.add(buttons);
        result.add(vgap(16));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.NONE;
        g.anchor = GridBagConstraints.NORTH;
        outer.add(result, g);
        return withScrollSpace(outer);
    }

    private void setStep(int newStep) {
        step = Math.max(1, Math.min(3, newStep));
        cardLayout.show(pages, String.valueOf(step));
        pages.revalidate();
        for (int i = 0; i < 3; i++) {
            boolean active = i + 1 <= step;
            stepNumbers[i].setBackground(active ? GREEN : BG);
            stepNumbers[i].setForeground(active ? WHITE : MUTED);
            stepTitles[i].setForeground(active ? GREEN : MUTED);
        }
        updateButtons();
    }

    private boolean isAdmin() {
        return user != null && "ADMIN".equalsIgnoreCase(user.role());
    }

    private Booking selectedBooking() {
        Object item = orderBox.getSelectedItem();
        return item instanceof BookingChoice c ? c.booking : null;
    }

    private boolean isPaid(Booking b) {
        return b != null && (paid.contains(b.id()) || "CONFIRMED".equals(b.status()));
    }

    private boolean isAwaitingOrPaid(Booking b) {
        return b != null && (isPaid(b) || pending.contains(b.id()));
    }

    private void updateButtons() {
        Booking b = selectedBooking();
        boolean valid = user != null && b != null;
        nextButton.setEnabled(valid && !busy);
        nextButton.setText(isAwaitingOrPaid(b) ? "Xem trạng thái đơn" : "Tiếp tục thanh toán");
        reportButton.setEnabled(valid && !busy && !isAwaitingOrPaid(b) && "PENDING".equals(b.status()));
        confirmButton.setEnabled(valid && !busy && isAdmin() && pending.contains(b.id()) && !isPaid(b));
        refreshButton.setEnabled(user != null && !busy);
        reloadButton.setEnabled(user != null && !busy);
        back2.setEnabled(!busy);
        back3.setEnabled(!busy);
        copyButton.setEnabled(valid && !busy);
        orderBox.setEnabled(user != null && !busy);
    }

    private void setBusy(boolean value) {
        busy = value;
        updateButtons();
    }

    /** Giu nguyen hop dong ham duoc MainFrame su dung. */
    public void refreshData() {
        if (user == null) return;
        if (busy) { refreshAgain = true; return; }
        Booking current = selectedBooking();
        final long idToKeep = focusId > 0 ? focusId : (current == null ? -1 : current.id());
        setBusy(true);
        new SwingWorker<Data, Void>() {
            @Override protected Data doInBackground() throws Exception {
                return new Data(bookings.listVisible(user), payments.pendingReports(), payments.paidBookingIds());
            }
            @Override protected void done() {
                try {
                    Data data = get();
                    pending = data.pending;
                    paid = data.paid;
                    filling = true;
                    orderBox.removeAllItems();
                    int match = -1;
                    for (Booking b : data.orders) {
                        if ("CANCELLED".equals(b.status())) continue;
                        orderBox.addItem(new BookingChoice(b));
                        if (b.id() == idToKeep) match = orderBox.getItemCount() - 1;
                    }
                    if (match >= 0) orderBox.setSelectedIndex(match);
                    filling = false;
                    if (focusId == idToKeep) focusId = -1;
                    updateSelection();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    error(e.getCause());
                } finally {
                    filling = false;
                    setBusy(false);
                    if (refreshAgain) {
                        refreshAgain = false;
                        refreshData();
                    }
                }
            }
        }.execute();
    }

    private void updateSelection() {
        if (filling) return;
        Booking b = selectedBooking();
        if (b == null) {
            court1.setText("Chưa có đơn đặt sân");
            court2.setText("Chưa có đơn đặt sân");
            date1.setText("--"); date2.setText("--");
            time1.setText("--"); time2.setText("--");
            amount1.setText("--"); amount2.setText("--"); amount3.setText("--");
            transferNote.setText("--");
            status1.setText("Không có đơn đặt sân khả dụng.");
            resultTitle.setText("Chưa có đơn đặt sân");
            resultText.setText("Hãy đặt sân trước khi thanh toán.");
            updateButtons();
            return;
        }
        String court = "Sân " + b.courtId();
        String date = b.bookingDate().format(DATE);
        String time = b.startTime().format(TIME) + " - " + b.endTime().format(TIME);
        court1.setText(court); court2.setText(court);
        date1.setText(date); date2.setText(date);
        time1.setText(time); time2.setText(time);
        transferNote.setText("THMINTON " + b.id());
        resultOrder.setText("Đơn #" + b.id() + "   |   " + court + "   |   " + date + "   |   " + time);
        amount1.setText("Đang tải...");
        amount2.setText("Đang tải...");
        amount3.setText("Đang tải...");
        updateStatus(b);
        updateButtons();

        final long requestedId = b.id();
        new SwingWorker<BigDecimal, Void>() {
            @Override protected BigDecimal doInBackground() throws Exception { return payments.amount(b); }
            @Override protected void done() {
                Booking stillSelected = selectedBooking();
                if (stillSelected == null || stillSelected.id() != requestedId) return;
                String price;
                try { price = vnd.format(get()) + " VND"; }
                catch (Exception ex) { price = "Không tải được giá"; }
                amount1.setText(price);
                amount2.setText(price);
                amount3.setText(price);
            }
        }.execute();
    }

    private void updateStatus(Booking b) {
        if (isPaid(b)) {
            status1.setText("Đã xác nhận thanh toán");
            resultIcon.setText("✓");
            resultIcon.setForeground(GREEN);
            resultTitle.setText("Thanh toán thành công!");
            resultText.setText("Admin đã xác nhận nhận được khoản chuyển tiền.");
        } else if (pending.contains(b.id())) {
            status1.setText("Đã báo chuyển khoản - chờ Admin kiểm tra");
            resultIcon.setText("◷");
            resultIcon.setForeground(new Color(180, 122, 25));
            resultTitle.setText("Đang chờ xác nhận");
            resultText.setText("Thông báo đã gửi. Admin sẽ đối chiếu và xác nhận tiền về.");
        } else {
            status1.setText("Chưa báo chuyển khoản");
            resultIcon.setText("i");
            resultIcon.setForeground(MUTED);
            resultTitle.setText("Chưa gửi thông báo thanh toán");
            resultText.setText("Hãy hoàn tất bước chuyển khoản trước.");
        }
    }

    private void reportTransfer() {
        Booking b = selectedBooking();
        if (b == null || busy || isAwaitingOrPaid(b) || !"PENDING".equals(b.status())) return;
        int answer = MintDialogs.showConfirmDialog(this,
            "Bạn xác nhận đã thực hiện chuyển khoản cho đơn #" + b.id() + "?\n"
                + "Nội dung: THMINTON " + b.id() + "\n"
                + "Thông báo này KHÔNG tự động xác nhận đã nhận tiền.",
            "Xác nhận đã chuyển khoản", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) return;
        setBusy(true);
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                payments.reportTransfer(user, b.id());
                return null;
            }
            @Override protected void done() {
                setBusy(false);
                try {
                    get();
                    pending = new java.util.HashSet<>(pending);
                    pending.add(b.id());
                    updateStatus(b);
                    updateButtons();
                    setStep(3);
                    refreshData();
                } catch (Exception ex) { error(unwrap(ex)); }
            }
        }.execute();
    }

    private void confirmTransfer() {
        Booking b = selectedBooking();
        if (b == null || busy || !isAdmin() || !pending.contains(b.id()) || isPaid(b)) return;
        int answer = MintDialogs.showConfirmDialog(this,
            "Chỉ xác nhận khi bạn đã đối chiếu sao kê và tiền ĐÃ VỀ!\n"
                + "Xác nhận thanh toán cho đơn #" + b.id() + "?",
            "Admin xác nhận tiền", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) return;
        setBusy(true);
        new SwingWorker<Long, Void>() {
            @Override protected Long doInBackground() throws Exception {
                return payments.confirmTransfer(user, b.id());
            }
            @Override protected void done() {
                setBusy(false);
                try {
                    long paymentId = get();
                    paid = new java.util.HashSet<>(paid);
                    paid.add(b.id());
                    updateStatus(b);
                    updateButtons();
                    MintDialogs.showMessageDialog(PaymentPanel.this,
                        "Đã xác nhận thanh toán. Mã giao dịch #" + paymentId);
                    refreshData();
                } catch (Exception ex) { error(unwrap(ex)); }
            }
        }.execute();
    }

    private void copyTransferNote() {
        Booking b = selectedBooking();
        if (b == null) return;
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                new StringSelection("THMINTON " + b.id()), null);
            MintDialogs.showMessageDialog(this, "Đã sao chép nội dung chuyển khoản.");
        } catch (Exception ex) {
            MintDialogs.showMessageDialog(this, "Không thể sao chép: " + ex.getMessage());
        }
    }

    private void loadQR() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("images/transfer-qr.png")) {
            if (in == null) {
                qrImage.setText("Chưa có ảnh: images/transfer-qr.png");
                return;
            }
            BufferedImage image = ImageIO.read(in);
            if (image == null) throw new IllegalArgumentException("Ảnh QR không hợp lệ.");
            // Giu ti le, tranh meo va cat anh QR.
            int w = image.getWidth(), h = image.getHeight();
            double ratio = Math.min(202.0 / w, 202.0 / h);
            Image scaled = image.getScaledInstance(Math.max(1, (int)(w * ratio)),
                Math.max(1, (int)(h * ratio)), Image.SCALE_SMOOTH);
            qrImage.setIcon(new ImageIcon(scaled));
            qrImage.setText("");
        } catch (Exception ex) {
            qrImage.setText("Không đọc được ảnh QR: " + ex.getMessage());
        }
    }

    private void showLargeQR() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("images/transfer-qr.png")) {
            if (in == null) return;
            BufferedImage original = ImageIO.read(in);
            if (original == null) return;
            int w = original.getWidth(), h = original.getHeight();
            double ratio = Math.min(480.0 / w, 480.0 / h);
            Image resized = original.getScaledInstance(
                Math.max(1, (int) (w * ratio)), Math.max(1, (int) (h * ratio)), Image.SCALE_SMOOTH);
            MintDialogs.showMessageDialog(this, new JLabel(new ImageIcon(resized)),
                "QR chuyển khoản - TH-MINTON", JOptionPane.PLAIN_MESSAGE);
        } catch (Exception ex) { error(ex); }
    }

    private static Throwable unwrap(Throwable ex) {
        return ex instanceof ExecutionException && ex.getCause() != null ? ex.getCause() : ex;
    }

    private void error(Throwable ex) {
        MintDialogs.showMessageDialog(this,
            "Không thực hiện được: " + (ex == null ? "Lỗi không xác định" : ex.getMessage()),
            "TH-MINTON", JOptionPane.ERROR_MESSAGE);
    }

    // ===== CAC THANH PHAN GIAO DIEN DUNG CHUNG =====
    private static JLabel label(String value, float size, boolean bold, Color color) {
        JLabel l = new JLabel(value);
        l.setFont(bold ? AppFonts.bold(size) : AppFonts.regular(size));
        l.setForeground(color);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private static RoundedPanel card() {
        RoundedPanel card = new RoundedPanel(WHITE, 20);
        card.setBorder(new EmptyBorder(24, 25, 24, 25));
        return card;
    }

    private static JPanel columns(int w1, int w2) {
        return new WeightedColumns(w1, w2);
    }

    private static JPanel withScrollSpace(JPanel p) {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(BG);
        outer.setBorder(new EmptyBorder(2, 2, 6, 2));
        outer.add(p, BorderLayout.CENTER);
        return outer;
    }

    private static Component vgap(int height) { return Box.createVerticalStrut(height); }

    private static JSeparator separator() {
        JSeparator line = new JSeparator();
        line.setForeground(LINE);
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return line;
    }

    private static JPanel infoLine(String caption, JLabel value) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(label(caption, 14f, false, MUTED), BorderLayout.WEST);
        p.add(value, BorderLayout.EAST);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 33));
        return p;
    }

    private static JPanel hint(String number, String title, String desc) {
        JPanel result = new JPanel(new BorderLayout(14, 0));
        result.setOpaque(false);
        JLabel circle = label(number, 20f, true, GREEN);
        result.add(circle, BorderLayout.WEST);
        JPanel copy = new JPanel();
        copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
        copy.setOpaque(false);
        copy.add(label(title, 16f, true, INK));
        copy.add(vgap(5));
        copy.add(label(desc, 13f, false, MUTED));
        result.add(copy, BorderLayout.CENTER);
        result.setAlignmentX(Component.LEFT_ALIGNMENT);
        result.setMaximumSize(new Dimension(Integer.MAX_VALUE, 67));
        return result;
    }

    private static JPanel infoBox(String title, String content) {
        RoundedPanel p = new RoundedPanel(AMBER, 13);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(13, 14, 13, 14));
        p.add(label(title, 14f, true, INK));
        p.add(vgap(6));
        JLabel detail = label("<html><div style='width:270px'>" + content + "</div></html>", 13f, false, INK);
        p.add(detail);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    private static JComponent alignLeft(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    private record Data(List<Booking> orders, Set<Long> pending, Set<Long> paid) { }

    private static final class BookingChoice {
        final Booking booking;
        BookingChoice(Booking booking) { this.booking = booking; }
        @Override public String toString() {
            return "#" + booking.id() + " | Sân " + booking.courtId() + " | "
                + booking.bookingDate().format(DATE) + " | "
                + booking.startTime().format(TIME) + " - " + booking.endTime().format(TIME);
        }
    }

    /** 2 cot responsive theo ti le 2:1 hoac 1:1 */
    private static final class WeightedColumns extends JPanel {
        private final int w1, w2;
        WeightedColumns(int w1, int w2) {
            super(new GridBagLayout());
            this.w1 = w1; this.w2 = w2;
            setOpaque(false);
        }
        @Override public Component add(Component comp) {
            GridBagConstraints g = new GridBagConstraints();
            g.gridy = 0;
            g.gridx = getComponentCount();
            g.weightx = g.gridx == 0 ? w1 : w2;
            g.weighty = 1;
            g.fill = GridBagConstraints.BOTH;
            g.insets = g.gridx == 0 ? new Insets(0, 0, 0, 13) : new Insets(0, 0, 0, 0);
            super.add(comp, g);
            return comp;
        }
    }

    private static final class StepCircle extends JLabel {
        StepCircle(String number) {
            super(number, SwingConstants.CENTER);
            setOpaque(false);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D p = (Graphics2D) g.create();
            p.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            p.setColor(getBackground());
            p.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
            p.dispose();
            super.paintComponent(g);
        }
    }

    private static final class RoundedPanel extends JPanel {
        private final Color fill;
        private final int radius;
        RoundedPanel(Color color, int radius) {
            this.fill = color;
            this.radius = radius;
            setOpaque(false);
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D p = (Graphics2D)g.create();
            p.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            p.setColor(fill);
            p.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            if (WHITE.equals(fill)) {
                p.setColor(LINE);
                p.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            }
            p.dispose();
        }
    }

    /**
     * Round-corner order selector. Uses the normal JComboBox model,
     * ActionListeners and keyboard/popup handling from Swing.
     */
    private static final class RoundedComboBox<T> extends JComboBox<T> {
        RoundedComboBox() {
            setOpaque(false);
            setBackground(WHITE);
            setForeground(INK);
            setFont(AppFonts.medium(14f));
            setBorder(new EmptyBorder(0, 13, 0, 5));
            setFocusable(true);
            // Do not install RoundedComboUI here: it has a square BasicComboPopup.
            // The shared MintDropdown UI paints both the arrow and the popup.
            setRenderer(new DefaultListCellRenderer() {
                @Override public Component getListCellRendererComponent(
                        JList<?> list, Object value, int index,
                        boolean selected, boolean focus) {
                    super.getListCellRendererComponent(list, value, index, selected, focus);
                    setFont(AppFonts.medium(13.5f));
                    setBorder(new EmptyBorder(7, 10, 7, 8));
                    // Current value must not paint a square on the round surface.
                    setOpaque(index >= 0);
                    if (index < 0) setForeground(INK);
                    else if (selected) {
                        setBackground(MINT);
                        setForeground(INK);
                    } else {
                        setBackground(WHITE);
                        setForeground(INK);
                    }
                    return this;
                }
            });
            MintDropdown.install(this);
        }

        @Override public void addNotify() {
            super.addNotify();
            // A Look & Feel refresh can reset the combo UI while recreating screens.
            // Reinstall *after* attaching to the window if that happened.
            if (!getUI().getClass().getName().startsWith(MintDropdown.class.getName() + "$")) {
                MintDropdown.install(this);
            }
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            if (w <= 1 || h <= 1) { g2.dispose(); return; }
            g2.setColor(WHITE);
            g2.fillRoundRect(0, 0, w - 1, h - 1, 17, 17);
            g2.setColor(isFocusOwner() ? GREEN : LINE);
            g2.setStroke(new BasicStroke(isFocusOwner() ? 1.5f : 1f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, 17, 17);
            g2.clip(new RoundRectangle2D.Float(2, 2, w - 4, h - 4, 16, 16));
            super.paintComponent(g2);
            g2.dispose();
        }
    }

    private static final class RoundedComboUI extends BasicComboBoxUI {
        @Override protected JButton createArrowButton() {
            JButton arrow = new JButton() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    int w = getWidth(), h = getHeight();
                    g2.setColor(isEnabled() ? MINT : BG);
                    g2.fillRoundRect(2, 5, Math.max(1, w - 6),
                            Math.max(1, h - 10), 10, 10);
                    g2.setColor(isEnabled() ? GREEN : MUTED);
                    g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_ROUND));
                    int x = w / 2, y = h / 2;
                    g2.drawLine(x - 5, y - 2, x, y + 3);
                    g2.drawLine(x, y + 3, x + 5, y - 2);
                    g2.dispose();
                }
            };
            arrow.setOpaque(false);
            arrow.setBorder(BorderFactory.createEmptyBorder());
            arrow.setContentAreaFilled(false);
            arrow.setFocusPainted(false);
            arrow.setPreferredSize(new Dimension(39, 37));
            arrow.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return arrow;
        }

        @Override public void paintCurrentValueBackground(Graphics g, Rectangle r, boolean focus) {
            // The enclosing RoundedComboBox draws its own rounded background.
        }
    }

    private static final class NavButton extends JButton {
        private final boolean primary;
        NavButton(String text, boolean primary) {
            super(text);
            this.primary = primary;
            setFont(AppFonts.bold(13.5f));
            setForeground(primary ? WHITE : INK);
            setBackground(primary ? GREEN : BG);
            setBorder(new EmptyBorder(11, 17, 11, 17));
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFocusPainted(false);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D p = (Graphics2D)g.create();
            p.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color color = primary ? GREEN : BG;
            if (!isEnabled()) color = LINE;
            else if (getModel().isRollover()) color = primary ? new Color(0, 91, 69) : MINT;
            p.setColor(color);
            p.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            p.dispose();
            setForeground(!isEnabled() ? MUTED : primary ? WHITE : INK);
            super.paintComponent(g);
        }
    }
}
