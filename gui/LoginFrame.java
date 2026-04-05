package gui;

import javax.swing.*;
import java.awt.*;

import model.Admin;
import model.Employee;
import service.AdminService;
import service.EmployeeService;

public class LoginFrame extends JFrame {

    private JTextField txtUser;
    private JPasswordField txtPass;
    private JComboBox<String> roleBox;

    public LoginFrame() {

        setTitle("HRMS Login");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Full screen
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridBagLayout()); // This keeps panel centered

        // ======= Main Login Panel =======
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(450, 350));
        panel.setLayout(new GridLayout(5,2,10,10));
        panel.setBorder(BorderFactory.createTitledBorder("Login"));

        panel.add(new JLabel("Role:"));
        roleBox = new JComboBox<>(new String[]{"Admin","Employee"});
        panel.add(roleBox);

        panel.add(new JLabel("Email / Employee ID:"));
        txtUser = new JTextField();
        panel.add(txtUser);

        panel.add(new JLabel("Password:"));
        txtPass = new JPasswordField();
        panel.add(txtPass);

        JButton btnLogin = new JButton("Login");
        JButton btnExit = new JButton("Exit");

        panel.add(btnLogin);
        panel.add(btnExit);

        add(panel); // centered automatically

        // ======= Actions =======
        btnLogin.addActionListener(e -> login());
        btnExit.addActionListener(e -> System.exit(0));

        setVisible(true);
    }

    private void login() {

        String role = roleBox.getSelectedItem().toString();
        String user = txtUser.getText();
        String pass = new String(txtPass.getPassword());

        if(role.equals("Admin")) {

            Admin admin = new AdminService().authenticateAdmin(user, pass);

            if(admin != null) {
                new AdminFrame(admin);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,"Invalid Admin Login");
            }

        } else {

            try {
                int id = Integer.parseInt(user);
                Employee emp =
                        new EmployeeService().authenticateEmployee(id, pass);

                if(emp != null) {
                    new EmployeeFrame(emp);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this,"Invalid Employee Login");
                }

            } catch(Exception e) {
                JOptionPane.showMessageDialog(this,"Employee ID must be number");
            }
        }
    }
}
