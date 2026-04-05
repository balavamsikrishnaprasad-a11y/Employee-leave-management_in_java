package model;

import java.time.LocalDate;

public class LeaveRequest {
    private int leaveId;
    private int employeeId;
    private String employeeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private int numberOfDays;
    private String leaveType; // Sick, Casual, Earned
    private String reason;
    private String status; // Pending, Approved, Rejected
    private LocalDate applicationDate;
    private String approvedBy;

    public LeaveRequest(int leaveId, int employeeId, String employeeName, LocalDate startDate, 
                       LocalDate endDate, int numberOfDays, String leaveType, String reason, 
                       String status, LocalDate applicationDate, String approvedBy) {
        this.leaveId = leaveId;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.numberOfDays = numberOfDays;
        this.leaveType = leaveType;
        this.reason = reason;
        this.status = status;
        this.applicationDate = applicationDate;
        this.approvedBy = approvedBy;
    }

    public LeaveRequest() {
    }

    public int getLeaveId() {
        return leaveId;
    }

    public void setLeaveId(int leaveId) {
        this.leaveId = leaveId;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public int getNumberOfDays() {
        return numberOfDays;
    }

    public void setNumberOfDays(int numberOfDays) {
        this.numberOfDays = numberOfDays;
    }

    public String getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(String leaveType) {
        this.leaveType = leaveType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(LocalDate applicationDate) {
        this.applicationDate = applicationDate;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    @Override
    public String toString() {
        return "LeaveRequest{" +
                "leaveId=" + leaveId +
                ", employeeId=" + employeeId +
                ", employeeName='" + employeeName + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", numberOfDays=" + numberOfDays +
                ", leaveType='" + leaveType + '\'' +
                ", reason='" + reason + '\'' +
                ", status='" + status + '\'' +
                ", applicationDate=" + applicationDate +
                ", approvedBy='" + approvedBy + '\'' +
                '}';
    }
}
