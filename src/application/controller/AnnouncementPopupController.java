package application.controller;

import application.DBUtil;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class AnnouncementPopupController {

    @FXML private TextArea messageField;

    @FXML
    private void onSave() {
        String msg = messageField.getText().trim();

        if (msg.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO announcements (message) VALUES (?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, msg);
            ps.executeUpdate();

            // close popup
            Stage stage = (Stage) messageField.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
