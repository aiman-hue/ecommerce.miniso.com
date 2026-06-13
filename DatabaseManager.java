/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ecommerce;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseManager {

    public static Connection connect() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/e-commerceapp";
        String user = "root";
        String password = "";
        return DriverManager.getConnection(url, user, password);
    }

    public static List<Product> getProductsSortedByPrice(boolean descending) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, c.catname AS category FROM products p " +
                     "LEFT JOIN categories c ON p.category_id = c.id " +
                     "ORDER BY p.price " + (descending ? "DESC" : "ASC");
        
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                products.add(mapProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }
        // =====================================================
    // Invoice Methods
    // =====================================================
    public static void saveInvoicePath(int orderId, String invoicePath) {
        String sql = "UPDATE orders SET invoice_path = ? WHERE order_id = ?";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, invoicePath);
            stmt.setInt(2, orderId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static List<Map<String, Object>> getUserInvoices(String username) {
        List<Map<String, Object>> invoices = new ArrayList<>();
        int userId = getUserIdByUsername(username);
        
        String sql = "SELECT order_id, order_date, total_amount, status, invoice_path FROM orders WHERE user_id = ? ORDER BY order_date DESC";
        
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> invoice = new HashMap<>();
                invoice.put("order_id", rs.getInt("order_id"));
                invoice.put("order_date", rs.getTimestamp("order_date").toLocalDateTime());
                invoice.put("total_amount", rs.getDouble("total_amount"));
                invoice.put("status", rs.getString("status"));
                invoice.put("invoice_path", rs.getString("invoice_path"));
                invoices.add(invoice);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return invoices;
    }
    
    public static boolean updateUserProfile(String username, String email, String password) {
        String sql = "UPDATE users SET email = ?, password = ? WHERE username = ?";
        try (Connection conn = connect();
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
    // =====================================================
    // TASK 3: INNER JOIN - User orders with product details
    // =====================================================
    public static List<Map<String, Object>> getUserOrderDetails() {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = "SELECT u.username, p.name AS product_name, oi.quantity, " +
                     "o.total_amount, o.status FROM orders o " +
                     "INNER JOIN users u ON o.user_id = u.id " +
                     "INNER JOIN order_items oi ON o.order_id = oi.order_id " +
                     "INNER JOIN products p ON oi.product_id = p.id";
        
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("username", rs.getString("username"));
                row.put("product_name", rs.getString("product_name"));
                row.put("quantity", rs.getInt("quantity"));
                row.put("total_amount", rs.getDouble("total_amount"));
                row.put("status", rs.getString("status"));
                results.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    // =====================================================
    // TASK 4: LEFT JOIN - Categories with product counts
    // =====================================================
    public static List<Map<String, Object>> getCategoryProductCounts() {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = "SELECT c.catname AS category, COUNT(p.id) AS product_count " +
                     "FROM categories c LEFT JOIN products p ON c.id = p.category_id " +
                     "GROUP BY c.id";
        
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("category", rs.getString("category"));
                row.put("product_count", rs.getInt("product_count"));
                results.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    // =====================================================
    // TASK 5: Subquery with IN - Products in most popular category
    // =====================================================
    public static List<Product> getProductsInMostPopularCategory() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, c.catname AS category FROM products p " +
                     "JOIN categories c ON p.category_id = c.id " +
                     "WHERE p.category_id = (SELECT category_id FROM products " +
                     "WHERE category_id IS NOT NULL GROUP BY category_id ORDER BY COUNT(*) DESC LIMIT 1)";
        
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                products.add(mapProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    // =====================================================
    // TASK 7: GROUP BY with HAVING - Categories with min products
    // =====================================================
    public static List<Map<String, Object>> getCategoriesWithMinProducts(int minCount) {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = "SELECT c.catname, COUNT(p.id) AS product_count " +
                     "FROM categories c JOIN products p ON c.id = p.category_id " +
                     "GROUP BY c.id HAVING COUNT(p.id) > ?";
        
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, minCount);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("category", rs.getString("catname"));
                row.put("product_count", rs.getInt("product_count"));
                results.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    // =====================================================
    // TASK 8: Aggregate Functions - Price statistics by category
    // =====================================================
    public static List<Map<String, Object>> getPriceStatisticsByCategory() {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = "SELECT c.catname AS category, MIN(p.price) AS min_price, " +
                     "MAX(p.price) AS max_price, ROUND(AVG(p.price), 2) AS avg_price " +
                     "FROM categories c JOIN products p ON c.id = p.category_id " +
                     "GROUP BY c.id";
        
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("category", rs.getString("category"));
                row.put("min_price", rs.getDouble("min_price"));
                row.put("max_price", rs.getDouble("max_price"));
                row.put("avg_price", rs.getDouble("avg_price"));
                results.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    // =====================================================
    // TASKS 9-10: Views - High value customers
    // =====================================================
    public static List<Map<String, Object>> getHighValueCustomers() {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = "SELECT * FROM HighValueCustomers";
        
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("username", rs.getString("username"));
                row.put("email", rs.getString("email"));
                row.put("total_orders", rs.getInt("total_orders"));
                row.put("total_spent", rs.getDouble("total_spent"));
                results.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    // =====================================================
    // TASK 11: Stored Procedure - Get user orders
    // =====================================================
    public static List<Map<String, Object>> getUserOrdersStoredProc(int userId) {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = "{CALL GetUserOrders(?)}";
        
        try (Connection conn = connect();
             CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("order_id", rs.getInt("order_id"));
                row.put("order_date", rs.getTimestamp("order_date"));
                row.put("total_amount", rs.getDouble("total_amount"));
                row.put("status", rs.getString("status"));
                row.put("item_count", rs.getInt("item_count"));
                results.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    // =====================================================
    // TASK 12: Stored Procedure - Add to cart
    // =====================================================
    public static boolean addToCartStoredProc(String username, int productId, int quantity) {
        String sql = "{CALL EnrollStudent(?, ?, ?)}";
        
        try (Connection conn = connect();
             CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setString(1, username);
            stmt.setInt(2, productId);
            stmt.setInt(3, quantity);
            stmt.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =====================================================
    // TASK 13: Stored Procedure - Get monthly sales
    // =====================================================
    public static double getMonthlySales(int month, int year) {
        String sql = "{CALL GetSemesterFees(?, ?, ?)}";
        
        try (Connection conn = connect();
             CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setInt(1, month);
            stmt.setInt(2, year);
            stmt.registerOutParameter(3, Types.DECIMAL);
            stmt.execute();
            return stmt.getDouble(3);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    // =====================================================
    // TASK 14 & 16: Scalar Function - Calculate age
    // =====================================================
    public static Integer calculateAge(Date dob) {
        String sql = "SELECT CalculateAge(?)";
        
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, dob);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // =====================================================
    // TASK 15: Stored Procedure for products by category
    // =====================================================
    public static List<Map<String, Object>> getProductsByCategoryProcedure(String categoryName) {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = "{CALL GetStudentsByMajor(?)}";
        
        try (Connection conn = connect();
             CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setString(1, categoryName);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("product_id", rs.getInt("product_id"));
                row.put("product_name", rs.getString("product_name"));
                row.put("product_price", rs.getDouble("product_price"));
                row.put("discount_percent", rs.getDouble("discount_percent"));
                results.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    // =====================================================
    // TASK 19: Transaction - Transfer cart items
    // =====================================================
    public static String transferCartItems(String fromUser, String toUser, int productId) {
        String sql = "{CALL TransferEnrollment(?, ?, ?, ?)}";
        
        try (Connection conn = connect();
             CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setString(1, fromUser);
            stmt.setString(2, toUser);
            stmt.setInt(3, productId);
            stmt.registerOutParameter(4, Types.VARCHAR);
            stmt.execute();
            return stmt.getString(4);
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    // =====================================================
    // TASK 20: Window Function with CTE - Top spenders
    // =====================================================
   // =====================================================
// TASK 20: Window Function with CTE - Top spenders (FIXED)
// =====================================================
public static List<Map<String, Object>> getTopSpendersByMonth(int topN) {
    List<Map<String, Object>> results = new ArrayList<>();
    String sql = "WITH MonthlySpending AS ( " +
                 "SELECT " +
                 "    u.username, " +
                 "    DATE_FORMAT(o.order_date, '%Y-%m') AS month, " +
                 "    SUM(o.total_amount) AS monthly_spending " +
                 "FROM users u " +
                 "INNER JOIN orders o ON u.id = o.user_id " +
                 "WHERE o.order_date IS NOT NULL " +
                 "GROUP BY u.id, DATE_FORMAT(o.order_date, '%Y-%m') " +
                 "), " +
                 "RankedSpending AS ( " +
                 "SELECT " +
                 "    username, " +
                 "    month, " +
                 "    monthly_spending, " +
                 "    RANK() OVER (PARTITION BY month ORDER BY monthly_spending DESC) AS spending_rank " +
                 "FROM MonthlySpending " +
                 ") " +
                 "SELECT * FROM RankedSpending WHERE spending_rank <= ? ORDER BY month DESC, spending_rank ASC";
    
    try (Connection conn = connect();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, topN);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            Map<String, Object> row = new HashMap<>();
            row.put("username", rs.getString("username"));
            row.put("month", rs.getString("month"));
            row.put("monthly_spending", rs.getDouble("monthly_spending"));
            row.put("spending_rank", rs.getInt("spending_rank"));
            results.add(row);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return results;
}

    // =====================================================
    // Category Management Methods
    // =====================================================
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

    // =====================================================
    // Cart & Wishlist Methods
    // =====================================================
    public static void addToCart(String username, int productId) {
        String sql = "INSERT INTO user_cart (username, product_id, quantity) VALUES (?, ?, 1)";

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
                     "p.image_path, p.about, c.catname AS category, uc.quantity " +
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
                p.setQuantity(rs.getInt("quantity"));
                cart.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return cart;
    }

    public static void updateCartQuantity(String username, int productId, int quantity) {
        String query = "UPDATE user_cart SET quantity = ? WHERE username = ? AND product_id = ?";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, quantity);
            stmt.setString(2, username);
            stmt.setInt(3, productId);
            stmt.executeUpdate();
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
        return 1;
    }

    // =====================================================
    // Order Methods
    // =====================================================
    public static List<Order> loadConfirmedOrders(String username) {
        List<Order> orders = new ArrayList<>();

        try (Connection conn = connect()) {
            int userId = getUserIdByUsername(username);

            String sql = "SELECT o.order_id, o.order_date, o.total_amount, o.status, p.name AS product_name, oi.quantity " +
                         "FROM orders o " +
                         "JOIN order_items oi ON o.order_id = oi.order_id " +
                         "JOIN products p ON oi.product_id = p.id " +
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

    public static List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();

        try (Connection conn = connect()) {
            String sql = "SELECT o.order_id, o.order_date, o.total_amount, o.status, " +
                         "p.name AS product_name, oi.quantity, " +
                         "o.full_name, o.city, o.address, o.country " +
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
                order.setFullName(rs.getString("full_name"));
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
            ps.setString(8, "COD");

            String path = null;
            if (receipt != null) {
                File dir = new File("receipts");
                if (!dir.exists()) dir.mkdirs();

                File dest = new File(dir, System.currentTimeMillis() + "_" + receipt.getName());
                Files.copy(receipt.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                path = dest.getAbsolutePath();
            }

            ps.setString(9, path);
            ps.setString(10, "Confirmed");

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    public static void insertOrderItem(int orderId, int productId, int quantity, double price) {
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

    // =====================================================
    // User Methods
    // =====================================================
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

    // =====================================================
    // Review Methods
    // =====================================================
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
                    rs.getDouble("discounted_price"),
                    rs.getInt("discount_percent"),
                    rs.getDouble("rating"),
                    rs.getString("reviews"),
                    rs.getString("about"),
                    rs.getString("feedback"),
                    rs.getString("image_path")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
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
                order.setFullName(rs.getString("full_name"));
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

    public static List<Order> getAllOrdersWithUsername() {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT o.order_id, o.user_id, o.total_amount, o.address, o.contact, o.country, o.city, " +
                       "o.payment_mode, o.receipt_path, o.order_date, o.status, " +
                       "o.full_name, " +
                       "p.name AS product_name, oi.quantity " +
                       "FROM orders o " +
                       "JOIN order_items oi ON o.order_id = oi.order_id " +
                       "JOIN products p ON oi.product_id = p.id " +
                       "WHERE o.status IN ('Pending', 'Dispatched', 'Delivered', 'Completed')";

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
                order.setFullName(rs.getString("full_name"));
                order.setProductName(rs.getString("product_name"));
                order.setQuantity(rs.getInt("quantity"));
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return orders;
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

    // =====================================================
    // Helper method to map ResultSet to Product
    // =====================================================
    private static Product mapProduct(ResultSet rs) throws SQLException {
        return new Product(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getDouble("price"),
            rs.getString("category") != null ? rs.getString("category") : "",
            rs.getDouble("discounted_price"),
            rs.getInt("discount_percent"),
            rs.getString("image_path"),
            rs.getString("about")
        );
    }

} // <-- ONLY ONE closing brace at the end