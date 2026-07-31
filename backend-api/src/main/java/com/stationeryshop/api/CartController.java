package com.stationeryshop.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private DataSource dataSource;

    // POST /cart — add item to cart (or update quantity if exists)
    @PostMapping
    public ResponseEntity<Map<String, Object>> addToCart(
            @RequestBody Map<String, Object> body) {

        Map<String, Object> response = new LinkedHashMap<>();

        int userId = getInt(body.get("user_id"));
        int productId = getInt(body.get("product_id"));
        int quantity = getInt(body.get("quantity"));

        if (userId <= 0 || productId <= 0) {
            response.put("success", false);
            response.put("message", "user_id and product_id are required");
            return ResponseEntity.badRequest().body(response);
        }

        if (quantity <= 0) quantity = 1;

        try (Connection conn = dataSource.getConnection()) {

            // Check stock
            String stockSql = "SELECT stock_quantity FROM products WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(stockSql)) {
                stmt.setInt(1, productId);
                ResultSet rs = stmt.executeQuery();
                if (!rs.next()) {
                    response.put("success", false);
                    response.put("message", "Product not found");
                    return ResponseEntity.status(404).body(response);
                }
                int stock = rs.getInt("stock_quantity");
                if (stock < quantity) {
                    response.put("success", false);
                    response.put("message", "Not enough stock. Available: " + stock);
                    return ResponseEntity.badRequest().body(response);
                }
            }

            // Check if already in cart
            String checkSql = "SELECT id, quantity FROM cart_items WHERE user_id = ? AND product_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(checkSql)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, productId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    // Update quantity
                    int cartId = rs.getInt("id");
                    int existingQty = rs.getInt("quantity");
                    int newQty = existingQty + quantity;

                    String updateSql = "UPDATE cart_items SET quantity = ? WHERE id = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setInt(1, newQty);
                        updateStmt.setInt(2, cartId);
                        updateStmt.executeUpdate();
                    }

                    response.put("success", true);
                    response.put("message", "Cart updated");
                    response.put("cart_id", cartId);
                    response.put("quantity", newQty);
                    return ResponseEntity.ok(response);
                }
            }

            // Insert new cart item
            String insertSql = "INSERT INTO cart_items (user_id, product_id, quantity) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, productId);
                stmt.setInt(3, quantity);
                stmt.executeUpdate();

                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    response.put("success", true);
                    response.put("message", "Added to cart");
                    response.put("cart_id", keys.getInt(1));
                    response.put("quantity", quantity);
                    return ResponseEntity.status(201).body(response);
                }
            }

            response.put("success", false);
            response.put("message", "Failed to add to cart");
            return ResponseEntity.status(500).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // GET /cart/user/{userId} — get all cart items for a user
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getCartByUser(
            @PathVariable int userId) throws Exception {

        Map<String, Object> result = new LinkedHashMap<>();
        List<Map<String, Object>> items = new ArrayList<>();
        double totalAmount = 0;

        String sql = """
            SELECT ci.id, ci.product_id, ci.quantity, p.name, p.description,
                   p.price, p.stock_quantity, p.image_url, c.name AS category
            FROM cart_items ci
            JOIN products p ON ci.product_id = p.id
            JOIN categories c ON p.category_id = c.id
            WHERE ci.user_id = ?
            ORDER BY ci.created_at DESC
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", rs.getInt("id"));
                item.put("product_id", rs.getInt("product_id"));
                item.put("name", rs.getString("name"));
                item.put("description", rs.getString("description"));
                item.put("price", rs.getDouble("price"));
                item.put("quantity", rs.getInt("quantity"));
                item.put("stock_quantity", rs.getInt("stock_quantity"));
                item.put("image_url", rs.getString("image_url"));
                item.put("category", rs.getString("category"));

                double lineTotal = rs.getDouble("price") * rs.getInt("quantity");
                item.put("line_total", lineTotal);
                totalAmount += lineTotal;

                items.add(item);
            }
        }

        result.put("items", items);
        result.put("item_count", items.size());
        result.put("total_amount", totalAmount);

        return ResponseEntity.ok(result);
    }

    // PUT /cart/{id} — update cart item quantity
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateCartItem(
            @PathVariable int id, @RequestBody Map<String, Object> body) throws Exception {

        Map<String, Object> response = new LinkedHashMap<>();
        int quantity = getInt(body.get("quantity"));

        if (quantity <= 0) {
            response.put("success", false);
            response.put("message", "Quantity must be greater than 0");
            return ResponseEntity.badRequest().body(response);
        }

        String sql = "UPDATE cart_items SET quantity = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantity);
            stmt.setInt(2, id);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                response.put("success", true);
                response.put("message", "Cart updated");
                response.put("quantity", quantity);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Cart item not found");
                return ResponseEntity.status(404).body(response);
            }
        }
    }

    // DELETE /cart/{id} — remove single item from cart
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> removeCartItem(
            @PathVariable int id) throws Exception {

        Map<String, Object> response = new LinkedHashMap<>();

        String sql = "DELETE FROM cart_items WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                response.put("success", true);
                response.put("message", "Removed from cart");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Cart item not found");
                return ResponseEntity.status(404).body(response);
            }
        }
    }

    // DELETE /cart/user/{userId} — clear entire cart
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> clearCart(
            @PathVariable int userId) throws Exception {

        Map<String, Object> response = new LinkedHashMap<>();

        String sql = "DELETE FROM cart_items WHERE user_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            int rows = stmt.executeUpdate();

            response.put("success", true);
            response.put("message", "Cart cleared");
            response.put("items_removed", rows);
            return ResponseEntity.ok(response);
        }
    }

    private int getInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        if (value instanceof String) return Integer.parseInt((String) value);
        return 0;
    }
}
