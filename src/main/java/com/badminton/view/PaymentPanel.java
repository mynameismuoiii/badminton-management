package com.badminton.view;

import com.badminton.model.Booking;
import com.badminton.model.User;
import com.badminton.service.BookingService;
import com.badminton.service.PaymentService;

import javax.imageio.ImageIO;
import javax.swing.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * N1: QR ngan hang +
 * khach bao da chuyen khoan +
 * admin xac nhan.
 */
public class PaymentPanel extends JPanel {

    private final User user;

    private final BookingService bookingService =
        new BookingService();

    private final PaymentService paymentService =
        new PaymentService();

    private final JComboBox<BookingChoice> orderBox =
        new JComboBox<>();

    private final JLabel amountLabel =
        new JLabel("--");

    private final JLabel contentLabel =
        new JLabel("--");

    private final JLabel statusLabel =
        new JLabel("--");

    private final JLabel qrLabel =
        new JLabel();

    private final JButton refreshButton =
        new JButton("Làm mới");

    private final JButton reportButton =
        new JButton("Tôi đã thanh toán");

    private final JButton confirmButton =
        new JButton(
            "Admin: Xác nhận đã nhận tiền"
        );

    private final NumberFormat money =
        NumberFormat.getNumberInstance(
            Locale.forLanguageTag("vi-VN")
        );

    private final List<Booking> visible =
        new ArrayList<>();

    private Set<Long> pending =
        new HashSet<>();

    private Set<Long> paid =
        new HashSet<>();

    private boolean busy;
    private boolean loadingChoices;
    private boolean rerunAfterBusy;

    private long focusBookingId = -1;

    public PaymentPanel() {
        this(null);
    }

    public PaymentPanel(User user) {

        this.user = user;

        setLayout(
            new BorderLayout(14, 14)
        );

        setBorder(
            BorderFactory.createEmptyBorder(
                18, 20, 18, 20
            )
        );

        JLabel title =
            new JLabel(
                "THANH TOÁN CHUYỂN KHOẢN"
            );

        title.setFont(
            title.getFont().deriveFont(
                Font.BOLD,
                23f
            )
        );

        add(title, BorderLayout.NORTH);

        // THONG TIN DON
        JPanel details = new JPanel(
            new GridBagLayout()
        );

        details.setBorder(
            BorderFactory.createTitledBorder(
                "Thông tin đơn đặt sân"
            )
        );

        GridBagConstraints g =
            new GridBagConstraints();

        g.insets = new Insets(
            11, 8, 11, 8
        );

        g.fill =
            GridBagConstraints.HORIZONTAL;

        g.gridy = 0;

        row(
            details,
            g,
            "Chọn đơn",
            orderBox
        );

        row(
            details,
            g,
            "Số tiền",
            amountLabel
        );

        row(
            details,
            g,
            "Nội dung CK",
            contentLabel
        );

        row(
            details,
            g,
            "Trạng thái",
            statusLabel
        );

        // CAC NUT THANH TOAN
        JPanel actions = new JPanel(
            new FlowLayout(
                FlowLayout.LEFT,
                8,
                0
            )
        );

        actions.add(refreshButton);
        actions.add(reportButton);
        actions.add(confirmButton);

        g.gridx = 0;
        g.gridwidth = 2;
        g.weightx = 1;

        details.add(actions, g);

        // KHUNG QR
        JPanel qr = new JPanel(
            new BorderLayout(6, 9)
        );

        qr.setBorder(
            BorderFactory.createTitledBorder(
                "Quét QR để chuyển khoản"
            )
        );

        qrLabel.setHorizontalAlignment(
            SwingConstants.CENTER
        );

        qrLabel.setVerticalAlignment(
            SwingConstants.CENTER
        );

        qrLabel.setPreferredSize(
            new Dimension(270, 270)
        );

        qr.add(
            qrLabel,
            BorderLayout.CENTER
        );

        JLabel qrHint = new JLabel(
            "<html><center>" +
            "Sau khi chuyển khoản, " +
            "bấm 'Tôi đã thanh toán'." +
            "<br>" +
            "Admin sẽ đối chiếu và xác nhận." +
            "</center></html>",
            SwingConstants.CENTER
        );

        qr.add(
            qrHint,
            BorderLayout.SOUTH
        );

        JPanel center = new JPanel(
            new BorderLayout(16, 10)
        );

        center.add(
            details,
            BorderLayout.CENTER
        );

        center.add(
            qr,
            BorderLayout.EAST
        );

        add(
            new JScrollPane(center),
            BorderLayout.CENTER
        );

        JLabel footer = new JLabel(
            "Lưu ý: Bấm 'Tôi đã thanh toán' " +
            "KHÔNG đồng nghĩa ngân hàng " +
            "đã chuyển tiền thành công."
        );

        add(
            footer,
            BorderLayout.SOUTH
        );

        // TAI ANH QR
        showQR();

        // EVENTS
        orderBox.addActionListener(
            e -> updateSelection()
        );

        refreshButton.addActionListener(
            e -> refreshData()
        );

        reportButton.addActionListener(
            e -> report()
        );

        confirmButton.addActionListener(
            e -> confirm()
        );

        boolean admin =
            user != null &&
            "ADMIN".equalsIgnoreCase(
                user.role()
            );

        confirmButton.setVisible(admin);

        if (user == null) {

            reportButton.setEnabled(false);
            refreshButton.setEnabled(false);

            statusLabel.setText(
                "Hãy đăng nhập để thanh toán."
            );

        } else {

            refreshData();
        }
    }

