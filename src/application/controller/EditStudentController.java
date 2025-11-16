package application.controller;

import application.DBUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import application.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class EditStudentController {

    @FXML private TextField nameField;
    @FXML private TextField classField;
    @FXML private TextField totalFeeField;
    @FXML private TextField paidFeeField;
    @FXML private TextField newPaymentField;
    @FXML private DatePicker dueDateField;

    private Student selectedStudent;

    public void setStudentData(Student student) {
        this.selectedStudent = student;

        nameField.setText(student.getName());
        classField.setText(student.getStudentClass());
        totalFeeField.setText(String.valueOf(student.getTotalFee()));
        paidFeeField.setText(String.valueOf(student.getPaidFee()));
        dueDateField.setValue(student.getDueDate());
    }

    @FXML
    private void onUpdate() {

        double newPayment = newPaymentField.getText().isEmpty() ?
                0 : Double.parseDouble(newPaymentField.getText());

        double updatedPaid = selectedStudent.getPaidFee() + newPayment;
        double newBalance = selectedStudent.getTotalFee() - updatedPaid;
        String status = (newBalance <= 0) ? "Paid" : "Due";

        String sql = "UPDATE students SET name=?, student_class=?, paid_fee=?, balance=?, status=?, due_date=? WHERE id=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nameField.getText());
            ps.setString(2, classField.getText());
            ps.setDouble(3, updatedPaid);
            ps.setDouble(4, newBalance);
            ps.setString(5, status);
            ps.setDate(6, java.sql.Date.valueOf(dueDateField.getValue()));
            ps.setInt(7, selectedStudent.getId());

            ps.executeUpdate();

            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
