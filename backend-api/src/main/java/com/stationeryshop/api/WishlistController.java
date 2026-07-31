package com.stationeryshop.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@RestController
@RequestMapping("/wishlists")
public class WishlistController {

    @Autowired
    private DataSource dataSource;

    // POST /wishlists — add product to wishlist
    @PostMapping
    public ResponseEntity<Map<String, Object>> addToWishlist(
            @RequestBody Map<String, Object> body) {

        Map<String, Object> response = new LinkedHashMap<>();

        int userId = getInt(body.get("user_id"));
        int productId = getInt(body.get("product_id"));

        if (userId <= 0 || productId <= 0) {
            response.put("success", false);
            response.put("message", "user_id and product_id are required");
            return ResponseEntity.badRequest().body(response);
        }

        try (Connection conn = dataSource.getConnection()) {

            // Check if already in wishlist
            String checkSql = "SELECT id FROM wishlists WHERE user_id = ? AND product_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(checkSql)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, productId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    response.put("success", false);
                    response.put("message", "Product already in wishlist");
                    return ResponseEntity.status(409).body(response);
                }
            }

            // Insert into wishlist
            String insertSql = "INSERT INTO wishlists (user_id, product_id) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, productId);
                stmt.executeUpdate();

                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    response.put("success", true);
                    response.put("message", "Added to wishlist");
                    response.put("wishlist_id", keys.getInt(1));
                    return ResponseEntity.status(201).body(response);
                }
            }

            response.put("success", false);
            response.put("message", "Failed to add to wishlist");
            return ResponseEntity.status(500).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // GET /wishlists/user/{userId} — get all wishlist items for a user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getWishlistByUser(
            @PathVariable int userId) throws Exception {

        List<Map<String, Object>> items = new ArrayList<>();

        String sql = """
            SELECT w.id, w.product_id, p.name, p.description, p.price,
                   p.stock_quantity, p.image_url, c.name AS category, w.created_at
            FROM wishlists w
            JOIN products p ON w.product_id = p.id
            JOIN categories c ON p.category_id = c.id
            WHERE w.user_id = ?
            ORDER BY w.created_at DESC
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
                item.put("stock_quantity", rs.getInt("stock_quantity"));
                item.put("image_url", rs.getString("image_url"));
                item.put("category", rs.getString("category"));
                item.put("created_at", String.valueOf(rs.getTimestamp("created_at")));
                items.add(item);
            }
        }

        return ResponseEntity.ok(items);
    }

    // DELETE /wishlists/{id} — remove from wishlist
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> removeFromWishlist(
            @PathVariable int id) throws Exception {

        Map<String, Object> response = new LinkedHashMap<>();

        String sql = "DELETE FROM wishlists WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                response.put("success", true);
                response.put("message", "Removed from wishlist");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Wishlist item not found");
                return ResponseEntity.status(404).body(response);
            }
        }
    }

    // DELETE /wishlists/user/{userId}/product/{productId} — remove by user and product
    @DeleteMapping("/user/{userId}/product/{productId}")
    public ResponseEntity<Map<String, Object>> removeByUserAndProduct(
            @PathVariable int userId, @PathVariable int productId) throws Exception {

        Map<String, Object> response = new LinkedHashMap<>();

        String sql = "DELETE FROM wishlists WHERE user_id = ? AND product_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                response.put("success", true);
                response.put("message", "Removed from wishlist");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Item not found in wishlist");
                return ResponseEntity.status(404).body(response);
            }
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
