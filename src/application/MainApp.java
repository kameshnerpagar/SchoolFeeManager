package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    private static Stage mainStage;

    @Override
    public void start(Stage stage) throws Exception {
        mainStage = stage;
        showLoginScreen();
        stage.show();

    }

    public static void showLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/login.fxml"));
            mainStage.setScene(new Scene(loader.load(), 740, 520));
            mainStage.setTitle("School Fee Manager - Login");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showAdminDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/admin_dashboard.fxml"));
            mainStage.setScene(new Scene(loader.load(), 1024, 650));
            mainStage.setTitle("Admin Dashboard");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showStudentDashboard(int studentId) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/student_dashboard.fxml"));
            Parent root = loader.load();

            // get controller and pass student id
            application.controller.StudentDashboardController controller = loader.getController();
            controller.loadStudent(studentId);

            mainStage.setScene(new Scene(root, 1024, 650));
            mainStage.setTitle("Student Dashboard");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
