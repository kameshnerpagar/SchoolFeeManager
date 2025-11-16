package application;

public class PaymentRecord {
    private String date;
    private double amount;
    private String type;

    public PaymentRecord(String date, double amount, String type) {
        this.date = date;
        this.amount = amount;
        this.type = type;
    }

    public String getDate() { return date; }
    public double getAmount() { return amount; }
    public String getType() { return type; }
}
