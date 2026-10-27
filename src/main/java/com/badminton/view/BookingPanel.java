package com.badminton.view;

import com.badminton.dao.CustomerDAO;
import com.badminton.model.Booking;
import com.badminton.model.Court;
import com.badminton.model.Customer;
import com.badminton.model.User;
import com.badminton.service.BookingService;
import com.badminton.service.CourtService;
import com.badminton.util.PriceCalculator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class BookingPanel extends JPanel {

    // =============================
    // SERVICES
    // =============================

    private final User user;

    private final Consumer<Long> goToPayment;

    private final BookingService service =
        new BookingService();

    private final CourtService courtService =
        new CourtService();

    private final CustomerDAO customerDAO =
        new CustomerDAO();

    // =============================
    // CAC THANH PHAN GIAO DIEN
    // =============================

    private final JComboBox<Court> courtBox =
        new JComboBox<>();

    private final JComboBox<Customer> customerBox =
        new JComboBox<>();

    private final JTextField dateField =
        new JTextField(
            LocalDate.now().toString(),
            12
        );

    private final JComboBox<LocalTime> startBox =
        new JComboBox<>();

    private final JComboBox<LocalTime> endBox =
        new JComboBox<>();

    private final JLabel estimate =
        new JLabel("Tạm tính: --");

    private final JButton bookButton =
        new JButton(
            "Đặt sân và tiếp tục thanh toán"
        );

    private final JButton refreshButton =
        new JButton("Làm mới");

    private final JButton paymentButton =
        new JButton("Thanh toán đơn đã chọn");

    private final JButton cancelButton =
        new JButton("Hủy đơn đã chọn");

    // =============================
    // BANG DON DAT SAN
    // =============================

    private final DefaultTableModel model =
        new DefaultTableModel(
            new Object[]{
                "Mã đơn",
                "Sân",
                "Ngày",
                "Bắt đầu",
                "Kết thúc",
                "Trạng thái"
            },
            0
        ) {

            @Override
            public boolean isCellEditable(
                    int row,
                    int col
            ) {
                return false;
            }
        };

    private final JTable table =
        new JTable(model);

    private final List<Booking> visibleBookings =
        new ArrayList<>();

    private final NumberFormat money =
        NumberFormat.getNumberInstance(
            Locale.forLanguageTag("vi-VN")
        );

    private boolean busy;

    // Luu ID san duoc chon tu Danh sach san
    private long pendingCourtId = -1;

    // =============================
    // CONSTRUCTOR
    // =============================

    public BookingPanel() {
        this(null, id -> {});
    }

    public BookingPanel(
            User user,
            Consumer<Long> goToPayment
    ) {

        this.user = user;
        this.goToPayment = goToPayment;

        setLayout(
            new BorderLayout(12, 12)
        );

        setBorder(
            BorderFactory.createEmptyBorder(
                18, 20, 18, 20
            )
        );

        // =============================
        // HEADER
        // =============================

        JLabel title =
            new JLabel("ĐẶT SÂN CẦU LÔNG");

        title.setFont(
            title.getFont().deriveFont(
                Font.BOLD,
                24f
            )
        );

        JLabel hint = new JLabel(
            "Chọn sân, ngày, giờ. "
                + "Giá được tính theo thời lượng đặt."
        );

        JPanel head = new JPanel(
            new GridLayout(2, 1, 0, 5)
        );

        head.add(title);
        head.add(hint);

        add(head, BorderLayout.NORTH);

        // =============================
        // FORM DAT SAN
        // =============================

        JPanel form = new JPanel(
            new GridBagLayout()
        );

        form.setBorder(
            BorderFactory.createTitledBorder(
                "Thông tin đặt sân"
            )
        );

        GridBagConstraints g =
            new GridBagConstraints();

        g.insets =
            new Insets(7, 9, 7, 9);

        g.fill =
            GridBagConstraints.HORIZONTAL;

        g.gridy = 0;

        row(form, g, "Sân", courtBox);

        row(
            form,
            g,
            "Ngày (yyyy-MM-dd)",
            dateField
        );

        row(
            form,
            g,
            "Giờ bắt đầu",
            startBox
        );

        row(
            form,
            g,
            "Giờ kết thúc",
            endBox
        );

        if (
            user != null &&
            "ADMIN".equalsIgnoreCase(user.role())
        ) {

            row(
                form,
                g,
                "Khách hàng",
                customerBox
            );
        }

        row(
            form,
            g,
            "Thành tiền",
            estimate
        );

        // =============================
        // NUT DAT SAN VA LAM MOI
        // =============================

        JPanel actions = new JPanel(
            new FlowLayout(
                FlowLayout.LEFT,
                9,
                0
            )
        );

        actions.add(bookButton);
        actions.add(refreshButton);

        g.gridx = 0;
        g.gridwidth = 2;
        g.weightx = 1;

        form.add(actions, g);

        JPanel main = new JPanel(
            new BorderLayout(10, 10)
        );

        main.add(
            form,
            BorderLayout.NORTH
        );

        // =============================
        // DANH SACH DON DAT SAN
        // =============================

        JPanel history = new JPanel(
            new BorderLayout(8, 8)
        );

        history.setBorder(
            BorderFactory.createTitledBorder(
                "Đơn đặt sân"
            )
        );

        table.setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION
        );

        table.setRowHeight(29);
        table.setAutoCreateRowSorter(true);

        history.add(
            new JScrollPane(table),
            BorderLayout.CENTER
        );

        JPanel bottom = new JPanel(
            new FlowLayout(FlowLayout.RIGHT)
        );

        bottom.add(cancelButton);
        bottom.add(paymentButton);

        history.add(
            bottom,
            BorderLayout.SOUTH
        );

        main.add(
            history,
            BorderLayout.CENTER
        );

        add(
            main,
            BorderLayout.CENTER
        );

        // =============================
        // GIO BAT DAU
        // =============================

        for (int h = 6; h <= 22; h++) {

            for (int m = 0; m <= 30; m += 30) {

                startBox.addItem(
                    LocalTime.of(h, m)
                );
            }
        }

        // =============================
        // GIO KET THUC
        // =============================

        for (int h = 6; h <= 23; h++) {

            for (int m = 0; m <= 30; m += 30) {

                if (h == 23 && m == 30) {
                    break;
                }

                endBox.addItem(
                    LocalTime.of(h, m)
                );
            }
        }

        startBox.setSelectedItem(
            LocalTime.of(18, 0)
        );

        endBox.setSelectedItem(
            LocalTime.of(19, 0)
        );

        // =============================
        // HIEN TEN SAN VA GIA
        // =============================

        courtBox.setRenderer(
            new DefaultListCellRenderer() {

                @Override
                public Component getListCellRendererComponent(
                        JList<?> list,
                        Object value,
                        int index,
                        boolean selected,
                        boolean focus
                ) {

                    super.getListCellRendererComponent(
                        list,
                        value,
                        index,
                        selected,
                        focus
                    );

                    if (value instanceof Court c) {

                        setText(
                            c.name()
                                + " - "
                                + money.format(c.pricePerHour())
                                + " đ/giờ"
                        );
                    }

                    return this;
                }
            }
        );

        // =============================
        // HIEN KHACH HANG
        // =============================

        customerBox.setRenderer(
            new DefaultListCellRenderer() {

                @Override
                public Component getListCellRendererComponent(
                        JList<?> list,
                        Object value,
                        int index,
                        boolean selected,
                        boolean focus
                ) {

                    super.getListCellRendererComponent(
                        list,
                        value,
                        index,
                        selected,
                        focus
                    );

                    if (value instanceof Customer c) {

                        setText(
                            "#" + c.id()
                                + " - "
                                + c.fullName()
                        );
                    }

                    return this;
                }
            }
        );

        // =============================
        // EVENTS
        // =============================

        courtBox.addActionListener(
            e -> updateEstimate()
        );

        startBox.addActionListener(
            e -> updateEstimate()
        );

        endBox.addActionListener(
            e -> updateEstimate()
        );

        bookButton.addActionListener(
            e -> createBooking()
        );

        refreshButton.addActionListener(
            e -> refreshData()
        );

        paymentButton.addActionListener(
            e -> paySelected()
        );

        cancelButton.addActionListener(
            e -> cancelSelected()
        );

        if (user == null) {

            bookButton.setEnabled(false);
            paymentButton.setEnabled(false);

            hint.setText(
                "Vui lòng đăng nhập "
                    + "từ màn hình chính để đặt sân."
            );

        } else {

            refreshData();
        }
    }

    // =============================
    // THEM DONG VAO FORM
    // =============================

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

    // =============================
    // TINH GIA TAM TINH
    // =============================

    private void updateEstimate() {

        Court court =
            (Court) courtBox.getSelectedItem();

        LocalTime from =
            (LocalTime) startBox.getSelectedItem();

        LocalTime to =
            (LocalTime) endBox.getSelectedItem();

        if (
            court == null ||
            from == null ||
            to == null ||
            !from.isBefore(to)
        ) {

            estimate.setText(
                "Giờ kết thúc phải lớn hơn giờ bắt đầu"
            );

            return;
        }

        BigDecimal amount =
            PriceCalculator.calculate(
                court.pricePerHour(),
                from,
                to
            );

        estimate.setText(
            money.format(amount) + " VND"
        );
    }

    // =============================
    // TU DONG CHON SAN
    // =============================

    /**
     * Nhan ID san tu giao dien Danh sach san.
     * Neu dang load du lieu, se chon sau.
     */
    public void selectCourt(long courtId) {

        if (courtId <= 0) {
            return;
        }

        pendingCourtId = courtId;

        if (!busy) {

            if (applySelectedCourt(courtId)) {

                pendingCourtId = -1;

            } else {

                refreshData();
            }
        }
    }

    // Tim san trong combobox va chon san do
    private boolean applySelectedCourt(long courtId) {

        for (
            int i = 0;
            i < courtBox.getItemCount();
            i++
        ) {

            Court court =
                courtBox.getItemAt(i);

            if (court.id() == courtId) {

                courtBox.setSelectedIndex(i);

                updateEstimate();

                return true;
            }
        }

        return false;
    }

    // =============================
    // LOAD SAN VA DANH SACH DON
    // =============================

    public void refreshData() {

        if (user == null || busy) {
            return;
        }

        setBusy(true);

        new SwingWorker<Data, Void>() {

            @Override
            protected Data doInBackground()
                    throws Exception {

                List<Court> courts =
                    courtService.listCourts();

                List<Customer> customers =
                    "ADMIN".equalsIgnoreCase(user.role())
                        ? customerDAO.findAll()
                        : List.of();

                List<Booking> bookings =
                    service.listVisible(user);

                return new Data(
                    courts,
                    customers,
                    bookings
                );
            }

            @Override
            protected void done() {

                setBusy(false);

                try {

                    Data data = get();

                    long oldCourt =
                        courtBox.getSelectedItem()
                            instanceof Court c
                            ? c.id()
                            : -1;

                    courtBox.removeAllItems();

                    for (Court court : data.courts) {

                        if (court.active()) {
                            courtBox.addItem(court);
                        }
                    }

                    // Uu tien san tu Danh sach san
                    long courtToSelect =
                        pendingCourtId > 0
                            ? pendingCourtId
                            : oldCourt;

                    boolean found =
                        applySelectedCourt(courtToSelect);

                    if (pendingCourtId > 0) {

                        pendingCourtId = -1;

                        if (!found) {

                            JOptionPane.showMessageDialog(
                                BookingPanel.this,
                                "Sân vừa chọn không còn hoạt động. "
                                    + "Hãy chọn sân khác."
                            );
                        }
                    }

                    customerBox.removeAllItems();

                    for (Customer c : data.customers) {
                        customerBox.addItem(c);
                    }

                    // Hien thi danh sach don
                    visibleBookings.clear();
                    visibleBookings.addAll(data.bookings);

                    model.setRowCount(0);

                    for (Booking b : data.bookings) {

                        String courtName =
                            data.courts.stream()
                                .filter(
                                    c -> c.id() == b.courtId()
                                )
                                .map(Court::name)
                                .findFirst()
                                .orElse(
                                    "Sân #" + b.courtId()
                                );

                        model.addRow(
                            new Object[]{
                                b.id(),
                                courtName,
                                b.bookingDate(),
                                b.startTime(),
                                b.endTime(),
                                b.status()
                            }
                        );
                    }

                    updateEstimate();

                } catch (InterruptedException ex) {

                    Thread.currentThread().interrupt();

                } catch (ExecutionException ex) {

                    showError(ex.getCause());
                }
            }
        }.execute();
    }

    // =============================
    // TAO DON DAT SAN
    // =============================

    private void createBooking() {

        if (user == null || busy) {
            return;
        }

        try {

            Court court =
                (Court) courtBox.getSelectedItem();

            if (court == null) {

                throw new IllegalArgumentException(
                    "Chưa có sân hoạt động."
                );
            }

            long customerId;

            if ("ADMIN".equalsIgnoreCase(user.role())) {

                Customer c =
                    (Customer) customerBox.getSelectedItem();

                if (c == null) {

                    throw new IllegalArgumentException(
                        "Hãy chọn khách hàng."
                    );
                }

                customerId = c.id();

            } else {

                customerId = -1;
            }

            LocalDate date =
                LocalDate.parse(
                    dateField.getText().strip()
                );

            LocalTime start =
                (LocalTime) startBox.getSelectedItem();

            LocalTime end =
                (LocalTime) endBox.getSelectedItem();

            Booking booking = new Booking(
                0,
                court.id(),
                customerId,
                date,
                start,
                end,
                "PENDING"
            );

            BigDecimal amount =
                PriceCalculator.calculate(
                    court.pricePerHour(),
                    start,
                    end
                );

            int answer =
                JOptionPane.showConfirmDialog(
                    this,
                    "Xác nhận đặt "
                        + court.name()
                        + " ngày "
                        + date
                        + " ("
                        + start
                        + " - "
                        + end
                        + ")?\n"
                        + "Tổng tiền: "
                        + money.format(amount)
                        + " VND",
                    "Xác nhận đặt sân",
                    JOptionPane.YES_NO_OPTION
                );

            if (answer != JOptionPane.YES_OPTION) {
                return;
            }

            setBusy(true);

            // Xu ly database tren background thread
            new SwingWorker<Long, Void>() {

                @Override
                protected Long doInBackground()
                        throws Exception {

                    long id =
                        "ADMIN".equalsIgnoreCase(user.role())
                            ? booking.customerId()
                            : service.customerId(user);

                    Booking finalBooking = new Booking(
                        0,
                        booking.courtId(),
                        id,
                        booking.bookingDate(),
                        booking.startTime(),
                        booking.endTime(),
                        "PENDING"
                    );

                    return service.createBooking(
                        user,
                        finalBooking
                    );
                }

                @Override
                protected void done() {

                    setBusy(false);

                    try {

                        long id = get();

                        JOptionPane.showMessageDialog(
                            BookingPanel.this,
                            "Đặt sân thành công! "
                                + "Mã đơn #"
                                + id
                                + ". Chuyển sang thanh toán."
                        );

                        refreshData();

                        // CHUYEN SANG THANH TOAN QR
                        goToPayment.accept(id);

                    } catch (InterruptedException ex) {

                        Thread.currentThread().interrupt();

                    } catch (ExecutionException ex) {

                        showError(ex.getCause());
                    }
                }
            }.execute();

        } catch (
            java.time.format.DateTimeParseException ex
        ) {

            JOptionPane.showMessageDialog(
                this,
                "Ngày phải theo định dạng yyyy-MM-dd."
            );

        } catch (Exception ex) {

            showError(ex);
        }
    }

    // =============================
    // THANH TOAN DON DA CHON
    // =============================

    private void paySelected() {

        int row = table.getSelectedRow();

        if (row < 0) {

            JOptionPane.showMessageDialog(
                this,
                "Hãy chọn một đơn trong bảng."
            );

            return;
        }

        Booking b =
            visibleBookings.get(
                table.convertRowIndexToModel(row)
            );

        if ("CANCELLED".equals(b.status())) {

            JOptionPane.showMessageDialog(
                this,
                "Đơn này đã hủy."
            );

            return;
        }

        goToPayment.accept(b.id());
    }

    // =============================
    // HUY DON CHUA THANH TOAN
    // =============================

    private void cancelSelected() {

        if (busy || user == null) {
            return;
        }

        int row = table.getSelectedRow();

        if (row < 0) {

            JOptionPane.showMessageDialog(
                this,
                "Hãy chọn một đơn trong bảng."
            );

            return;
        }

        Booking b =
            visibleBookings.get(
                table.convertRowIndexToModel(row)
            );

        if (!"PENDING".equals(b.status())) {

            JOptionPane.showMessageDialog(
                this,
                "Chỉ được hủy đơn đang chờ thanh toán."
            );

            return;
        }

        int answer =
            JOptionPane.showConfirmDialog(
                this,
                "Hủy đơn #" + b.id() + "?",
                "Xác nhận hủy",
                JOptionPane.YES_NO_OPTION
            );

        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        setBusy(true);

        new SwingWorker<Boolean, Void>() {

            @Override
            protected Boolean doInBackground()
                    throws Exception {

                return service.cancelBooking(
                    user,
                    b.id()
                );
            }

            @Override
            protected void done() {

                setBusy(false);

                try {

                    if (get()) {

                        JOptionPane.showMessageDialog(
                            BookingPanel.this,
                            "Đã hủy đơn."
                        );
                    }

                    refreshData();

                } catch (InterruptedException ex) {

                    Thread.currentThread().interrupt();

                } catch (ExecutionException ex) {

                    showError(ex.getCause());
                }
            }
        }.execute();
    }

    // =============================
    // BAT TAT CAC NUT
    // =============================

    private void setBusy(boolean busy) {

        this.busy = busy;

        bookButton.setEnabled(
            !busy && user != null
        );

        refreshButton.setEnabled(
            !busy && user != null
        );

        paymentButton.setEnabled(
            !busy && user != null
        );

        cancelButton.setEnabled(
            !busy && user != null
        );
    }

    // =============================
    // HIEN THI LOI
    // =============================

    private void showError(Throwable ex) {

        JOptionPane.showMessageDialog(
            this,
            ex instanceof SQLException
                ? "Lỗi MySQL: " + ex.getMessage()
                : "Không thực hiện được: " + ex.getMessage(),
            "TH-MINTON",
            JOptionPane.ERROR_MESSAGE
        );
    }

    // Luu du lieu tra ve tu SwingWorker
    private record Data(
        List<Court> courts,
        List<Customer> customers,
        List<Booking> bookings
    ) {}
}