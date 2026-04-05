package service;

import model.Admin;
import util.DataStore;

import java.sql.*;

public class AdminService {
    private Connection connection;

    public AdminService() {
        this.connection = DataStore.getConnection();
    }

    public Admin authenticateAdmin(String email, String password) {
        try {
            String sql = "SELECT * FROM Admin WHERE email = ? AND password = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Admin admin = new Admin();
                admin.setAdminId(rs.getInt("adminId"));
                admin.setName(rs.getString("name"));
                admin.setEmail(rs.getString("email"));
                return admin;
            }
        } catch (SQLException e) {
            System.out.println("Authentication error: " + e.getMessage());
        }
        return null;
    }

    public Admin getAdminDetails(int adminId) {
        try {
            String sql = "SELECT * FROM Admin WHERE adminId = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, adminId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Admin admin = new Admin();
                admin.setAdminId(rs.getInt("adminId"));
                admin.setName(rs.getString("name"));
                admin.setEmail(rs.getString("email"));
                return admin;
            }
        } catch (SQLException e) {
            System.out.println("Error fetching admin details: " + e.getMessage());
        }
        return null;
    }
}
