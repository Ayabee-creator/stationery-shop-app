package com.stationeryshop.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private DataSource dataSource;

    // POST /reviews — submit a review
    @PostMapping
    public ResponseEntity<Map<String, Object>> addReview(
            @RequestBody Map<String, Object> body) {

        Map<String, Object> response = new LinkedHashMap<>();

        int userId = getInt(body.get("user_id"));
        int productId = getInt(body.get("product_id"));
        int rating = getInt(body.get("rating"));
        String comment = (String) body.get("comment");

        if (userId <= 0 || productId <= 0) {
            response.put("success", false);
            response.put("message", "user_id and product_id are required");
            return ResponseEntity.badRequest().body(response);
        }

        if (rating < 1 || rating > 5) {
            response.put("success", false);
            response.put("message", "Rating must be between 1 and 5");
            return ResponseEntity.badRequest().body(response);
        }

        try (Connection conn = dataSource.getConnection()) {

            // Check user exists
            String userSql = "SELECT id FROM users WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(userSql)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();

                if (!rs.next()) {
                    response.put("success", false);
                    response.put("message", "User not found");
                    return ResponseEntity.status(404).body(response);
                }
            }

            // Check product exists
            String productSql = "SELECT id FROM products WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(productSql)) {
                stmt.setInt(1, productId);
                ResultSet rs = stmt.executeQuery();

                if (!rs.next()) {
                    response.put("success", false);
                    response.put("message", "Product not found");
                    return ResponseEntity.status(404).body(response);
                }
            }

            String insertSql = """
                INSERT INTO reviews (user_id, product_id, rating, comment)
                VALUES (?, ?, ?, ?)
            """;

            try (PreparedStatement stmt = conn.prepareStatement(
                    insertSql, Statement.RETURN_GENERATED_KEYS)) {

                stmt.setInt(1, userId);
                stmt.setInt(2, productId);
                stmt.setInt(3, rating);
                stmt.setString(4, comment);
                stmt.executeUpdate();

                ResultSet keys = stmt.getGeneratedKeys();

                if (keys.next()) {
                    response.put("success", true);
                    response.put("message", "Review submitted successfully");
                    response.put("review_id", keys.getInt(1));
                    response.put("user_id", userId);
                    response.put("product_id", productId);
                    response.put("rating", rating);

                    return ResponseEntity.status(201).body(response);
                }
            }

            response.put("success", false);
            response.put("message", "Failed to submit review");
            return ResponseEntity.status(500).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Review failed: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // GET /reviews/product/{productId} — get all reviews for a product
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Map<String, Object>>> getReviewsByProduct(
            @PathVariable int productId) throws Exception {

        List<Map<String, Object>> reviews = new ArrayList<>();

        String sql = """
            SELECT r.id, r.user_id, u.username,
                   r.product_id, p.name AS product_name,
                   r.rating, r.comment, r.created_at
            FROM reviews r
            JOIN users u ON r.user_id = u.id
            JOIN products p ON r.product_id = p.id
            WHERE r.product_id = ?
            ORDER BY r.created_at DESC
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> review = new LinkedHashMap<>();
                review.put("id", rs.getInt("id"));
                review.put("user_id", rs.getInt("user_id"));
                review.put("username", rs.getString("username"));
                review.put("product_id", rs.getInt("product_id"));
                review.put("product_name", rs.getString("product_name"));
                review.put("rating", rs.getInt("rating"));
                review.put("comment", rs.getString("comment"));
                review.put("created_at", String.valueOf(rs.getTimestamp("created_at")));

                reviews.add(review);
            }
        }

        return ResponseEntity.ok(reviews);
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
}