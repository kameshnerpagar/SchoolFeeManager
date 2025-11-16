package application;

public class AppState {
    private static AppState instance = new AppState();
    private int userId;
    private String username;
    private String role; // "ADMIN" or "STUDENT"
    private Integer linkedStudentId; // nullable

    private AppState() {}

    public static AppState getInstance() {
        return instance;
    }

    public void setUser(int userId, String username, String role, Integer linkedStudentId) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.linkedStudentId = linkedStudentId;
    }

    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public Integer getLinkedStudentId() { return linkedStudentId; }
}
