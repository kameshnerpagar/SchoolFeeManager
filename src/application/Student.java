package application;

import java.time.LocalDate;

public class Student {
    private int id;
    private String name;
    private String studentClass;
    private double totalFee;
    private double paidFee;
    private double balance;
    private String status;
    private LocalDate dueDate;


    public Student(int id, String name, String studentClass, double totalFee, double paidFee, double balance, String status, LocalDate dueDate) {
        this.id = id;
        this.name = name;
        this.studentClass = studentClass;
        this.totalFee = totalFee;
        this.paidFee = paidFee;
        this.balance = balance;
        this.status = status;
        this.dueDate = dueDate;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getStudentClass() { return studentClass; }
    public double getTotalFee() { return totalFee; }
    public double getPaidFee() { return paidFee; }
    public double getBalance() { return balance; }
    public String getStatus() { return status; }
    public LocalDate getDueDate() {
        return dueDate;
    }
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

}