    // NHAN MA DON TU MAN HINH DAT SAN
    public void focusBooking(long id) {

        focusBookingId = id;
        refreshData();
    }

    private void row(
            JPanel form,
            GridBagConstraints g,
            String name,
            JComponent value
    ) {

        g.gridx = 0;
        g.gridwidth = 1;
        g.weightx = 0;

        form.add(
            new JLabel(name + ":"),
            g
        );

        g.gridx = 1;
        g.weightx = 1;

        form.add(value, g);

        g.gridy++;
    }

    // DOC ANH QR TU RESOURCES
    private void showQR() {

        // Anh QR se duoc bo sung sau
        // src/main/resources/images/transfer-qr.png

        try (
            InputStream stream =
                getClass()
                    .getClassLoader()
                    .getResourceAsStream(
                        "images/transfer-qr.png"
                    )
        ) {

            if (stream == null) {

                qrLabel.setText(
                    "<html><center>" +
                    "CHƯA CÓ ẢNH QR" +
                    "<br>" +
                    "Thêm transfer-qr.png " +
                    "vào resources/images/" +
                    "<br>" +
                    "để hiển thị QR ngân hàng." +
                    "</center></html>"
                );

                return;
            }

            BufferedImage image =
                ImageIO.read(stream);

            if (image == null) {

                throw new IllegalArgumentException(
                    "Ảnh QR không hợp lệ."
                );
            }

            Image scaled =
                image.getScaledInstance(
                    245,
                    245,
                    Image.SCALE_SMOOTH
                );

            qrLabel.setIcon(
                new ImageIcon(scaled)
            );

            qrLabel.setText("");

        } catch (Exception ex) {

            qrLabel.setText(
                "Không đọc được ảnh QR: " +
                ex.getMessage()
            );
        }
    }

