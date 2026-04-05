package service;

import model.Employee;
import util.DataStore;

import java.sql.*;

public class EmployeeService {
    private Connection connection;

    public EmployeeService() {
        this.connection = DataStore.getConnection();
    }

    public Employee authenticateEmployee(int employeeId, String password) {
        String sql = "SELECT * FROM Employee WHERE employeeId = ? AND password = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, employeeId);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Employee emp = new Employee();
                    emp.setEmployeeId(rs.getInt("employeeId"));
                    emp.setName(rs.getString("name"));
                    emp.setEmail(rs.getString("email"));
                    emp.setDepartment(rs.getString("department"));
                    emp.setTotalLeaveBalance(rs.getInt("totalLeaveBalance"));
                    return emp;
                }
            }
        } catch (SQLException e) {
            System.out.println("Authentication error: " + e.getMessage());
        }
        return null;
    }

    public Employee getEmployeeDetails(int employeeId) {
        String sql = "SELECT * FROM Employee WHERE employeeId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, employeeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Employee emp = new Employee();
                    emp.setEmployeeId(rs.getInt("employeeId"));
                    emp.setName(rs.getString("name"));
                    emp.setEmail(rs.getString("email"));
                    emp.setDepartment(rs.getString("department"));
                    emp.setTotalLeaveBalance(rs.getInt("totalLeaveBalance"));
                    return emp;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching employee: " + e.getMessage());
        }
        return null;
    }

    public boolean updateLeaveBalance(int employeeId, int newBalance) {
        String sql = "UPDATE Employee SET totalLeaveBalance = ? WHERE employeeId = ?";
        int attempts = 0;
        int maxAttempts = 5;
        while (attempts < maxAttempts) {
            attempts++;
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, newBalance);
                pstmt.setInt(2, employeeId);
                int result = pstmt.executeUpdate();
                return result > 0;
            } catch (SQLException e) {
                String msg = e.getMessage() == null ? "" : e.getMessage();
                System.out.println("Error updating leave balance: " + msg);
                if (msg.contains("database is locked") && attempts < maxAttempts) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    continue;
                }
                break;
            }
        }
        return false;
    }

    public boolean validateEmployeeId(int employeeId) {
        String sql = "SELECT COUNT(*) FROM Employee WHERE employeeId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, employeeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error validating employee: " + e.getMessage());
        }
        return false;
    }
}
