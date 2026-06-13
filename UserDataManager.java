/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ecommerce;

import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class UserDataManager {

    // Replace with your DB details
    static final String DB_URL = (
    "jdbc:mysql://localhost:3306/e-commerceapp");
;  // Your database name
    static final String DB_USERNAME = "root";
    static final String DB_PASSWORD = "";
    static final String DB_EMAIL = "";
    static final String DB_ROLE = "";
    
    

    // Only needed if you're using in-memory maps (optional now)
    private static final HashMap<String, String> users = new HashMap<>();

    // ✅ Check if user exists by username OR email
    public static boolean checkUserExists(String username, String email) {
        String sql = "SELECT * FROM users WHERE username = ? OR email = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL,DB_USERNAME,DB_EMAIL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // user exists if a row is found
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ✅ Register new user
    public static boolean registerUser(String username, String email, String password) {
        String sql = "INSERT INTO users (username, email, password, role,gender) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, password); // You should hash this in real apps
            pstmt.setString(4, "user");   // default role as user
            pstmt.setString(5, "");

            int rowsInserted = pstmt.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ✅ Validate login credentials
    public static boolean validateUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ✅ Get user role (admin/user)
    public static String getUserRole(String username, String password) {
        String sql = "SELECT role FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("role");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return "user";
    }
    
    public static String getadminRole(String username, String password) {
        String sql = "SELECT role FROM users WHERE username = admin AND password = admin123";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }
    
    public static boolean userExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // Returns true if any record exists

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    static void saveUser(String username, String password, String email, String gender, String role) {
        String sql = "INSERT INTO users (username, password, email, role, gender) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, email);
            pstmt.setString(4, role);
            pstmt.setString(5, gender);

            pstmt.executeUpdate();
            System.out.println("User registered successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ============================================
    // NEW METHODS FOR ADMIN DASHBOARD
    // ============================================
    
    // ✅ Get all users (for admin panel)
    public static ResultSet getAllUsers() {
        String sql = "SELECT id, username, email, role, gender, created_at FROM users ORDER BY id DESC";
        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            PreparedStatement stmt = conn.prepareStatement(sql);
            return stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    // ✅ Update user role (admin only)
    public static boolean updateUserRole(String username, String newRole) {
        String sql = "UPDATE users SET role = ? WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newRole);
            stmt.setString(2, username);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // ✅ Delete user (admin only, cannot delete admin)
    public static boolean deleteUser(String username) {
        String sql = "DELETE FROM users WHERE username = ? AND role != 'admin'";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // ✅ Get user by username (returns map of user info)
    public static java.util.Map<String, String> getUserByUsername(String username) {
        String sql = "SELECT username, email, role, gender FROM users WHERE username = ?";
        java.util.Map<String, String> userInfo = new java.util.HashMap<>();
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                userInfo.put("username", rs.getString("username"));
                userInfo.put("email", rs.getString("email"));
                userInfo.put("role", rs.getString("role"));
                userInfo.put("gender", rs.getString("gender"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userInfo;
    }
    
    // ✅ Update user profile
    public static boolean updateUserProfile(String username, String email, String password) {
        String sql = "UPDATE users SET email = ?, password = ? WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            stmt.setString(3, username);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}