/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ecommerce;

import com.mysql.cj.x.protobuf.MysqlxCrud;
import ecommerce.Product;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    public static Connection connect() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/e-commerceapp";
        String user = "root";
        String password = "";
        return DriverManager.getConnection(url, user, password);
    }

   

    public static List<String> getCategories() {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT catname FROM categories";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                categories.add(rs.getString("catname"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return categories;
    }

    public static void addCategory(String catname) {
        String sql = "INSERT INTO categories (catname) VALUES (?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, catname);
            pstmt.executeUpdate();
            System.out.println("✅ Category added: " + catname);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteCategory(String catname) {
        String sql = "DELETE FROM categories WHERE catname = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, catname);
            pstmt.executeUpdate();
            System.out.println("❌ Category deleted: " + catname);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

   

    public static void addProduct(Product p) {
        String getCategoryIdSQL = "SELECT id FROM categories WHERE catname = ?";
        String insertProductSQL = "INSERT INTO products (name, price, discounted_price, discount_percent, image_path, category_id, about) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = connect();
             PreparedStatement getCatStmt = conn.prepareStatement(getCategoryIdSQL);
             PreparedStatement insertStmt = conn.prepareStatement(insertProductSQL)) {

            getCatStmt.setString(1, p.getCategory());
            ResultSet rs = getCatStmt.executeQuery();

            if (rs.next()) {
                int category_id = rs.getInt("id");

                insertStmt.setString(1, p.getName());
                insertStmt.setDouble(2, p.getPrice());
                insertStmt.setDouble(3, p.getDiscountedPrice());
                insertStmt.setDouble(4, p.getDiscountPercent());
                insertStmt.setString(5, p.getImagePath());
                insertStmt.setInt(6, category_id);
                insertStmt.setString(7, p.getAbout());
                insertStmt.executeUpdate();

                System.out.println("✅ Product added: " + p.getName());
            } else {
                System.out.println("❌ Category not found: " + p.getCategory());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
     public static List<String> loadCategories() {
    List<String> categories = new ArrayList<>();
    String sql = "SELECT catname FROM categories";

    try (Connection conn = connect();
         PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {

        while (rs.next()) {
            categories.add(rs.getString("catname"));
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return categories;
}

    public static List<Product> loadProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.id, p.name, p.price, p.discounted_price, p.discount_percent, " +
                     "p.image_path, p.about, c.catname AS category " +
                     "FROM products p LEFT JOIN categories c ON p.category_id = c.id";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Product newProduct = new Product(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getDouble("price"),
                    rs.getString("category"),
                    rs.getDouble("discounted_price"),
                    rs.getInt("discount_percent"),
                    rs.getString("image_path"),
                    rs.getString("about")
                );
                products.add(newProduct);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return products;
    }

    public static void deleteProduct(String productName) {
        String sql = "DELETE FROM products WHERE name = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, productName);
            pstmt.executeUpdate();
            System.out.println("❌ Product deleted: " + productName);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ----------- Cart & Wishlist -----------

    public static void addToCart(String username, int productId) {
        String sql = "INSERT INTO user_cart (username, product_id) VALUES (?, ?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setInt(2, productId);
            pstmt.executeUpdate();
            System.out.println("🛒 Added to cart: " + productId + " for " + username);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addToWishlist(String username, int productId) {
        String sql = "INSERT INTO user_wishlist (username, product_id) VALUES (?, ?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setInt(2, productId);
            pstmt.executeUpdate();
            System.out.println("💖 Added to wishlist: " + productId + " for " + username);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removeFromCart(String username, int productId) {
        String sql = "DELETE FROM user_cart WHERE username = ? AND product_id = ?";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setInt(2, productId);
            stmt.executeUpdate();
            System.out.println("🛒 Removed from cart: " + productId + " for " + username);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removeFromWishlist(String username, int productId) {
        String sql = "DELETE FROM user_wishlist WHERE username = ? AND product_id = ?";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setInt(2, productId);
            stmt.executeUpdate();
            System.out.println("💖 Removed from wishlist: " + productId + " for " + username);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void clearCart(String username) {
        String sql = "DELETE FROM user_cart WHERE username = ?";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.executeUpdate();
            System.out.println("🧹 Cleared cart for: " + username);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

 public static Product mapProduct(ResultSet rs) throws SQLException {
    return new Product(
        rs.getInt("id"),
        rs.getString("name"),
        rs.getDouble("price"),
        rs.getString("category"),
        rs.getDouble("discounted_price"),
        rs.getInt("discount_percent"),
        rs.getString("image_path"),
        rs.getString("about")
    );
}

    public static List<Product> loadWishlist(String username) {
        List<Product> wishlist = new ArrayList<>();
        String sql = "SELECT p.id, p.name, p.price, p.discounted_price, p.discount_percent, " +
                     "p.image_path, p.about, c.catname AS category " +
                     "FROM user_wishlist uw " +
                     "JOIN products p ON uw.product_id = p.id " +
                     "LEFT JOIN categories c ON p.category_id = c.id " +
                     "WHERE uw.username = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                wishlist.add(mapProduct(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return wishlist;
    }
public static List<Product> loadCart(String username) {
    List<Product> cart = new ArrayList<>();
    String sql = "SELECT p.id, p.name, p.price, p.discounted_price, p.discount_percent, " +
                 "p.image_path, p.about, c.catname AS category, uc.quantity " +   // Added uc.quantity here
                 "FROM user_cart uc " +
                 "JOIN products p ON uc.product_id = p.id " +
                 "LEFT JOIN categories c ON p.category_id = c.id " +
                 "WHERE uc.username = ?";

    try (Connection conn = connect();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setString(1, username);
        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {
            Product p = mapProduct(rs);
            p.setQuantity(rs.getInt("quantity"));  // Assuming Product class has quantity field & setter
            cart.add(p);
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return cart;
}


 public static List<Order> loadConfirmedOrders(String username) {
    List<Order> orders = new ArrayList<>();

    try (Connection conn = connect()) {
        int userId = getUserIdByUsername(username);

        // ✅ Include o.status in the SELECT clause
        String sql = "SELECT o.order_id, o.order_date, o.total_amount, o.status, p.name AS product_name, oi.quantity\n" +
"FROM orders o\n" +
"JOIN order_items oi ON o.order_id = oi.order_id\n" +
"JOIN products p ON oi.product_id = p.id\n" +
"WHERE o.user_id = ? AND o.status IN ('Confirmed', 'Dispatched', 'Delivered', 'Completed')";

        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, userId);

        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            Order order = new Order();
            order.setOrderId(rs.getInt("order_id"));
            order.setOrderDate(rs.getTimestamp("order_date").toLocalDateTime());
            order.setTotalAmount(rs.getDouble("total_amount"));
            order.setProductName(rs.getString("product_name"));
            order.setQuantity(rs.getInt("quantity"));

            // ✅ Set the status from ResultSet
            order.setStatus(rs.getString("status"));

            orders.add(order);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return orders;
}





   
  public static List<Order> getOrdersByUser(int userId) {
    List<Order> orders = new ArrayList<>();
    String sql = "SELECT * FROM orders WHERE user_id = ?";

    try (Connection conn = connect();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Order order = new Order();
            order.setOrderId(rs.getInt("order_id"));
            order.setUserId(rs.getInt("user_id"));
            order.setTotalAmount(rs.getDouble("total_amount"));
            order.setAddress(rs.getString("address"));
            order.setContact(rs.getString("contact"));
            order.setCountry(rs.getString("country"));
            order.setCity(rs.getString("city"));
            order.setPaymentMode(rs.getString("payment_mode"));
            order.setReceiptPath(rs.getString("receipt_path"));
         order.setOrderDate(rs.getTimestamp("order_date").toLocalDateTime());


            orders.add(order);
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return orders;
}

public static boolean insertReview(int productId, String username, int rating, String review, String feedback) {
    int userId = getUserIdByUsername(username);
    if (userId == -1) return false;

    String sql = "INSERT INTO reviews (product_id, user_id, rating, review, feedback) VALUES (?, ?, ?, ?, ?)";
    try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, productId);
        stmt.setInt(2, userId);
        stmt.setInt(3, rating);
        stmt.setString(4, review);
        stmt.setString(5, feedback);
        return stmt.executeUpdate() > 0;
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

public static List<String> getReviewsForProduct(int productId) {
    List<String> reviews = new ArrayList<>();
    String sql = "SELECT u.username, r.rating, r.review, r.feedback " +
                 "FROM reviews r JOIN users u ON r.user_id = u.id " +
                 "WHERE r.product_id = ?";
    try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, productId);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            String username = rs.getString("username");
            int rating = rs.getInt("rating");
            String review = rs.getString("review");
            String feedback = rs.getString("feedback");

            String entry = "👤 " + username + " | ⭐ " + rating + "\nReview: " + review + "\nFeedback: " + feedback;
            reviews.add(entry);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return reviews;
}

    public static int getUserIdByUsername(String username) {
    String sql = "SELECT id FROM users WHERE username = ?"; 

    try (Connection conn = connect();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setString(1, username);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return rs.getInt("id");
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return -1;
}
public static int insertOrder(int userId, String fullName, double totalAmount, String address, String contact, String country, String city, File receipt) {
    String sql = "INSERT INTO orders (user_id, full_name, total_amount, address, contact, country, city, payment_mode, receipt_path, status, order_date) " +
                 "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";

    try (Connection conn = connect();
         PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

        ps.setInt(1, userId);
        ps.setString(2, fullName);
        ps.setDouble(3, totalAmount);
        ps.setString(4, address);
        ps.setString(5, contact);
        ps.setString(6, country);
        ps.setString(7, city);
        ps.setString(8, "COD"); // Payment mode hardcoded

        // Handle receipt file (optional)
        String path = null;
        if (receipt != null) {
            File dir = new File("receipts");
            if (!dir.exists()) dir.mkdirs();

            File dest = new File(dir, System.currentTimeMillis() + "_" + receipt.getName());
            Files.copy(receipt.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            path = dest.getAbsolutePath();
        }

        ps.setString(9, path);          // receipt_path
        ps.setString(10, "Confirmed");  // status

        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            return rs.getInt(1); // Return generated order ID
        }

    } catch (Exception e) {
        e.printStackTrace(); // Log the error
    }

    return -1; // Return -1 on failure
}




public static List<Order> getAllOrdersWithUsername() {
    List<Order> orders = new ArrayList<>();
    String query = "SELECT o.order_id, o.user_id, o.total_amount, o.address, o.contact, o.country, o.city, " +
                   "o.payment_mode, o.receipt_path, o.order_date, o.status, " +
                   "o.full_name, " +  // Use full_name instead of username
                   "p.name AS product_name, oi.quantity " +
                   "FROM orders o " +
                   "JOIN order_items oi ON o.order_id = oi.order_id " +
                   "JOIN products p ON oi.product_id = p.id " +
                   "WHERE o.status IN ('Pending', 'Dispatched', 'Delivered', 'Completed');";

    try (Connection conn = connect();
         PreparedStatement stmt = conn.prepareStatement(query);
         ResultSet rs = stmt.executeQuery()) {

        while (rs.next()) {
            Order order = new Order();
            order.setOrderId(rs.getInt("order_id"));
            order.setUserId(rs.getInt("user_id"));
            order.setTotalAmount(rs.getDouble("total_amount"));
            order.setAddress(rs.getString("address"));
            order.setContact(rs.getString("contact"));
            order.setCountry(rs.getString("country"));
            order.setCity(rs.getString("city"));
            order.setPaymentMode(rs.getString("payment_mode"));
            order.setReceiptPath(rs.getString("receipt_path"));
            order.setOrderDate(rs.getTimestamp("order_date").toLocalDateTime());
            order.setStatus(rs.getString("status"));
            order.setFullName(rs.getString("full_name"));  // Set full name from orders table
            order.setProductName(rs.getString("product_name"));
            order.setQuantity(rs.getInt("quantity"));
            orders.add(order);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return orders;
}


public static List<Order> getAllOrders() {
    List<Order> orders = new ArrayList<>();

    try (Connection conn = connect()) {
        String sql = "SELECT o.order_id, o.order_date, o.total_amount, o.status, " +
                     "p.name AS product_name, oi.quantity, " +
                     "o.full_name, o.city, o.address, o.country " +  // replaced username with full_name
                     "FROM orders o " +
                     "JOIN order_items oi ON o.order_id = oi.order_id " +
                     "JOIN products p ON oi.product_id = p.id";

        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            Order order = new Order();
            order.setOrderId(rs.getInt("order_id"));
            order.setOrderDate(rs.getTimestamp("order_date").toLocalDateTime());
            order.setTotalAmount(rs.getDouble("total_amount"));
            order.setStatus(rs.getString("status"));
            order.setProductName(rs.getString("product_name"));
            order.setQuantity(rs.getInt("quantity"));

            // New fields
            order.setFullName(rs.getString("full_name"));  // use full_name
            order.setCity(rs.getString("city"));
            order.setAddress(rs.getString("address"));
            order.setCountry(rs.getString("country"));

            orders.add(order);
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return orders;
}

public static List<String> getAllUsernames() {
    List<String> users = new ArrayList<>();
    String sql = "SELECT username FROM users";
    try (Connection conn = connect();
         PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {

        while (rs.next()) {
            users.add(rs.getString("username"));
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }
    return users;
}

    static void insertOrderItem(int orderId, int productId, int quantity, double price) {
    String sql = "INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
    
    try (Connection conn = DatabaseManager.connect();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setInt(1, orderId);
        pstmt.setInt(2, productId);
        pstmt.setInt(3, quantity);
        pstmt.setDouble(4, price);

        pstmt.executeUpdate();

    } catch (SQLException e) {
        e.printStackTrace();
    }
}

public static boolean updateOrderStatus(int orderId, String newStatus) {
    String query = "UPDATE orders SET status = ? WHERE order_id = ?";
    try (Connection conn = connect();
         PreparedStatement stmt = conn.prepareStatement(query)) {

        stmt.setString(1, newStatus);
        stmt.setInt(2, orderId);

        int rows = stmt.executeUpdate();
        return rows > 0;

    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}





public static void updateCartQuantity(String username, int productId, int quantity) {
    String query = "UPDATE user_cart SET quantity = ? WHERE username = ? AND product_id = ?";
    try (Connection conn = connect();
         PreparedStatement stmt = conn.prepareStatement(query)) {
        stmt.setInt(1, quantity);
        stmt.setString(2, username);
        stmt.setInt(3, productId);
        int rows = stmt.executeUpdate();
        System.out.println("🛠️ Rows updated: " + rows);
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
public static int getCartQuantity(String username, int productId) {
    String sql = "SELECT quantity FROM user_cart WHERE username = ? AND product_id = ?";
    try (Connection conn = connect();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setString(1, username);
        stmt.setInt(2, productId);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return rs.getInt("quantity");
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return 1; // default quantity if not found
}




 public static List<Order> loadDeliveredOrders() {
    List<Order> orders = new ArrayList<>();
    String query = "SELECT o.order_id, o.order_date, o.total_amount, o.address, o.contact, o.country, o.city, " +
                   "o.full_name, oi.product_id, oi.quantity, oi.status, p.name AS product_name " +
                   "FROM orders o " +
                   "JOIN order_items oi ON o.order_id = oi.order_id " +
                   "JOIN products p ON oi.product_id = p.id " +
                   "WHERE oi.status = 'Delivered'";

    try (Connection conn = connect();
         PreparedStatement stmt = conn.prepareStatement(query);
         ResultSet rs = stmt.executeQuery()) {

        while (rs.next()) {
            Order order = new Order();
            order.setOrderId(rs.getInt("order_id"));
            order.setOrderDate(rs.getTimestamp("order_date").toLocalDateTime());
            order.setTotalAmount(rs.getDouble("total_amount"));
            order.setAddress(rs.getString("address"));
            order.setContact(rs.getString("contact"));
            order.setCountry(rs.getString("country"));
            order.setCity(rs.getString("city"));
            order.setFullName(rs.getString("full_name"));  // Use full_name instead of username
            order.setProductName(rs.getString("product_name"));
            order.setQuantity(rs.getInt("quantity"));
            order.setStatus(rs.getString("status"));
            orders.add(order);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return orders;
}

public static boolean saveReceiptPath(int orderId, String receiptPath) {
    String sql = "UPDATE orders SET receipt_path = ? WHERE order_id = ?";
    try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, receiptPath);
        stmt.setInt(2, orderId);
        return stmt.executeUpdate() > 0;
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}


   public static boolean updateProductReview(int productId, int rating, String review, String feedback) {
    try (Connection conn = connect();
         PreparedStatement stmt = conn.prepareStatement(
             "UPDATE products SET rating = ?, reviews = ?, feedback = ? WHERE id = ?")) {
        stmt.setInt(1, rating);
        stmt.setString(2, review);
        stmt.setString(3, feedback);
        stmt.setInt(4, productId);
        return stmt.executeUpdate() > 0;
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

public static Product getProductById(int productId) {
    try (Connection conn = connect();
         PreparedStatement stmt = conn.prepareStatement("SELECT * FROM products WHERE id = ?")) {
        stmt.setInt(1, productId);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return new Product(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getDouble("price"),
                rs.getDouble("discounted_Price"),
                rs.getInt("discount_Percent"),
                rs.getDouble("rating"),
                rs.getString("reviews"),
                rs.getString("about"),
                rs.getString("feedback"),
                rs.getString("image_Path")
            );
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null;
}


}

