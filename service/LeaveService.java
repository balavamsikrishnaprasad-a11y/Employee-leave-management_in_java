package service;

import model.LeaveRequest;
import util.DataStore;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class LeaveService {
    private Connection connection;

    public LeaveService() {
        this.connection = DataStore.getConnection();
    }

    public boolean submitLeaveRequest(LeaveRequest leave) {
        // Validation
        if (!validateLeaveRequest(leave)) {
            return false;
        }

        try {
            String sql = "INSERT INTO LeaveRequest (employeeId, employeeName, startDate, endDate, " +
                    "numberOfDays, leaveType, reason, status, applicationDate) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, leave.getEmployeeId());
                pstmt.setString(2, leave.getEmployeeName());
                pstmt.setString(3, leave.getStartDate().toString());
                pstmt.setString(4, leave.getEndDate().toString());
                pstmt.setInt(5, leave.getNumberOfDays());
                pstmt.setString(6, leave.getLeaveType());
                pstmt.setString(7, leave.getReason());
                pstmt.setString(8, "Pending");
                pstmt.setString(9, LocalDate.now().toString());

                int result = pstmt.executeUpdate();
                if (result > 0) {
                    System.out.println("✓ Leave request submitted successfully!");
                    return true;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error submitting leave: " + e.getMessage());
        }
        return false;
    }

    private boolean validateLeaveRequest(LeaveRequest leave) {
        // Validate employee ID
        EmployeeService empService = new EmployeeService();
        if (!empService.validateEmployeeId(leave.getEmployeeId())) {
            System.out.println("✗ Invalid Employee ID!");
            return false;
        }

        // Validate dates
        if (leave.getStartDate().isAfter(leave.getEndDate())) {
            System.out.println("✗ Start date cannot be after end date!");
            return false;
        }

        if (leave.getStartDate().isBefore(LocalDate.now())) {
            System.out.println("✗ Leave start date cannot be in the past!");
            return false;
        }

        // Validate leave days
        if (leave.getNumberOfDays() <= 0) {
            System.out.println("✗ Number of days must be greater than 0!");
            return false;
        }

        long days = ChronoUnit.DAYS.between(leave.getStartDate(), leave.getEndDate()) + 1;
        if (leave.getNumberOfDays() != days) {
            System.out.println("✗ Number of days doesn't match the date range!");
            return false;
        }

        // Validate leave balance
        if (leave.getNumberOfDays() > 30) {
            System.out.println("✗ Leave request exceeds 30 days!");
            return false;
        }

        // Check leave type
        if (!leave.getLeaveType().equalsIgnoreCase("Sick") && 
            !leave.getLeaveType().equalsIgnoreCase("Casual") && 
            !leave.getLeaveType().equalsIgnoreCase("Earned")) {
            System.out.println("✗ Invalid leave type! Use: Sick, Casual, or Earned");
            return false;
        }

        System.out.println("✓ Leave request validation passed!");
        return true;
    }

    public List<LeaveRequest> getPendingLeaves() {
        List<LeaveRequest> leaves = new ArrayList<>();
        String sql = "SELECT * FROM LeaveRequest WHERE status = 'Pending'";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                leaves.add(extractLeaveRequest(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching pending leaves: " + e.getMessage());
        }
        return leaves;
    }

    public List<LeaveRequest> getEmployeeLeaveHistory(int employeeId) {
        List<LeaveRequest> leaves = new ArrayList<>();
        String sql = "SELECT * FROM LeaveRequest WHERE employeeId = ? ORDER BY applicationDate DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, employeeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    leaves.add(extractLeaveRequest(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching leave history: " + e.getMessage());
        }
        return leaves;
    }

    public List<LeaveRequest> getPendingLeavesByEmployee(int employeeId) {
        List<LeaveRequest> leaves = new ArrayList<>();
        String sql = "SELECT * FROM LeaveRequest WHERE employeeId = ? AND status = 'Pending' ORDER BY applicationDate ASC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, employeeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    leaves.add(extractLeaveRequest(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching pending leaves for employee: " + e.getMessage());
        }
        return leaves;
    }

    /**
     * Reset employee balance (optional) and approve all pending leaves for that employee.
     * Uses a dedicated connection to isolate from shared connection locks.
     * Returns number of approved leaves, or -1 on error.
     */
    public int resetBalanceAndApprovePending(int employeeId, String adminName, boolean resetTo20) {
        int approvedCount = 0;
        Connection dedicatedConn = null;

        try {
            // Use dedicated connection to avoid shared connection locks
            dedicatedConn = util.DataStore.getNewConnection();

            // Step 1: Optionally reset balance to 20 using dedicated connection
            if (resetTo20) {
                String updateSql = "UPDATE Employee SET totalLeaveBalance = 20 WHERE employeeId = ?";
                try (PreparedStatement pstmt = dedicatedConn.prepareStatement(updateSql)) {
                    pstmt.setInt(1, employeeId);
                    int result = pstmt.executeUpdate();
                    if (result > 0) {
                        System.out.println("✓ Balance reset to 20.");
                    } else {
                        System.out.println("✗ Employee not found for balance reset.");
                        return -1;
                    }
                }
                Thread.sleep(100); // Brief pause to let DB settle
            }

            // Step 2: Fetch pending leaves using dedicated connection
            List<LeaveRequest> pending = new ArrayList<>();
            String selectSql = "SELECT leaveId, numberOfDays FROM LeaveRequest WHERE employeeId = ? AND status = 'Pending' ORDER BY applicationDate ASC";
            try (PreparedStatement pstmt = dedicatedConn.prepareStatement(selectSql)) {
                pstmt.setInt(1, employeeId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        LeaveRequest lr = new LeaveRequest();
                        lr.setLeaveId(rs.getInt("leaveId"));
                        lr.setNumberOfDays(rs.getInt("numberOfDays"));
                        pending.add(lr);
                    }
                }
            }

            if (pending.isEmpty()) {
                System.out.println("✓ No pending leaves to approve.");
                return 0;
            }

            // Step 3: Approve each leave using shared connection (original approveLeave method)
            for (LeaveRequest leave : pending) {
                if (approveLeave(leave.getLeaveId(), adminName)) {
                    approvedCount++;
                }
                Thread.sleep(50); // Brief pause between approvals to avoid lock contention
            }

            return approvedCount;
        } catch (SQLException e) {
            System.out.println("✗ Error: " + (e.getMessage() != null ? e.getMessage() : e.toString()));
            return -1;
        } catch (InterruptedException ignore) {
            Thread.currentThread().interrupt();
            return approvedCount;
        } finally {
            if (dedicatedConn != null) {
                try {
                    dedicatedConn.close();
                } catch (SQLException ignore) {
                }
            }
        }
    }

    public boolean approveLeave(int leaveId, String adminName) {
        String sql = "UPDATE LeaveRequest SET status = 'Approved', approvedBy = ? WHERE leaveId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, adminName);
            pstmt.setInt(2, leaveId);

            int result = pstmt.executeUpdate();
            if (result > 0) {
                // Update employee leave balance
                LeaveRequest leave = getLeaveById(leaveId);
                if (leave != null) {
                    EmployeeService empService = new EmployeeService();
                    int currentBalance = empService.getEmployeeDetails(leave.getEmployeeId()).getTotalLeaveBalance();
                    int newBalance = currentBalance - leave.getNumberOfDays();
                    empService.updateLeaveBalance(leave.getEmployeeId(), newBalance);
                }
                System.out.println("✓ Leave approved successfully!");
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error approving leave: " + e.getMessage());
        }
        return false;
    }

    public boolean rejectLeave(int leaveId, String adminName) {
        // Fetch existing leave to check its current status and days
        LeaveRequest leave = getLeaveById(leaveId);
        if (leave == null) {
            System.out.println("✗ Leave request not found!");
            return false;
        }

        String sql = "UPDATE LeaveRequest SET status = 'Rejected', approvedBy = ? WHERE leaveId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, adminName);
            pstmt.setInt(2, leaveId);

            int result = pstmt.executeUpdate();
            if (result > 0) {
                // If the leave was previously approved, restore the employee's leave balance
                if ("Approved".equalsIgnoreCase(leave.getStatus())) {
                    EmployeeService empService = new EmployeeService();
                    int currentBalance = empService.getEmployeeDetails(leave.getEmployeeId()).getTotalLeaveBalance();
                    int newBalance = currentBalance + leave.getNumberOfDays();
                    empService.updateLeaveBalance(leave.getEmployeeId(), newBalance);
                    System.out.println("✓ Leave rejected and balance restored!");
                } else {
                    System.out.println("✓ Leave rejected successfully!");
                }
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error rejecting leave: " + e.getMessage());
        }
        return false;
    }

    private LeaveRequest extractLeaveRequest(ResultSet rs) throws SQLException {
        LeaveRequest leave = new LeaveRequest();
        leave.setLeaveId(rs.getInt("leaveId"));
        leave.setEmployeeId(rs.getInt("employeeId"));
        leave.setEmployeeName(rs.getString("employeeName"));
        leave.setStartDate(LocalDate.parse(rs.getString("startDate")));
        leave.setEndDate(LocalDate.parse(rs.getString("endDate")));
        leave.setNumberOfDays(rs.getInt("numberOfDays"));
        leave.setLeaveType(rs.getString("leaveType"));
        leave.setReason(rs.getString("reason"));
        leave.setStatus(rs.getString("status"));
        leave.setApplicationDate(LocalDate.parse(rs.getString("applicationDate")));
        leave.setApprovedBy(rs.getString("approvedBy"));
        return leave;
    }

    public LeaveRequest getLeaveById(int leaveId) {
        String sql = "SELECT * FROM LeaveRequest WHERE leaveId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, leaveId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractLeaveRequest(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching leave: " + e.getMessage());
        }
        return null;
    }

    /**
     * Reject leave with option to reset the employee's leave balance to a default value (20).
     */
    public boolean rejectLeave(int leaveId, String adminName, boolean resetToDefault) {
        try {
            // Fetch existing leave to check its current status and employee
            LeaveRequest leave = getLeaveById(leaveId);
            if (leave == null) {
                System.out.println("✗ Leave request not found!");
                return false;
            }

            String sql = "UPDATE LeaveRequest SET status = 'Rejected', approvedBy = ? WHERE leaveId = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, adminName);
            pstmt.setInt(2, leaveId);

            int result = pstmt.executeUpdate();
            if (result > 0) {
                EmployeeService empService = new EmployeeService();
                if (resetToDefault) {
                    // Reset to the standard allocation (20)
                    empService.updateLeaveBalance(leave.getEmployeeId(), 20);
                    System.out.println("✓ Leave rejected and balance reset to 20!");
                } else {
                    // If previously approved, restore the deducted days
                    if ("Approved".equalsIgnoreCase(leave.getStatus())) {
                        int currentBalance = empService.getEmployeeDetails(leave.getEmployeeId()).getTotalLeaveBalance();
                        int newBalance = currentBalance + leave.getNumberOfDays();
                        empService.updateLeaveBalance(leave.getEmployeeId(), newBalance);
                        System.out.println("✓ Leave rejected and balance restored!");
                    } else {
                        System.out.println("✓ Leave rejected successfully!");
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error rejecting leave: " + e.getMessage());
        }
        return false;
    }
}
