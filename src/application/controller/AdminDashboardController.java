package application.controller;

import application.DBUtil;
import application.Student;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import application.MainApp;
import javafx.stage.Stage;

public class AdminDashboardController {

    @FXML private Label totalStudentsLabel;
    @FXML private Label totalCollectionLabel;
    @FXML private Label pendingAmountLabel;
    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, String> colName;
    @FXML private TableColumn<Student, String> colClass;
    @FXML private TableColumn<Student, Double> colTotalFee;
    @FXML private TableColumn<Student, Double> colPaidFee;
    @FXML private TableColumn<Student, Double> colBalance;
    @FXML private TableColumn<Student, String> colDueDate;
    @FXML private TableColumn<Student, String> colStatus;
    @FXML private ListView<String> defaultersList;
    @FXML private ListView<String> announcementList;
    @FXML private ListView<String> notificationList;

    //Initialize



    @FXML
    public void initialize() {
        // test UI values
        totalStudentsLabel.setText("5");
        totalCollectionLabel.setText("₹45,000");
        pendingAmountLabel.setText("₹12,000");
        colName.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));
        colClass.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStudentClass()));
        colTotalFee.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().getTotalFee()).asObject());
        colPaidFee.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().getPaidFee()).asObject());
        colBalance.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().getBalance()).asObject());
        colDueDate.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getDueDate() != null
                                ? data.getValue().getDueDate().toString()
                                : ""
                )
        );
        colStatus.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus()));

        loadStudentTable();
        loadTotalStudents();
        loadTotalCollection();
        loadPendingAmount();
        loadTopDefaulters();
        loadAnnouncements();
        loadNotifications();
    }

    private void loadNotifications() {
        notificationList.getItems().clear();

        // 1. fee due soon (within 3 days)
        String sql1 = "SELECT name, DATEDIFF(due_date, CURDATE()) AS days_left " +
                "FROM students WHERE balance > 0 AND due_date >= CURDATE() " +
                "AND DATEDIFF(due_date, CURDATE()) <= 3";

        // 2. overdue (due_date passed)
        String sql2 = "SELECT name, due_date FROM students WHERE balance > 0 AND due_date < CURDATE()";

        try (Connection conn = DBUtil.getConnection()) {

            // ---- Due Soon ----
            PreparedStatement ps1 = conn.prepareStatement(sql1);
            ResultSet rs1 = ps1.executeQuery();

            while (rs1.next()) {
                String name = rs1.getString("name");
                int daysLeft = rs1.getInt("days_left");

                notificationList.getItems().add(
                        "⚠️ " + name + ": Fee due in " + daysLeft + " days"
                );
            }

            // ---- Overdue ----
            PreparedStatement ps2 = conn.prepareStatement(sql2);
            ResultSet rs2 = ps2.executeQuery();

            while (rs2.next()) {
                String name = rs2.getString("name");
                String date = rs2.getString("due_date");

                notificationList.getItems().add(
                        "❌ " + name + ": Fee overdue (was due on " + date + ")"
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (notificationList.getItems().isEmpty()) {
            notificationList.getItems().add("✔ No upcoming or overdue payments.");
        }
    }

    public void openAddStudentPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/add_student.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Add Student");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadStudentTable();
            loadTotalStudents();
            loadTotalCollection();
            loadPendingAmount();
            loadTopDefaulters();
            loadNotifications();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openEditStudentPopup() {
        Student selected = studentTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            System.out.println("No student selected!");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edit_student.fxml"));
            Parent root = loader.load();

            EditStudentController controller = loader.getController();
            controller.setStudentData(selected);

            Stage stage = new Stage();
            stage.setTitle("Update Student");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadStudentTable();
            loadTotalStudents();
            loadTotalCollection();
            loadPendingAmount();
            loadTopDefaulters();
            loadNotifications();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void loadTopDefaulters() {
        String sql = "SELECT name, balance FROM students WHERE balance > 0 ORDER BY balance DESC LIMIT 5";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            defaultersList.getItems().clear();

            while (rs.next()) {
                String name = rs.getString("name");
                double bal = rs.getDouble("balance");

                defaultersList.getItems().add(name + " - ₹" + (int) bal);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openAnnouncementPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/announcement_popup.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("New Announcement");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadAnnouncements(); // refresh list

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadAnnouncements() {
        String sql = "SELECT message, created_at FROM announcements ORDER BY created_at DESC LIMIT 10";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            announcementList.getItems().clear();

            while (rs.next()) {
                String msg = rs.getString("message");
                String date = rs.getString("created_at");
                announcementList.getItems().add(msg + "  (" + date + ")");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onDeleteStudent() {
        Student selected = studentTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            Alert a = new Alert(Alert.AlertType.INFORMATION,
                    "Please select a student to delete.", ButtonType.OK);
            a.setHeaderText(null);
            a.showAndWait();
            return;
        }

        Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete \"" + selected.getName() + "\"?",
                ButtonType.YES, ButtonType.NO
        );
        confirm.setHeaderText("Confirm Deletion");
        if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) return;

        String deleteUserSql = "DELETE FROM users WHERE linked_student_id = ?";
        String deleteStudentSql = "DELETE FROM students WHERE id = ?";

        try (Connection conn = DBUtil.getConnection()) {

            // FIRST DELETE USER ACCOUNT (if exists)
            try (PreparedStatement psUser = conn.prepareStatement(deleteUserSql)) {
                psUser.setInt(1, selected.getId());
                psUser.executeUpdate(); // even if 0 rows, it's fine
            }

            // THEN DELETE STUDENT
            try (PreparedStatement ps = conn.prepareStatement(deleteStudentSql)) {

                ps.setInt(1, selected.getId());
                int rows = ps.executeUpdate();

                if (rows > 0) {
                    // refresh UI
                    loadStudentTable();
                    loadTotalStudents();
                    loadTotalCollection();
                    loadPendingAmount();
                    loadTopDefaulters();
                    loadNotifications();

                    studentTable.getSelectionModel().clearSelection();

                    Alert ok = new Alert(Alert.AlertType.INFORMATION,
                            "Student deleted successfully.",
                            ButtonType.OK);
                    ok.setHeaderText(null);
                    ok.showAndWait();
                } else {
                    Alert err = new Alert(Alert.AlertType.ERROR,
                            "Delete failed. Student not found.",
                            ButtonType.OK);
                    err.setHeaderText(null);
                    err.showAndWait();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Alert err = new Alert(Alert.AlertType.ERROR,
                    "Error deleting student. Check console.",
                    ButtonType.OK);
            err.setHeaderText(null);
            err.showAndWait();
        }
    }



    private void loadStudentTable() {
        String sql = "SELECT id, name, student_class, total_fee, paid_fee, balance, due_date, status FROM students";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            studentTable.getItems().clear();

            while (rs.next()) {
                LocalDate due = null;
                String dueRaw = rs.getString("due_date");
                if (dueRaw != null && !dueRaw.isEmpty()) {
                    due = LocalDate.parse(dueRaw);
                }

                studentTable.getItems().add(
                        new Student(
                                rs.getInt("id"),
                                rs.getString("name"),
                                rs.getString("student_class"),
                                rs.getDouble("total_fee"),
                                rs.getDouble("paid_fee"),
                                rs.getDouble("balance"),
                                rs.getString("status"),
                                due
                        )
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadTotalStudents() {
        String sql = "SELECT COUNT(*) AS total FROM students";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                totalStudentsLabel.setText(String.valueOf(rs.getInt("total")));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadTotalCollection() {
        String sql = "SELECT SUM(paid_fee) AS total_paid FROM students";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                double totalPaid = rs.getDouble("total_paid");
                totalCollectionLabel.setText("₹" + (int) totalPaid);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void loadPendingAmount() {
        String sql = "SELECT SUM(balance) AS pending FROM students";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                double pending = rs.getDouble("pending");
                pendingAmountLabel.setText("₹" + (int) pending);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @FXML
    public void logout() {
        MainApp.showLoginScreen();
    }
}
