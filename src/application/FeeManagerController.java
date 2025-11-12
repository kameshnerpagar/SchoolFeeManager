package application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;

public class FeeManagerController {

    @FXML private TextField nameField, classField, totalFeeField, paidFeeField;
    @FXML private ComboBox<String> statusBox;
    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, Integer> idColumn;
    @FXML private TableColumn<Student, String> nameColumn, classColumn, statusColumn;
    @FXML private TableColumn<Student, Double> totalColumn, paidColumn, balanceColumn;

    private ObservableList<Student> studentList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        statusBox.getItems().addAll("Paid", "Due");
        idColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("name"));
        classColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("studentClass"));
        totalColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("totalFee"));
        paidColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("paidFee"));
        balanceColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("balance"));
        statusColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("status"));
        loadData();
    }

    @FXML
    public void addStudent() {
        String name = nameField.getText();
        String studentClass = classField.getText();
        double total = Double.parseDouble(totalFeeField.getText());
        double paid = Double.parseDouble(paidFeeField.getText());
        double balance = total - paid;
        String status = statusBox.getValue();

        String query = "INSERT INTO students(name, student_class, total_fee, paid_fee, balance, status) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, name);
            ps.setString(2, studentClass);
            ps.setDouble(3, total);
            ps.setDouble(4, paid);
            ps.setDouble(5, balance);
            ps.setString(6, status);
            ps.executeUpdate();
            loadData();
            clearFields();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void deleteStudent() {
        Student selected = studentTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        String query = "DELETE FROM students WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, selected.getId());
            ps.executeUpdate();
            loadData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void loadData() {
        studentList.clear();
        try (Connection conn = DBUtil.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM students")) {

            while (rs.next()) {
                studentList.add(new Student(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("student_class"),
                        rs.getDouble("total_fee"),
                        rs.getDouble("paid_fee"),
                        rs.getDouble("balance"),
                        rs.getString("status")
                ));
            }
            studentTable.setItems(studentList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void clearFields() {
        nameField.clear();
        classField.clear();
        totalFeeField.clear();
        paidFeeField.clear();
        statusBox.setValue(null);
    }
}
