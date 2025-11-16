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

        double amount = Double.parseDouble(amountText);

        String insertPayment =
                "INSERT INTO payments(student_id, amount, method, payment_date) VALUES(?, ?, ?, NOW())";

        String updateStudent =
                "UPDATE students SET " +
                        "paid_fee = paid_fee + ?, " +
                        "balance = balance - ?, " +
                        "status = CASE WHEN (balance - ?) <= 0 THEN 'Paid' ELSE 'Due' END " +
                        "WHERE id = ?";

        try (Connection conn = DBUtil.getConnection()) {

            // Insert into payments table
            PreparedStatement ps1 = conn.prepareStatement(insertPayment);
            ps1.setInt(1, studentId);
            ps1.setDouble(2, amount);
            ps1.setString(3, method);
            ps1.executeUpdate();

            // Update students table
            PreparedStatement ps2 = conn.prepareStatement(updateStudent);
            ps2.setDouble(1, amount);  // add to paid_fee
            ps2.setDouble(2, amount);  // subtract from balance
            ps2.setDouble(3, amount);  // check final balance for Paid/Due
            ps2.setInt(4, studentId);
            ps2.executeUpdate();



            showAlert("Payment successful!");

            if (refreshCallback != null) refreshCallback.run();

            closeWindow();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error while processing payment.");
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
