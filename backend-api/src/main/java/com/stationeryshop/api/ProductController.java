package com.stationeryshop.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private DataSource dataSource;

    // GET /products — all products with average rating
    @GetMapping
    public List<Map<String, Object>> getAllProducts() throws Exception {
        List<Map<String, Object>> products = new ArrayList<>();
        String sql = """
            SELECT p.id, p.name, c.name AS category, c.id AS category_id,
                   p.description, p.price, p.stock_quantity, p.image_url,
                   COALESCE(AVG(r.rating), 0) AS avg_rating,
                   COUNT(r.id) AS review_count
            FROM products p
            JOIN categories c ON p.category_id = c.id
            LEFT JOIN reviews r ON p.id = r.product_id
            GROUP BY p.id, p.name, c.name, c.id, p.description, p.price, p.stock_quantity, p.image_url
            ORDER BY c.name, p.name
        """;
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                products.add(mapProduct(rs));
            }
        }
        return products;
    }

    // GET /products/popular — most ordered products
    @GetMapping("/popular")
    public List<Map<String, Object>> getPopularProducts() throws Exception {
        List<Map<String, Object>> products = new ArrayList<>();
        String sql = """
            SELECT p.id, p.name, c.name AS category, c.id AS category_id,
                   p.description, p.price, p.stock_quantity, p.image_url,
                   COALESCE(AVG(r.rating), 0) AS avg_rating,
                   COUNT(DISTINCT r.id) AS review_count,
                   COALESCE(SUM(oi.quantity), 0) AS total_ordered
            FROM products p
            JOIN categories c ON p.category_id = c.id
            LEFT JOIN reviews r ON p.id = r.product_id
            LEFT JOIN order_items oi ON p.id = oi.product_id
            GROUP BY p.id, p.name, c.name, c.id, p.description, p.price, p.stock_quantity, p.image_url
            ORDER BY total_ordered DESC
            LIMIT 6
        """;
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> p = mapProduct(rs);
                p.put("total_ordered", rs.getInt("total_ordered"));
                products.add(p);
            }
        }
        return products;
    }

    // GET /products/category/{categoryId} — products by category
    @GetMapping("/category/{categoryId}")
    public List<Map<String, Object>> getProductsByCategory(
            @PathVariable int categoryId) throws Exception {
        List<Map<String, Object>> products = new ArrayList<>();
        String sql = """
            SELECT p.id, p.name, c.name AS category, c.id AS category_id,
                   p.description, p.price, p.stock_quantity, p.image_url,
                   COALESCE(AVG(r.rating), 0) AS avg_rating,
                   COUNT(r.id) AS review_count
            FROM products p
            JOIN categories c ON p.category_id = c.id
            LEFT JOIN reviews r ON p.id = r.product_id
            WHERE p.category_id = ?
            GROUP BY p.id, p.name, c.name, c.id, p.description, p.price, p.stock_quantity, p.image_url
            ORDER BY p.name
        """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, categoryId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                products.add(mapProduct(rs));
            }
        }
        return products;
    }

    // GET /products/search?name=pen — search products
    @GetMapping("/search")
    public List<Map<String, Object>> searchProducts(
            @RequestParam String name) throws Exception {
        List<Map<String, Object>> products = new ArrayList<>();
        String sql = """
            SELECT p.id, p.name, c.name AS category, c.id AS category_id,
                   p.description, p.price, p.stock_quantity, p.image_url,
                   COALESCE(AVG(r.rating), 0) AS avg_rating,
                   COUNT(r.id) AS review_count
            FROM products p
            JOIN categories c ON p.category_id = c.id
            LEFT JOIN reviews r ON p.id = r.product_id
            WHERE p.name LIKE ?
            GROUP BY p.id, p.name, c.name, c.id, p.description, p.price, p.stock_quantity, p.image_url
            ORDER BY p.name
        """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + name + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                products.add(mapProduct(rs));
            }
        }
        return products;
    }

    // GET /products/5 — single product by id with rating
    @GetMapping("/{id}")
    public Map<String, Object> getProductById(
            @PathVariable int id) throws Exception {
        Map<String, Object> product = new LinkedHashMap<>();
        String sql = """
            SELECT p.id, p.name, c.name AS category, c.id AS category_id,
                   p.description, p.price, p.stock_quantity, p.image_url,
                   COALESCE(AVG(r.rating), 0) AS avg_rating,
                   COUNT(r.id) AS review_count
            FROM products p
            JOIN categories c ON p.category_id = c.id
            LEFT JOIN reviews r ON p.id = r.product_id
            WHERE p.id = ?
            GROUP BY p.id, p.name, c.name, c.id, p.description, p.price, p.stock_quantity, p.image_url
        """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                product = mapProduct(rs);
            }
        }
        return product;
    }

    private Map<String, Object> mapProduct(ResultSet rs) throws Exception {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("id", rs.getInt("id"));
        p.put("name", rs.getString("name"));
        p.put("category", rs.getString("category"));
        p.put("category_id", rs.getInt("category_id"));
        p.put("description", rs.getString("description"));
        p.put("price", rs.getDouble("price"));
        p.put("stock_quantity", rs.getInt("stock_quantity"));
        p.put("image_url", rs.getString("image_url"));
        p.put("avg_rating", Math.round(rs.getDouble("avg_rating") * 10.0) / 10.0);
        p.put("review_count", rs.getInt("review_count"));
        return p;
    }
}
