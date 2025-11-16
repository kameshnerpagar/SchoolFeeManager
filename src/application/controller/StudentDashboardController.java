package application.controller;

import application.AppState;
import application.DBUtil;
import application.MainApp;
import application.PaymentRecord;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class StudentDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label totalFeeLabel;
    @FXML private Label paidFeeLabel;
    @FXML private Label pendingFeeLabel;
    @FXML private ListView<String> notificationList;
    @FXML private ListView<String> announcementList;
    @FXML private TableView<PaymentRecord> paymentHistoryTable;
    @FXML private TableColumn<PaymentRecord, String> dateColumn;
    @FXML private TableColumn<PaymentRecord, Double> amountColumn;
    @FXML private TableColumn<PaymentRecord, String> typeColumn;
//    @FXML private TableView<?> paymentHistoryTable; // fill later with a payment model

    private int studentId;

    @FXML
    public void initialize() {
        // column bindings
        dateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDate()));
        amountColumn.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getAmount()).asObject());
        typeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType()));

        // optional: placeholder text when no rows
        paymentHistoryTable.setPlaceholder(new Label("No payment history yet."));
    }

    // called from MainApp after FXML load
    public void loadStudent(int id) {
        this.studentId = id;
        loadStudentInfo();
        loadAnnouncements();
        loadNotifications();
        // loadPaymentHistory() -> implement later if you have payment_history table
    }

    private void loadStudentInfo() {
        String sql = "SELECT name, total_fee, paid_fee, balance FROM students WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    double total = rs.getDouble("total_fee");
                    double paid = rs.getDouble("paid_fee");
                    double bal = rs.getDouble("balance");

                    welcomeLabel.setText("Welcome, " + name);
                    totalFeeLabel.setText("₹" + (int) total);
                    paidFeeLabel.setText("₹" + (int) paid);
                    pendingFeeLabel.setText("₹" + (int) bal);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadAnnouncements() {
        String sql = "SELECT message, created_at FROM announcements ORDER BY created_at DESC LIMIT 10";
        announcementList.getItems().clear();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                announcementList.getItems().add(rs.getString("message") + "  (" + rs.getString("created_at") + ")");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadNotifications() {
        notificationList.getItems().clear();
        String sql = "SELECT DATEDIFF(due_date, CURDATE()) AS days_left, name FROM students WHERE id = ? AND balance > 0";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int daysLeft = rs.getInt("days_left");
                    if (daysLeft < 0) {
                        notificationList.getItems().add("❌ Your fee is overdue (was due " + Math.abs(daysLeft) + " days ago)");
                    } else if (daysLeft <= 3) {
                        notificationList.getItems().add("⚠️ Your fee is due in " + daysLeft + " days");
                    } else {
                        notificationList.getItems().add("✔ No immediate dues.");
                    }
                } else {
                    notificationList.getItems().add("✔ No dues found.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadPaymentHistory() {
        // use controller's studentId (set in loadStudent)
        int sid = this.studentId;
        if (sid <= 0) return;

        paymentHistoryTable.getItems().clear();

        String sql = "SELECT payment_date, amount, method FROM payments WHERE student_id = ? ORDER BY payment_date DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sid);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String date = rs.getString("payment_date");
                    double amount = rs.getDouble("amount");
                    String method = rs.getString("method");
                    PaymentRecord record = new PaymentRecord(date, amount, method == null ? "Manual" : method);
                    paymentHistoryTable.getItems().add(record);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @FXML
    private void logout() {
        MainApp.showLoginScreen();
    }

    @FXML
    private void openPayFeePopup() {
        // implement later: opens popup to enter payment and updates DB
        Alert a = new Alert(Alert.AlertType.INFORMATION, "Pay fee popup not implemented yet.", ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
        // after implementing payment, refresh loadStudentInfo() to show new values.
    }

    public void openPayPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/pay_fee_popup.fxml"));
            Parent root = loader.load();

            PayFeeController controller = loader.getController();
            controller.setStudentId(AppState.getInstance().getLinkedStudentId());
            controller.setRefreshCallback(this::refreshDashboard); // refresh after payment

            Stage stage = new Stage();
            stage.setTitle("Pay Fee");
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void refreshDashboard() {
        loadStudentInfo();
        loadPaymentHistory();
        loadNotifications();
    }


}
