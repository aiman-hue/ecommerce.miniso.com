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
    static final String DB_URL =(
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
    String sql = "INSERT INTO users (username, email, password, role,gender) VALUES (?, ?, ?, ?,?)";

    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setString(1, username);
        pstmt.setString(2, email);
        pstmt.setString(3, password); // You should hash this in real apps
        pstmt.setString(4, "user");   // default role as user

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

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }return null;
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
        }return null;
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
        String sql = "INSERT INTO users (username, password, email, role,gender) VALUES (?, ?, ?, ?, ?)";
    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD );
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
   }}
}
 
