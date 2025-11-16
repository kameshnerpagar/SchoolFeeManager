package application.controller;

import application.AppState;
import application.DBUtil;
import application.MainApp;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;
    @FXML private Button loginButton;

    @FXML
    public void onLogin() {
        String userInput = usernameField.getText().trim();
        String pass = passwordField.getText().trim();

        if (userInput.isEmpty() || pass.isEmpty()) {
            messageLabel.setText("Enter username and password");
            return;
        }

        String sql = "SELECT user_id, username, password, role, linked_student_id FROM users WHERE username = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userInput);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String dbPass = rs.getString("password");
                    if (pass.equals(dbPass)) { // plain-text compare for now
                        int userId = rs.getInt("user_id");
                        String username = rs.getString("username");
                        String role = rs.getString("role");
                        Integer linkedStudentId = rs.getObject("linked_student_id") == null ? null : rs.getInt("linked_student_id");

                        // save app state
                        AppState.getInstance().setUser(userId, username, role, linkedStudentId);

                        messageLabel.setStyle("-fx-text-fill: green;");
                        messageLabel.setText("Login successful: " + role);

                        // TODO: switch to actual dashboard scene based on role
                        // For now show simple confirmation.
                        if ("ADMIN".equalsIgnoreCase(role)) {
                            MainApp.showAdminDashboard();
                        } else {
                            if (linkedStudentId != null) {
                                MainApp.showStudentDashboard(linkedStudentId);
                            } else {
                                messageLabel.setText("Student account not linked to a student record.");
                            }
                        }

                    } else {
                        messageLabel.setText("Invalid credentials");
                    }
                } else {
                    messageLabel.setText("User not found");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Login failed: " + e.getMessage());
        }
    }
}
