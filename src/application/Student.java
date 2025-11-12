package application;

public class Student {
    private int id;
    private String name;
    private String studentClass;
    private double totalFee;
    private double paidFee;
    private double balance;
    private String status;

    public Student(int id, String name, String studentClass, double totalFee, double paidFee, double balance, String status) {
        this.id = id;
        this.name = name;
        this.studentClass = studentClass;
        this.totalFee = totalFee;
        this.paidFee = paidFee;
        this.balance = balance;
        this.status = status;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getStudentClass() { return studentClass; }
    public double getTotalFee() { return totalFee; }
    public double getPaidFee() { return paidFee; }
    public double getBalance() { return balance; }
    public String getStatus() { return status; }
}
