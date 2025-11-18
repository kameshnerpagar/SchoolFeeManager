# 🎓 School Fee Manager — JavaFX + MySQL

A complete fee-management desktop application built using JavaFX, MySQL, and JDBC. It supports Admin & Student roles, real-time fee updates, payment history, notifications, and a clean modern UI.

---

## 🚀 Features

### 👨‍🏫 Admin Panel

* Add new students
* Update existing students
* Delete students (auto-delete linked user account)
* Manage announcements
* View top fee defaulters
* Real-time summary cards:

  * Total Students
  * Total Collection
  * Pending Amount
* Notification system:

  * Due soon (within 3 days)
  * Overdue students
* Full student table (name, class, total fee, paid fee, balance, due date, status)
* Modern UI with rounded cards and shadows

### 👨‍🎓 Student Panel

* Login using auto-generated credentials
* View total fee, paid fee, balance and status
* Read announcements
* See due-date notifications
* Make fee payments (transaction safe)
* View payment history

---

## 🛠️ Tech Stack

| Layer         | Technology                     |
| ------------- | ------------------------------ |
| UI / Frontend | JavaFX (FXML + CSS)            |
| Backend       | Java (OOP + JDBC)              |
| Database      | MySQL                          |

---

## 📁 Project Structure

```
src/
 ├── application/
 │    ├── MainApp.java
 │    ├── DBUtil.java
 │    ├── AppState.java
 │    ├── Student.java
 │    ├── PaymentRecord.java
 │    ├── controller/
 │    │      ├── LoginController.java
 │    │      ├── AdminDashboardController.java
 │    │      ├── StudentDashboardController.java
 │    │      ├── AddStudentController.java
 │    │      ├── EditStudentController.java
 │    │      └── PayFeeController.java
 │    └── resources/
 │           ├── login.fxml
 │           ├── admin_dashboard.fxml
 │           ├── student_dashboard.fxml
 │           ├── add_student.fxml
 │           ├── edit_student.fxml
 │           ├── pay_fee_popup.fxml
 │           ├── announcement_popup.fxml
 │           └── style.css
```

---

## 🗄️ Database Schema

### **Students**

* id
* name
* student_class
* total_fee
* paid_fee
* balance
* status (Paid / Due)
* due_date

### **Users**

* user_id
* username (unique)
* password
* role (ADMIN / STUDENT)
* linked_student_id
* full_name
* created_at

### **Payments**

* payment_id
* student_id
* amount
* method
* payment_date

### **Announcements**

* id
* message
* created_at

---

## 🖥️ Setup & Installation

1. Install Java JDK 17+
2. Install JavaFX SDK
3. Install MySQL + Workbench
4. Configure IntelliJ for JavaFX
5. Update DBUtil with your MySQL credentials

```
URL = jdbc:mysql://localhost:3306/dbname
USER = root
PASSWORD = yourpassword
```

6. Import SQL tables
7. Run MainApp.java

### Default Login

**Admin**

* username: admin
* password: admin123

**Student**

* username auto-generated as `name123`

---
