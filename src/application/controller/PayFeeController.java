package application.controller;

import application.DBUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PayFeeController {

    @FXML private Label studentNameLabel;
    @FXML private TextField amountField;
    @FXML private ComboBox<String> methodBox;

    private int studentId;
    private Runnable refreshCallback;

    public void setStudentId(int id) {
        this.studentId = id;
        loadStudentName();
    }

    public void setRefreshCallback(Runnable callback) {
        this.refreshCallback = callback;
    }

    @FXML
    public void initialize() {
        methodBox.getItems().addAll("Cash", "Online UPI", "Bank Transfer", "Card Payment");
    }

    private void loadStudentName() {
        String sql = "SELECT name FROM students WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                studentNameLabel.setText("Student: " + rs.getString("name"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onConfirmPayment() {
        String amountText = amountField.getText();
        String method = methodBox.getValue();

        if (amountText.isEmpty() || method == null) {
            showAlert("Please enter amount and select method.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                showAlert("Enter a positive amount.");
                return;
            }
        } catch (NumberFormatException ex) {
            showAlert("Invalid amount.");
            return;
        }

        String insertPaymentSql =
                "INSERT INTO payments(student_id, amount, method, payment_date) VALUES(?, ?, ?, NOW())";

        String selectStudentSql =
                "SELECT paid_fee, total_fee FROM students WHERE id = ? FOR UPDATE";

        String updateStudentSql =
                "UPDATE students SET paid_fee = ?, balance = ?, status = ? WHERE id = ?";

        // Single connection + transaction to keep DB consistent
        try (Connection conn = DBUtil.getConnection()) {
            try {
                conn.setAutoCommit(false);

                // 1) Insert payment record
                try (PreparedStatement psInsert = conn.prepareStatement(insertPaymentSql)) {
                    psInsert.setInt(1, studentId);
                    psInsert.setDouble(2, amount);
                    psInsert.setString(3, method);
                    psInsert.executeUpdate();
                }

                // 2) Read current paid_fee & total_fee (FOR UPDATE to lock row)
                double currentPaid = 0;
                double totalFee = 0;
                try (PreparedStatement psSelect = conn.prepareStatement(selectStudentSql)) {
                    psSelect.setInt(1, studentId);
                    try (ResultSet rs = psSelect.executeQuery()) {
                        if (rs.next()) {
                            currentPaid = rs.getDouble("paid_fee");
                            totalFee = rs.getDouble("total_fee");
                        } else {
                            // student not found -> rollback and exit
                            conn.rollback();
                            showAlert("Student not found.");
                            return;
                        }
                    }
                }

                // 3) Compute new values in Java (same correct approach as EditStudentController)
                double newPaid = currentPaid + amount;
                double newBalance = totalFee - newPaid;
                String status = (newBalance <= 0) ? "Paid" : "Due";

                // 4) Update student with concrete values
                try (PreparedStatement psUpdate = conn.prepareStatement(updateStudentSql)) {
                    psUpdate.setDouble(1, newPaid);
                    psUpdate.setDouble(2, newBalance);
                    psUpdate.setString(3, status);
                    psUpdate.setInt(4, studentId);
                    psUpdate.executeUpdate();
                }

                // 5) Commit transaction
                conn.commit();

                showAlert("Payment successful!");
                if (refreshCallback != null) refreshCallback.run();
                closeWindow();

            } catch (Exception ex) {
                // rollback on any error
                try { conn.rollback(); } catch (Exception ignore) {}
                ex.printStackTrace();
                showAlert("Error while processing payment.");
            } finally {
                try { conn.setAutoCommit(true); } catch (Exception ignore) {}
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Database connection error.");
        }
    }



    private void closeWindow() {
        Stage stage = (Stage) amountField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