    // TAI LAI DANH SACH DON
    public void refreshData() {

        if (user == null) {
            return;
        }

        if (busy) {

            rerunAfterBusy = true;
            return;
        }

        long selected = focusBookingId;

        if (
            selected <= 0 &&
            orderBox.getSelectedItem()
                instanceof BookingChoice c
        ) {

            selected = c.booking.id();
        }

        final long targetId = selected;

        setBusy(true);

        new SwingWorker<Data, Void>() {

            @Override
            protected Data doInBackground()
                    throws Exception {

                List<Booking> bookings =
                    bookingService.listVisible(user);

                Set<Long> requestIds =
                    paymentService.pendingReports();

                Set<Long> paidIds =
                    paymentService.paidBookingIds();

                return new Data(
                    bookings,
                    requestIds,
                    paidIds
                );
            }

            @Override
            protected void done() {

                setBusy(false);

                try {

                    Data d = get();

                    visible.clear();
                    visible.addAll(
                        d.bookings
                    );

                    pending = d.pending;
                    paid = d.paid;

                    loadingChoices = true;

                    orderBox.removeAllItems();

                    int index = -1;

                    for (Booking b : visible) {

                        if (
                            "CANCELLED".equals(b.status())
                        ) {
                            continue;
                        }

                        orderBox.addItem(
                            new BookingChoice(b)
                        );

                        if (
                            b.id() ==
                                (
                                    focusBookingId > 0
                                        ? focusBookingId
                                        : targetId
                                )
                        ) {

                            index =
                                orderBox.getItemCount()
                                - 1;
                        }
                    }

                    if (index >= 0) {

                        orderBox.setSelectedIndex(
                            index
                        );
                    }

                    loadingChoices = false;

                    focusBookingId = -1;

                    updateSelection();

                    if (rerunAfterBusy) {

                        rerunAfterBusy = false;
                        refreshData();
                    }

                } catch (InterruptedException ex) {

                    Thread.currentThread().interrupt();

                } catch (ExecutionException ex) {

                    showError(ex.getCause());
                }
            }
        }.execute();
    }

    // HIEN THI THONG TIN DON DUOC CHON
    private void updateSelection() {

        if (loadingChoices) {
            return;
        }

        BookingChoice choice =
            (BookingChoice) orderBox.getSelectedItem();

        if (choice == null) {

            amountLabel.setText(
                "Chưa có đơn"
            );

            contentLabel.setText("--");
            statusLabel.setText("--");

            reportButton.setEnabled(false);
            confirmButton.setEnabled(false);

            return;
        }

        Booking b = choice.booking;

        amountLabel.setText(
            "Đang tải giá..."
        );

        contentLabel.setText(
            "THMINTON " + b.id()
        );

        if (
            paid.contains(b.id()) ||
            "CONFIRMED".equals(b.status())
        ) {

            statusLabel.setText(
                "Đã xác nhận thanh toán"
            );

        } else if (
            pending.contains(b.id())
        ) {

            statusLabel.setText(
                "Đã báo chuyển khoản - " +
                "đang chờ admin xác nhận"
            );

        } else {

            statusLabel.setText(
                "Chưa báo chuyển khoản"
            );
        }

        reportButton.setEnabled(
            !busy &&
            !paid.contains(b.id()) &&
            !pending.contains(b.id()) &&
            "PENDING".equals(b.status())
        );

        confirmButton.setEnabled(
            !busy &&
            user != null &&
            "ADMIN".equalsIgnoreCase(user.role()) &&
            pending.contains(b.id()) &&
            !paid.contains(b.id())
        );

        final long id = b.id();

        new SwingWorker<BigDecimal, Void>() {

            @Override
            protected BigDecimal doInBackground()
                    throws Exception {

                return paymentService.amount(b);
            }

            @Override
            protected void done() {

                // Khong ghi de gia cua don khac
                // neu khach doi combobox.
                if (
                    !(
                        orderBox.getSelectedItem()
                            instanceof BookingChoice current
                    ) ||
                    current.booking.id() != id
                ) {

                    return;
                }

                try {

                    amountLabel.setText(
                        money.format(get()) +
                        " VND"
                    );

                } catch (Exception ex) {

                    amountLabel.setText(
                        "Không tải được giá."
                    );
                }
            }
        }.execute();
    }

