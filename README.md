📌 Employee Leave Management System (Java + SQLite)
📖 Overview

This project is a desktop-based Employee Leave Management System developed using Java (Swing GUI) and SQLite database. It allows employees to request leaves and administrators to manage and approve/reject them efficiently.

🚀 Features
👨‍💼 Admin
Login authentication
View all employees
View leave requests
Approve / Reject leave requests
👩‍💻 Employee
Login authentication
Apply for leave
View leave status
🛠️ Tech Stack
Java (Core + Swing) – UI & logic
SQLite – Lightweight database
JDBC (sqlite-jdbc.jar) – Database connectivity
📂 Project Structure
├── Main.java
├── gui/
│   ├── LoginFrame.java
│   ├── AdminFrame.java
│   └── EmployeeFrame.java
├── model/
│   ├── Admin.java
│   ├── Employee.java
│   └── LeaveRequest.java
├── service/
│   ├── AdminService.java
│   ├── EmployeeService.java
│   └── LeaveService.java
├── util/
│   └── DataStore.java
├── hrms.db
├── sqlite-jdbc.jar
⚙️ How to Run the Project
1️⃣ Clone the Repository
git clone https://github.com/your-username/your-repo-name.git
cd your-repo-name
2️⃣ Compile the Project
javac -cp ".;sqlite-jdbc.jar" Main.java
3️⃣ Run the Application
java -cp ".;sqlite-jdbc.jar" Main
🗄️ Database
The project uses SQLite database (hrms.db)
It automatically connects using JDBC
Ensure these files are present:
hrms.db
hrms.db-shm
hrms.db-wal
⚠️ Important Notes
.class files are already compiled — but it’s recommended to compile again.
Ensure Java (JDK 8 or above) is installed.
If running on Linux/Mac, replace ; with : in classpath.

Example:

java -cp ".:sqlite-jdbc.jar" Main
📌 Future Enhancements
Web-based version (Spring Boot / React)
Role-based authentication
Email notifications
Dashboard analytics
🤝 Contribution

Feel free to fork the repository and contribute improvements!
