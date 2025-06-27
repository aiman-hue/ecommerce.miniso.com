package ecommerce;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDataManager {

    private static final String URL = "jdbc:mysql://localhost:3306/e-commerceapp";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // ✅ Add a product with full details
  public static void addProduct(Product p) {
    String getCategoryIdSQL = "SELECT id FROM categories WHERE catname = ?";
    String insertSQL = "INSERT INTO products (name, price, discounted_price, discount_percent, category_id, image_path, about) VALUES (?, ?, ?, ?, ?, ?, ?)";

    try (Connection conn = connect();
         PreparedStatement catStmt = conn.prepareStatement(getCategoryIdSQL);
         PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

        catStmt.setString(1, p.getCategory());
        ResultSet rs = catStmt.executeQuery();

        if (rs.next()) {
            int categoryId = rs.getInt("id");  // corrected column name

            pstmt.setString(1, p.getName());
            pstmt.setDouble(2, p.getPrice());
            pstmt.setDouble(3, p.getDiscountedPrice());
            pstmt.setInt(4, p.getDiscountPercent());
            pstmt.setInt(5, categoryId);            // corrected parameter index & type
            pstmt.setString(6, p.getImagePath());   // corrected parameter index
            pstmt.setString(7, p.getAbout());

            pstmt.executeUpdate();
            System.out.println("✅ Product inserted: " + p.getName());
        } else {
            System.out.println("❌ Category not found: " + p.getCategory());
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }
}



    // ✅ Delete product by name
    public static void deleteProduct(String name) {
        String sql = "DELETE FROM products WHERE name = ?";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
 public static List<String> getCategories() {
        List<String> categories = new ArrayList<>();

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT catname FROM categories")) {

            while (rs.next()) {
                categories.add(rs.getString("catname"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return categories;
    }


   
}
