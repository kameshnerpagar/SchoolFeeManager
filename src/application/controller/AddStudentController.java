package application.controller;

import application.DBUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AddStudentController {

    @FXML private TextField nameField;
    @FXML private TextField classField;
    @FXML private TextField totalFeeField;
    @FXML private TextField paidFeeField;
    @FXML private DatePicker dueDateField;

    @FXML
    private void onAddStudent() {
        String name = nameField.getText().trim();
        String cls = classField.getText().trim();
        double total = Double.parseDouble(totalFeeField.getText().trim());
        double paid = Double.parseDouble(paidFeeField.getText().trim());
        double balance = total - paid;
        String status = (balance <= 0) ? "Paid" : "Due";

        String sql = "INSERT INTO students(name, student_class, total_fee, paid_fee, balance, status, due_date) VALUES (?,?,?,?,?,?,?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);
            ps.setString(2, cls);
            ps.setDouble(3, total);
            ps.setDouble(4, paid);
            ps.setDouble(5, balance);
            ps.setString(6, status);
            ps.setDate(7, java.sql.Date.valueOf(dueDateField.getValue()));

            int affected = ps.executeUpdate();

            if (affected == 0) {
                throw new RuntimeException("Inserting student failed, no rows affected.");
            }

            // get generated student id
            int newStudentId = -1;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    newStudentId = keys.getInt(1);
                }
            }

            // create user account automatically (username = nameWithoutSpaces + 123)
            if (newStudentId != -1) {
                String username = name.replaceAll("\\s+", "").toLowerCase() + "123";
                String password = username; // simple password for now
                String insertUserSql = "INSERT INTO users (username, password, role, linked_student_id, full_name) VALUES (?, ?, 'STUDENT', ?, ?)";

                try (PreparedStatement psu = conn.prepareStatement(insertUserSql)) {
                    psu.setString(1, username);
                    psu.setString(2, password);
                    psu.setInt(3, newStudentId);
                    psu.setString(4, name);
                    psu.executeUpdate();
                }
            }

            // close popup
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
