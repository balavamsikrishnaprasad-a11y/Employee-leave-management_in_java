package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

import model.Employee;
import model.LeaveRequest;
import service.EmployeeService;
import service.LeaveService;

public class EmployeeFrame extends JFrame {

    private Employee emp;
    private JTable table;
    private DefaultTableModel tableModel;

    public EmployeeFrame(Employee emp) {

        this.emp = emp;

        setTitle("Employee Dashboard - " + emp.getName());
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridBagLayout()); // Center everything

        // ===== MAIN CARD PANEL =====
        JPanel mainPanel = new JPanel(new BorderLayout(15,15));
        mainPanel.setPreferredSize(new Dimension(1000,550));
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(15,15,15,15)
        ));
        mainPanel.setBackground(Color.WHITE);

        // ===== TITLE =====
        JLabel title = new JLabel(
                "EMPLOYEE DASHBOARD - Welcome " + emp.getName(),
                SwingConstants.CENTER);

        title.setFont(new Font("Arial", Font.BOLD, 20));
        mainPanel.add(title, BorderLayout.NORTH);

        // ===== TABLE =====
        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // ===== BUTTON PANEL =====
        JPanel buttonPanel = new JPanel(new GridLayout(1,5,10,10));

        JButton btnApply = new JButton("Apply Leave");
        JButton btnHistory = new JButton("View Leave History");
        JButton btnBalance = new JButton("Check Balance");
        JButton btnDetails = new JButton("My Details");
        JButton btnLogout = new JButton("Logout");

        buttonPanel.add(btnApply);
        buttonPanel.add(btnHistory);
        buttonPanel.add(btnBalance);
        buttonPanel.add(btnDetails);
        buttonPanel.add(btnLogout);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel); // centered automatically

        // ===== ACTIONS =====

        btnApply.addActionListener(e -> applyLeave());
        btnHistory.addActionListener(e -> viewHistory());
        btnBalance.addActionListener(e -> checkBalance());
        btnDetails.addActionListener(e -> showDetails());
        btnLogout.addActionListener(e -> logout());

        setVisible(true);
    }

    // ================= APPLY LEAVE =================
    private void applyLeave() {

        try {
            String type = JOptionPane.showInputDialog(
                    "Leave Type (Sick/Casual/Earned)");

            LocalDate start = LocalDate.parse(
                    JOptionPane.showInputDialog("Start Date (yyyy-MM-dd)"));

            LocalDate end = LocalDate.parse(
                    JOptionPane.showInputDialog("End Date (yyyy-MM-dd)"));

            String reason =
                    JOptionPane.showInputDialog("Reason");

            long days =
                    java.time.temporal.ChronoUnit.DAYS.between(start,end) + 1;

            LeaveRequest lr = new LeaveRequest();
            lr.setEmployeeId(emp.getEmployeeId());
            lr.setEmployeeName(emp.getName());
            lr.setStartDate(start);
            lr.setEndDate(end);
            lr.setNumberOfDays((int)days);
            lr.setLeaveType(type);
            lr.setReason(reason);

            new LeaveService().submitLeaveRequest(lr);

            JOptionPane.showMessageDialog(this,
                    "Leave Request Submitted");

        } catch(Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Invalid Input Format");
        }
    }

    // ================= VIEW HISTORY =================
    private void viewHistory() {

        tableModel.setColumnIdentifiers(
                new String[]{"Leave ID","Type","Days","Status"});
        tableModel.setRowCount(0);

        List<LeaveRequest> list =
                new LeaveService()
                        .getEmployeeLeaveHistory(emp.getEmployeeId());

        for(LeaveRequest l : list){
            tableModel.addRow(new Object[]{
                    l.getLeaveId(),
                    l.getLeaveType(),
                    l.getNumberOfDays(),
                    l.getStatus()
            });
        }
    }

    // ================= CHECK BALANCE =================
    private void checkBalance() {

        Employee updated =
                new EmployeeService()
                        .getEmployeeDetails(emp.getEmployeeId());

        JOptionPane.showMessageDialog(this,
                "Available Leave Balance: "
                        + updated.getTotalLeaveBalance());
    }

    // ================= SHOW DETAILS =================
    private void showDetails() {

        JOptionPane.showMessageDialog(this,
                "Employee ID: " + emp.getEmployeeId() +
                "\nName: " + emp.getName() +
                "\nEmail: " + emp.getEmail() +
                "\nDepartment: " + emp.getDepartment());
    }

    // ================= LOGOUT =================
    private void logout() {

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure to logout?",
                "Logout",
                JOptionPane.YES_NO_OPTION);

        if(confirm == JOptionPane.YES_OPTION){
            new LoginFrame();
            dispose();
        }
    }
}
