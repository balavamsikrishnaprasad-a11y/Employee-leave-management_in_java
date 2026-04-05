package model;

public class Employee {
    private int employeeId;
    private String name;
    private String email;
    private String department;
    private int totalLeaveBalance;
    private String password;

    public Employee(int employeeId, String name, String email, String department, int totalLeaveBalance, String password) {
        this.employeeId = employeeId;
        this.name = name;
        this.email = email;
        this.department = department;
        this.totalLeaveBalance = totalLeaveBalance;
        this.password = password;
    }

    public Employee() {
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getTotalLeaveBalance() {
        return totalLeaveBalance;
    }

    public void setTotalLeaveBalance(int totalLeaveBalance) {
        this.totalLeaveBalance = totalLeaveBalance;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "employeeId=" + employeeId +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", department='" + department + '\'' +
                ", totalLeaveBalance=" + totalLeaveBalance +
                '}';
    }
}
