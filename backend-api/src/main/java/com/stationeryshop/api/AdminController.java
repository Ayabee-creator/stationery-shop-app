package com.stationeryshop.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private DataSource dataSource;

    // GET /admin/dashboard — overview stats
    @GetMapping("/dashboard")
    public Map<String, Object> getDashboardStats() throws Exception {
        Map<String, Object> stats = new LinkedHashMap<>();

        try (Connection conn = dataSource.getConnection()) {

            // Total products
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM products")) {
                if (rs.next()) stats.put("total_products", rs.getInt("total"));
            }

            // Total orders
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM orders")) {
                if (rs.next()) stats.put("total_orders", rs.getInt("total"));
            }

            // Total customers
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM users")) {
                if (rs.next()) stats.put("total_customers", rs.getInt("total"));
            }

            // Total revenue
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COALESCE(SUM(total_amount), 0) AS revenue FROM orders")) {
                if (rs.next()) stats.put("total_revenue", rs.getDouble("revenue"));
            }

            // Pending orders
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM orders WHERE status = 'pending'")) {
                if (rs.next()) stats.put("pending_orders", rs.getInt("total"));
            }

            // Low stock products (below 30)
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM products WHERE stock_quantity < 30")) {
                if (rs.next()) stats.put("low_stock_count", rs.getInt("total"));
            }
        }

        return stats;
    }

    // GET /admin/orders — all orders with customer info
    @GetMapping("/orders")
    public List<Map<String, Object>> getAllOrders() throws Exception {
        List<Map<String, Object>> orders = new ArrayList<>();

        String sql = """
            SELECT o.id, o.user_id, u.username, u.email, o.total_amount,
                   o.status, o.ordered_at, COUNT(oi.id) AS item_count
            FROM orders o
            JOIN users u ON o.user_id = u.id
            LEFT JOIN order_items oi ON o.id = oi.order_id
            GROUP BY o.id, o.user_id, u.username, u.email, o.total_amount, o.status, o.ordered_at
            ORDER BY o.ordered_at DESC
        """;

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> order = new LinkedHashMap<>();
                order.put("id", rs.getInt("id"));
                order.put("user_id", rs.getInt("user_id"));
                order.put("username", rs.getString("username"));
                order.put("email", rs.getString("email"));
                order.put("total_amount", rs.getDouble("total_amount"));
                order.put("status", rs.getString("status"));
                order.put("ordered_at", String.valueOf(rs.getTimestamp("ordered_at")));
                order.put("item_count", rs.getInt("item_count"));
                orders.add(order);
            }
        }
        return orders;
    }

    // PUT /admin/orders/{id}/status — update order status
    @PutMapping("/orders/{id}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @PathVariable int id, @RequestBody Map<String, String> body) throws Exception {

        Map<String, Object> response = new LinkedHashMap<>();
        String newStatus = body.get("status");

        if (newStatus == null || newStatus.isEmpty()) {
            response.put("success", false);
            response.put("message", "Status is required");
            return ResponseEntity.badRequest().body(response);
        }

        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, id);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                response.put("success", true);
                response.put("message", "Order status updated to " + newStatus);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Order not found");
                return ResponseEntity.status(404).body(response);
            }
        }
    }

    // GET /admin/customers — all users
    @GetMapping("/customers")
    public List<Map<String, Object>> getAllCustomers() throws Exception {
        List<Map<String, Object>> customers = new ArrayList<>();

        String sql = """
            SELECT u.id, u.username, u.email, u.phone, u.address, u.created_at,
                   COUNT(DISTINCT o.id) AS order_count,
                   COALESCE(SUM(o.total_amount), 0) AS total_spent
            FROM users u
            LEFT JOIN orders o ON u.id = o.user_id
            GROUP BY u.id, u.username, u.email, u.phone, u.address, u.created_at
            ORDER BY total_spent DESC
        """;

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> customer = new LinkedHashMap<>();
                customer.put("id", rs.getInt("id"));
                customer.put("username", rs.getString("username"));
                customer.put("email", rs.getString("email"));
                customer.put("phone", rs.getString("phone"));
                customer.put("address", rs.getString("address"));
                customer.put("created_at", String.valueOf(rs.getTimestamp("created_at")));
                customer.put("order_count", rs.getInt("order_count"));
                customer.put("total_spent", rs.getDouble("total_spent"));
                customers.add(customer);
            }
        }
        return customers;
    }

    // GET /admin/inventory — products with low stock
    @GetMapping("/inventory")
    public List<Map<String, Object>> getLowStockProducts() throws Exception {
        List<Map<String, Object>> products = new ArrayList<>();

        String sql = """
            SELECT p.id, p.name, c.name AS category, p.price, p.stock_quantity
            FROM products p
            JOIN categories c ON p.category_id = c.id
            WHERE p.stock_quantity < 50
            ORDER BY p.stock_quantity ASC
        """;

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> p = new LinkedHashMap<>();
                p.put("id", rs.getInt("id"));
                p.put("name", rs.getString("name"));
                p.put("category", rs.getString("category"));
                p.put("price", rs.getDouble("price"));
                p.put("stock_quantity", rs.getInt("stock_quantity"));
                products.add(p);
            }
        }
        return products;
    }

    // POST /admin/products — add new product
    @PostMapping("/products")
    public ResponseEntity<Map<String, Object>> addProduct(
            @RequestBody Map<String, Object> body) throws Exception {

        Map<String, Object> response = new LinkedHashMap<>();

        String name = (String) body.get("name");
        String description = (String) body.get("description");
        int categoryId = getInt(body.get("category_id"));
        double price = getDouble(body.get("price"));
        int stockQuantity = getInt(body.get("stock_quantity"));
        String imageUrl = (String) body.get("image_url");

        if (name == null || name.isEmpty() || categoryId <= 0 || price <= 0) {
            response.put("success", false);
            response.put("message", "name, category_id, and price are required");
            return ResponseEntity.badRequest().body(response);
        }

        String sql = """
            INSERT INTO products (category_id, name, description, price, stock_quantity, image_url)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, categoryId);
            stmt.setString(2, name);
            stmt.setString(3, description);
            stmt.setDouble(4, price);
            stmt.setInt(5, stockQuantity);
            stmt.setString(6, imageUrl);
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                response.put("success", true);
                response.put("message", "Product added");
                response.put("product_id", keys.getInt(1));
                return ResponseEntity.status(201).body(response);
            }
        }

        response.put("success", false);
        response.put("message", "Failed to add product");
        return ResponseEntity.status(500).body(response);
    }

    // PUT /admin/products/{id} — update product
    @PutMapping("/products/{id}")
    public ResponseEntity<Map<String, Object>> updateProduct(
            @PathVariable int id, @RequestBody Map<String, Object> body) throws Exception {

        Map<String, Object> response = new LinkedHashMap<>();

        String name = (String) body.get("name");
        String description = (String) body.get("description");
        int categoryId = getInt(body.get("category_id"));
        double price = getDouble(body.get("price"));
        int stockQuantity = getInt(body.get("stock_quantity"));
        String imageUrl = (String) body.get("image_url");

        String sql = """
            UPDATE products SET name = ?, description = ?, category_id = ?,
                   price = ?, stock_quantity = ?, image_url = ?
            WHERE id = ?
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.setInt(3, categoryId);
            stmt.setDouble(4, price);
            stmt.setInt(5, stockQuantity);
            stmt.setString(6, imageUrl);
            stmt.setInt(7, id);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                response.put("success", true);
                response.put("message", "Product updated");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Product not found");
                return ResponseEntity.status(404).body(response);
            }
        }
    }

    // DELETE /admin/products/{id} — delete product
    @DeleteMapping("/products/{id}")
    public ResponseEntity<Map<String, Object>> deleteProduct(
            @PathVariable int id) throws Exception {

        Map<String, Object> response = new LinkedHashMap<>();

        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                response.put("success", true);
                response.put("message", "Product deleted");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Product not found");
                return ResponseEntity.status(404).body(response);
            }
        }
    }

    // PUT /admin/products/{id}/stock — quick stock update
    @PutMapping("/products/{id}/stock")
    public ResponseEntity<Map<String, Object>> updateStock(
            @PathVariable int id, @RequestBody Map<String, Object> body) throws Exception {

        Map<String, Object> response = new LinkedHashMap<>();
        int stockQuantity = getInt(body.get("stock_quantity"));

        String sql = "UPDATE products SET stock_quantity = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, stockQuantity);
            stmt.setInt(2, id);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                response.put("success", true);
                response.put("message", "Stock updated");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Product not found");
                return ResponseEntity.status(404).body(response);
            }
        }
    }

    // GET /admin/revenue-chart — revenue per day for last 30 days
    @GetMapping("/revenue-chart")
    public List<Map<String, Object>> getRevenueChart() throws Exception {
        List<Map<String, Object>> data = new ArrayList<>();

        String sql = """
            SELECT DATE(ordered_at) AS order_date,
                   SUM(total_amount) AS daily_revenue,
                   COUNT(*) AS order_count
            FROM orders
            WHERE ordered_at >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
            GROUP BY DATE(ordered_at)
            ORDER BY order_date ASC
        """;

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> point = new LinkedHashMap<>();
                point.put("date", rs.getString("order_date"));
                point.put("revenue", rs.getDouble("daily_revenue"));
                point.put("orders", rs.getInt("order_count"));
                data.add(point);
            }
        }
        return data;
    }

    // GET /admin/top-products — top selling products by quantity
    @GetMapping("/top-products")
    public List<Map<String, Object>> getTopProducts() throws Exception {
        List<Map<String, Object>> products = new ArrayList<>();

        String sql = """
            SELECT p.id, p.name, p.price, p.image_url, c.name AS category,
                   SUM(oi.quantity) AS total_sold,
                   SUM(oi.quantity * oi.unit_price) AS total_revenue
            FROM order_items oi
            JOIN products p ON oi.product_id = p.id
            JOIN categories c ON p.category_id = c.id
            GROUP BY p.id, p.name, p.price, p.image_url, c.name
            ORDER BY total_sold DESC
            LIMIT 10
        """;

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> p = new LinkedHashMap<>();
                p.put("id", rs.getInt("id"));
                p.put("name", rs.getString("name"));
                p.put("price", rs.getDouble("price"));
                p.put("image_url", rs.getString("image_url"));
                p.put("category", rs.getString("category"));
                p.put("total_sold", rs.getInt("total_sold"));
                p.put("total_revenue", rs.getDouble("total_revenue"));
                products.add(p);
            }
        }
        return products;
    }

    // GET /admin/activity — recent activity feed
    @GetMapping("/activity")
    public List<Map<String, Object>> getRecentActivity() throws Exception {
        List<Map<String, Object>> activities = new ArrayList<>();

        // Recent orders
        String orderSql = """
            SELECT 'order' AS type, o.id AS ref_id, u.username,
                   o.total_amount AS amount, o.ordered_at AS created_at
            FROM orders o JOIN users u ON o.user_id = u.id
            ORDER BY o.ordered_at DESC LIMIT 5
        """;

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(orderSql)) {
            while (rs.next()) {
                Map<String, Object> a = new LinkedHashMap<>();
                a.put("type", "order");
                a.put("message", rs.getString("username") + " placed an order for R" + String.format("%.2f", rs.getDouble("amount")));
                a.put("ref_id", rs.getInt("ref_id"));
                a.put("created_at", String.valueOf(rs.getTimestamp("created_at")));
                activities.add(a);
            }
        }

        // Recent reviews
        String reviewSql = """
            SELECT 'review' AS type, r.id AS ref_id, u.username,
                   p.name AS product_name, r.rating, r.created_at
            FROM reviews r
            JOIN users u ON r.user_id = u.id
            JOIN products p ON r.product_id = p.id
            ORDER BY r.created_at DESC LIMIT 5
        """;

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(reviewSql)) {
            while (rs.next()) {
                Map<String, Object> a = new LinkedHashMap<>();
                a.put("type", "review");
                a.put("message", rs.getString("username") + " left a " + rs.getInt("rating") + "-star review on " + rs.getString("product_name"));
                a.put("ref_id", rs.getInt("ref_id"));
                a.put("created_at", String.valueOf(rs.getTimestamp("created_at")));
                activities.add(a);
            }
        }

        // Recent registrations
        String userSql = """
            SELECT 'registration' AS type, id AS ref_id, username, created_at
            FROM users
            ORDER BY created_at DESC LIMIT 5
        """;

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(userSql)) {
            while (rs.next()) {
                Map<String, Object> a = new LinkedHashMap<>();
                a.put("type", "registration");
                a.put("message", rs.getString("username") + " registered a new account");
                a.put("ref_id", rs.getInt("ref_id"));
                a.put("created_at", String.valueOf(rs.getTimestamp("created_at")));
                activities.add(a);
            }
        }

        // Sort all by date descending
        activities.sort((a, b) -> {
            String dateA = (String) a.get("created_at");
            String dateB = (String) b.get("created_at");
            return dateB.compareTo(dateA);
        });

        return activities.subList(0, Math.min(activities.size(), 10));
    }

    // PUT /admin/orders/bulk-status — bulk update order statuses
    @PutMapping("/orders/bulk-status")
    public ResponseEntity<Map<String, Object>> bulkUpdateOrderStatus(
            @RequestBody Map<String, Object> body) throws Exception {

        Map<String, Object> response = new LinkedHashMap<>();

        String newStatus = (String) body.get("status");
        Object idsObj = body.get("order_ids");

        if (newStatus == null || idsObj == null) {
            response.put("success", false);
            response.put("message", "status and order_ids are required");
            return ResponseEntity.badRequest().body(response);
        }

        List<?> ids = (List<?>) idsObj;

        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        int updated = 0;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Object idObj : ids) {
                stmt.setString(1, newStatus);
                stmt.setInt(2, getInt(idObj));
                stmt.addBatch();
                updated++;
            }
            stmt.executeBatch();
        }

        response.put("success", true);
        response.put("message", updated + " orders updated to " + newStatus);
        return ResponseEntity.ok(response);
    }

    // GET /admin/revenue-month — this month's revenue for goal tracker
    @GetMapping("/revenue-month")
    public Map<String, Object> getMonthlyRevenue() throws Exception {
        Map<String, Object> data = new LinkedHashMap<>();

        String sql = """
            SELECT COALESCE(SUM(total_amount), 0) AS month_revenue,
                   COUNT(*) AS month_orders
            FROM orders
            WHERE MONTH(ordered_at) = MONTH(CURDATE())
              AND YEAR(ordered_at) = YEAR(CURDATE())
        """;

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                data.put("month_revenue", rs.getDouble("month_revenue"));
                data.put("month_orders", rs.getInt("month_orders"));
            }
        }
        return data;
    }

    private int getInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        if (value instanceof String) return Integer.parseInt((String) value);
        return 0;
    }

    private double getDouble(Object value) {
        if (value == null) return 0;
        if (value instanceof Double) return (Double) value;
        if (value instanceof Number) return ((Number) value).doubleValue();
        if (value instanceof String) return Double.parseDouble((String) value);
        return 0;
    }
}