    // NUT TOI DA THANH TOAN
    private void report() {

        BookingChoice selected =
            (BookingChoice) orderBox.getSelectedItem();

        if (
            selected == null ||
            busy
        ) {
            return;
        }

        long id = selected.booking.id();

        int answer =
            JOptionPane.showConfirmDialog(
                this,
                "Bạn xác nhận ĐÃ THỰC HIỆN " +
                "chuyển khoản cho đơn #" +
                id +
                "?\n" +
                "Nội dung chuyển khoản: " +
                "THMINTON " +
                id +
                "\n" +
                "Hệ thống sẽ chuyển đơn " +
                "sang chờ admin kiểm tra.",
                "Báo đã chuyển khoản",
                JOptionPane.YES_NO_OPTION
            );

        if (
            answer !=
            JOptionPane.YES_OPTION
        ) {
            return;
        }

        setBusy(true);

        new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground()
                    throws Exception {

                paymentService.reportTransfer(
                    user,
                    id
                );

                return null;
            }

            @Override
            protected void done() {

                setBusy(false);

                try {

                    get();

                    JOptionPane.showMessageDialog(
                        PaymentPanel.this,
                        "Đã gửi thông báo chuyển khoản. " +
                        "Vui lòng chờ admin kiểm tra tiền về."
                    );

                    focusBooking(id);

                } catch (Exception ex) {

                    showError(
                        rootCause(ex)
                    );
                }
            }
        }.execute();
    }

    // NUT ADMIN XAC NHAN
    private void confirm() {

        BookingChoice selected =
            (BookingChoice) orderBox.getSelectedItem();

        if (
            selected == null ||
            busy ||
            user == null ||
            !"ADMIN".equalsIgnoreCase(user.role())
        ) {
            return;
        }

        long id = selected.booking.id();

        int answer =
            JOptionPane.showConfirmDialog(
                this,
                "CHỈ XÁC NHẬN khi đã kiểm tra " +
                "sao kê và tiền ĐÃ VỀ!\n" +
                "Xác nhận đơn #" +
                id +
                "?",
                "Admin: xác nhận tiền",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

        if (
            answer !=
            JOptionPane.YES_OPTION
        ) {
            return;
        }

        setBusy(true);

        new SwingWorker<Long, Void>() {

            @Override
            protected Long doInBackground()
                    throws Exception {

                return paymentService.confirmTransfer(
                    user,
                    id
                );
            }

            @Override
            protected void done() {

                setBusy(false);

                try {

                    long paymentId = get();

                    JOptionPane.showMessageDialog(
                        PaymentPanel.this,
                        "Đã xác nhận thanh toán thành công! " +
                        "Mã giao dịch #" +
                        paymentId
                    );

                    focusBooking(id);

                } catch (Exception ex) {

                    showError(
                        rootCause(ex)
                    );
                }
            }
        }.execute();
    }

    // KHOA NUT KHI DANG XU LY
    private void setBusy(boolean busy) {

        this.busy = busy;

        refreshButton.setEnabled(
            !busy && user != null
        );

        orderBox.setEnabled(
            !busy && user != null
        );

        if (!busy) {

            updateSelection();

        } else {

            reportButton.setEnabled(false);
            confirmButton.setEnabled(false);
        }
    }

    private static Throwable rootCause(
            Throwable ex
    ) {

        if (
            ex instanceof ExecutionException &&
            ex.getCause() != null
        ) {

            return ex.getCause();
        }

        return ex;
    }

    private void showError(Throwable ex) {

        JOptionPane.showMessageDialog(
            this,
            "Không thực hiện được: " +
            ex.getMessage(),
            "TH-MINTON",
            JOptionPane.ERROR_MESSAGE
        );
    }

    private record Data(
        List<Booking> bookings,
        Set<Long> pending,
        Set<Long> paid
    ) {}

    private static final class BookingChoice {

        private final Booking booking;

        private BookingChoice(Booking booking) {
            this.booking = booking;
        }

        @Override
        public String toString() {

            return "#" +
                booking.id() +
                " | Sân " +
                booking.courtId() +
                " | " +
                booking.bookingDate() +
                " " +
                booking.startTime() +
                " - " +
                booking.endTime();
        }
    }
}