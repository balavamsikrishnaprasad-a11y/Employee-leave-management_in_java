package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.List;

import model.Admin;
import model.LeaveRequest;
import service.LeaveService;
import util.DataStore;

public class AdminFrame extends JFrame {

    private Admin admin;
    private JTable table;
    private DefaultTableModel tableModel;

    public AdminFrame(Admin admin) {
        this.admin = admin;

        setTitle("Admin Dashboard - " + admin.getName());
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridBagLayout()); // Center layout

        // ===== MAIN CARD PANEL =====
        JPanel mainPanel = new JPanel(new BorderLayout(15,15));
        mainPanel.setPreferredSize(new Dimension(1100,600));
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(15,15,15,15)
        ));
        mainPanel.setBackground(Color.WHITE);

        // ===== TITLE =====
        JLabel title = new JLabel("ADMIN DASHBOARD", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        mainPanel.add(title, BorderLayout.NORTH);

        // ===== TABLE =====
        tableModel = new DefaultTableModel(){
    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
};

        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.getTableHeader().setReorderingAllowed(false);
        JScrollPane scrollPane = new JScrollPane(table);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // ===== BUTTON PANEL =====
        JPanel buttonPanel = new JPanel(new GridLayout(2,4,10,10));

        JButton btnViewEmp = new JButton("View Employees");
        JButton btnCreate = new JButton("Create Employee");
        JButton btnDelete = new JButton("Delete Employee");
        JButton btnPending = new JButton("View Pending Leaves");
        JButton btnApprove = new JButton("Approve Selected Leave");
        JButton btnReject = new JButton("Reject Selected Leave");
        JButton btnDetails = new JButton("View Leave Details");
        JButton btnLogout = new JButton("Logout");

        buttonPanel.add(btnViewEmp);
        buttonPanel.add(btnCreate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnPending);
        buttonPanel.add(btnApprove);
        buttonPanel.add(btnReject);
        buttonPanel.add(btnDetails);
        buttonPanel.add(btnLogout);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel); // centered automatically

        // ===== ACTIONS =====
        btnViewEmp.addActionListener(e -> viewEmployees());
        btnCreate.addActionListener(e -> createEmployee());
        btnDelete.addActionListener(e -> deleteEmployee());
        btnPending.addActionListener(e -> viewPendingLeaves());
        btnApprove.addActionListener(e -> approveSelectedLeave());
        btnReject.addActionListener(e -> rejectSelectedLeave());
        btnDetails.addActionListener(e -> viewLeaveDetails());
        btnLogout.addActionListener(e -> logout());

        setVisible(true);
    }

    private void viewEmployees() {
        tableModel.setColumnIdentifiers(
                new String[]{"ID","Name","Email","Department","Balance"});
        tableModel.setRowCount(0);

        try {
            Connection conn = DataStore.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Employee");

            while(rs.next()){
                tableModel.addRow(new Object[]{
                        rs.getInt("employeeId"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("department"),
                        rs.getInt("totalLeaveBalance")
                });
            }
        } catch(Exception e){
            JOptionPane.showMessageDialog(this,"Error loading employees");
        }
    }

    private void createEmployee() {
        try{
            int id = Integer.parseInt(JOptionPane.showInputDialog("Employee ID"));
            String name = JOptionPane.showInputDialog("Name");
            String email = JOptionPane.showInputDialog("Email");
            String dept = JOptionPane.showInputDialog("Department");
            String pass = JOptionPane.showInputDialog("Password");

            Connection conn = DataStore.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Employee VALUES (?,?,?,?,20,?)");

            ps.setInt(1,id);
            ps.setString(2,name);
            ps.setString(3,email);
            ps.setString(4,dept);
            ps.setString(5,pass);

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this,"Employee Created");
            viewEmployees();

        }catch(Exception e){
            JOptionPane.showMessageDialog(this,"Error creating employee");
        }
    }

    private void deleteEmployee() {
        int row = table.getSelectedRow();
        if(row==-1){
            JOptionPane.showMessageDialog(this,"Select employee first");
            return;
        }

        try{
            int id = (int)tableModel.getValueAt(row,0);

            Connection conn = DataStore.getConnection();
            PreparedStatement ps =
                    conn.prepareStatement("DELETE FROM Employee WHERE employeeId=?");

            ps.setInt(1,id);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this,"Employee Deleted");
            viewEmployees();

        }catch(Exception e){
            JOptionPane.showMessageDialog(this,"Error deleting");
        }
    }

 private void viewPendingLeaves(){

    tableModel.setColumnIdentifiers(
            new String[]{"Leave ID","Employee ID","Name","Days","Status"});

    tableModel.setRowCount(0); // Clear old rows

    List<LeaveRequest> list =
            new LeaveService().getPendingLeaves();

    for(LeaveRequest l : list){
        tableModel.addRow(new Object[]{
                l.getLeaveId(),
                l.getEmployeeId(),
                l.getEmployeeName(),
                l.getNumberOfDays(),
                l.getStatus()
        });
    }
}


    private void approveSelectedLeave(){
        int row = table.getSelectedRow();
        if(row==-1){
            JOptionPane.showMessageDialog(this,"Select leave first");
            return;
        }

        int leaveId = (int)tableModel.getValueAt(row,0);
        new LeaveService().approveLeave(leaveId, admin.getName());

        JOptionPane.showMessageDialog(this,"Approved");
        viewPendingLeaves();
    }

    private void rejectSelectedLeave(){
        int row = table.getSelectedRow();
        if(row==-1){
            JOptionPane.showMessageDialog(this,"Select leave first");
            return;
        }

        int leaveId = (int)tableModel.getValueAt(row,0);
        new LeaveService().rejectLeave(leaveId, admin.getName(), false);

        JOptionPane.showMessageDialog(this,"Rejected");
        viewPendingLeaves();
    }

private void viewLeaveDetails() {

    int selectedRow = table.getSelectedRow();

    if(selectedRow == -1){
        JOptionPane.showMessageDialog(this,
                "Select a leave first");
        return;
    }

    int leaveId = (int) tableModel.getValueAt(selectedRow, 0);

    LeaveRequest leave =
            new LeaveService().getLeaveById(leaveId);

    if(leave != null){
        JOptionPane.showMessageDialog(this,
                "Leave ID: " + leave.getLeaveId() +
                "\nEmployee ID: " + leave.getEmployeeId() +
                "\nName: " + leave.getEmployeeName() +
                "\nStart Date: " + leave.getStartDate() +
                "\nEnd Date: " + leave.getEndDate() +
                "\nDays: " + leave.getNumberOfDays() +
                "\nType: " + leave.getLeaveType() +
                "\nReason: " + leave.getReason() +
                "\nStatus: " + leave.getStatus(),
                "Leave Details",
                JOptionPane.INFORMATION_MESSAGE);
    }
}

    private void logout(){
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure to logout?",
                "Logout",
                JOptionPane.YES_NO_OPTION);

        if(confirm==JOptionPane.YES_OPTION){
            new LoginFrame();
            dispose();
        }
    }
}
