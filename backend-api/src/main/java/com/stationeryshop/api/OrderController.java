package com.stationeryshop.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private DataSource dataSource;

    // POST /orders — place a new order and update stock_quantity
    @PostMapping
    public ResponseEntity<Map<String, Object>> placeOrder(
            @RequestBody Map<String, Object> body) {

        Map<String, Object> response = new LinkedHashMap<>();

        int userId = getInt(body.get("user_id"));
        Object itemsObject = body.get("items");

        if (userId <= 0) {
            response.put("success", false);
            response.put("message", "user_id is required");
            return ResponseEntity.badRequest().body(response);
        }

        if (!(itemsObject instanceof List<?> itemsList) || itemsList.isEmpty()) {
            response.put("success", false);
            response.put("message", "Order must contain at least one item");
            return ResponseEntity.badRequest().body(response);
        }

        Connection conn = null;

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            // Check if user exists
            String userSql = "SELECT id FROM users WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(userSql)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();

                if (!rs.next()) {
                    conn.rollback();
                    response.put("success", false);
                    response.put("message", "User not found");
                    return ResponseEntity.status(404).body(response);
                }
            }

            BigDecimal totalAmount = BigDecimal.ZERO;
            List<OrderItemData> orderItems = new ArrayList<>();

            String productSql = """
                SELECT id, name, price, stock_quantity
                FROM products
                WHERE id = ?
                FOR UPDATE
            """;

            for (Object itemObject : itemsList) {
                if (!(itemObject instanceof Map<?, ?> itemMap)) {
                    conn.rollback();
                    response.put("success", false);
                    response.put("message", "Invalid item format");
                    return ResponseEntity.badRequest().body(response);
                }

                int productId = getInt(itemMap.get("product_id"));
                int quantity = getInt(itemMap.get("quantity"));

                if (productId <= 0 || quantity <= 0) {
                    conn.rollback();
                    response.put("success", false);
                    response.put("message", "Each item needs product_id and quantity");
                    return ResponseEntity.badRequest().body(response);
                }

                try (PreparedStatement stmt = conn.prepareStatement(productSql)) {
                    stmt.setInt(1, productId);
                    ResultSet rs = stmt.executeQuery();

                    if (!rs.next()) {
                        conn.rollback();
                        response.put("success", false);
                        response.put("message", "Product not found: " + productId);
                        return ResponseEntity.status(404).body(response);
                    }

                    String productName = rs.getString("name");
                    BigDecimal price = rs.getBigDecimal("price");
                    int stockQuantity = rs.getInt("stock_quantity");

                    if (stockQuantity < quantity) {
                        conn.rollback();
                        response.put("success", false);
                        response.put("message", "Not enough stock for " + productName);
                        return ResponseEntity.badRequest().body(response);
                    }

                    BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(quantity));
                    totalAmount = totalAmount.add(lineTotal);

                    OrderItemData item = new OrderItemData();
                    item.productId = productId;
                    item.productName = productName;
                    item.quantity = quantity;
                    item.unitPrice = price;
                    item.lineTotal = lineTotal;

                    orderItems.add(item);
                }
            }

            // Insert order
            String orderSql = """
                INSERT INTO orders (user_id, total_amount, status)
                VALUES (?, ?, ?)
            """;

            int orderId;

            try (PreparedStatement stmt = conn.prepareStatement(
                    orderSql, Statement.RETURN_GENERATED_KEYS)) {

                stmt.setInt(1, userId);
                stmt.setBigDecimal(2, totalAmount);
                stmt.setString(3, "pending");
                stmt.executeUpdate();

                ResultSet keys = stmt.getGeneratedKeys();

                if (keys.next()) {
                    orderId = keys.getInt(1);
                } else {
                    conn.rollback();
                    response.put("success", false);
                    response.put("message", "Failed to create order");
                    return ResponseEntity.status(500).body(response);
                }
            }

            // Insert order items
            String itemSql = """
                INSERT INTO order_items (order_id, product_id, quantity, unit_price)
                VALUES (?, ?, ?, ?)
            """;

            try (PreparedStatement stmt = conn.prepareStatement(itemSql)) {
                for (OrderItemData item : orderItems) {
                    stmt.setInt(1, orderId);
                    stmt.setInt(2, item.productId);
                    stmt.setInt(3, item.quantity);
                    stmt.setBigDecimal(4, item.unitPrice);
                    stmt.addBatch();
                }

                stmt.executeBatch();
            }

            // Update stock
            String stockSql = """
                UPDATE products
                SET stock_quantity = stock_quantity - ?
                WHERE id = ?
            """;

            try (PreparedStatement stmt = conn.prepareStatement(stockSql)) {
                for (OrderItemData item : orderItems) {
                    stmt.setInt(1, item.quantity);
                    stmt.setInt(2, item.productId);
                    stmt.addBatch();
                }

                stmt.executeBatch();
            }

            conn.commit();

            response.put("success", true);
            response.put("message", "Order placed successfully");
            response.put("order_id", orderId);
            response.put("user_id", userId);
            response.put("total_amount", totalAmount);
            response.put("status", "pending");
            response.put("items", orderItems.size());

            return ResponseEntity.status(201).body(response);

        } catch (Exception e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (Exception rollbackError) {
                rollbackError.printStackTrace();
            }

            response.put("success", false);
            response.put("message", "Order failed: " + e.getMessage());
            return ResponseEntity.status(500).body(response);

        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception closeError) {
                closeError.printStackTrace();
            }
        }
    }

    // GET /orders/user/{userId} — get all orders for a customer
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getOrdersByUser(
            @PathVariable int userId) throws Exception {

        List<Map<String, Object>> orders = new ArrayList<>();

        String sql = """
            SELECT o.id, o.user_id, o.total_amount, o.status, o.ordered_at,
                   COUNT(oi.id) AS item_count
            FROM orders o
            LEFT JOIN order_items oi ON o.id = oi.order_id
            WHERE o.user_id = ?
            GROUP BY o.id, o.user_id, o.total_amount, o.status, o.ordered_at
            ORDER BY o.ordered_at DESC
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> order = new LinkedHashMap<>();
                order.put("id", rs.getInt("id"));
                order.put("user_id", rs.getInt("user_id"));
                order.put("total_amount", rs.getBigDecimal("total_amount"));
                order.put("status", rs.getString("status"));
                order.put("ordered_at", String.valueOf(rs.getTimestamp("ordered_at")));
                order.put("item_count", rs.getInt("item_count"));

                orders.add(order);
            }
        }

        return ResponseEntity.ok(orders);
    }

    // GET /orders/{orderId} — get full order details with all items
    @GetMapping("/{orderId}")
    public ResponseEntity<Map<String, Object>> getOrderDetails(
            @PathVariable int orderId) throws Exception {

        Map<String, Object> response = new LinkedHashMap<>();

        String orderSql = """
            SELECT o.id, o.user_id, u.username, o.total_amount, o.status, o.ordered_at
            FROM orders o
            JOIN users u ON o.user_id = u.id
            WHERE o.id = ?
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(orderSql)) {

            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                response.put("id", rs.getInt("id"));
                response.put("user_id", rs.getInt("user_id"));
                response.put("username", rs.getString("username"));
                response.put("total_amount", rs.getBigDecimal("total_amount"));
                response.put("status", rs.getString("status"));
                response.put("ordered_at", String.valueOf(rs.getTimestamp("ordered_at")));
            } else {
                response.put("message", "Order not found");
                return ResponseEntity.status(404).body(response);
            }

            List<Map<String, Object>> items = new ArrayList<>();

            String itemSql = """
                SELECT oi.product_id, p.name AS product_name,
                       oi.quantity, oi.unit_price,
                       (oi.quantity * oi.unit_price) AS line_total
                FROM order_items oi
                JOIN products p ON oi.product_id = p.id
                WHERE oi.order_id = ?
            """;

            try (PreparedStatement itemStmt = conn.prepareStatement(itemSql)) {
                itemStmt.setInt(1, orderId);
                ResultSet itemRs = itemStmt.executeQuery();

                while (itemRs.next()) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("product_id", itemRs.getInt("product_id"));
                    item.put("product_name", itemRs.getString("product_name"));
                    item.put("quantity", itemRs.getInt("quantity"));
                    item.put("unit_price", itemRs.getBigDecimal("unit_price"));
                    item.put("line_total", itemRs.getBigDecimal("line_total"));

                    items.add(item);
                }
            }

            response.put("items", items);
        }

        return ResponseEntity.ok(response);
    }

    private int getInt(Object value) {
        if (value == null) {
            return 0;
        }

        if (value instanceof Integer) {
            return (Integer) value;
        }

        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        if (value instanceof String) {
            return Integer.parseInt((String) value);
        }

        return 0;
    }

    private static class OrderItemData {
        int productId;
        String productName;
        int quantity;
        BigDecimal unitPrice;
        BigDecimal lineTotal;
    }
}