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

    // GET /products — all products
    @GetMapping
    public List<Map<String, Object>> getAllProducts() throws Exception {
        List<Map<String, Object>> products = new ArrayList<>();
        String sql = """
            SELECT p.id, p.name, c.name AS category,
                   p.price, p.stock_quantity
            FROM products p
            JOIN categories c ON p.category_id = c.id
            ORDER BY c.name, p.name
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

    // GET /products/search?name=pen — search products
    @GetMapping("/search")
    public List<Map<String, Object>> searchProducts(
            @RequestParam String name) throws Exception {
        List<Map<String, Object>> products = new ArrayList<>();
        String sql = """
            SELECT p.id, p.name, c.name AS category,
                   p.price, p.stock_quantity
            FROM products p
            JOIN categories c ON p.category_id = c.id
            WHERE p.name LIKE ?
            ORDER BY p.name
        """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + name + "%");
            ResultSet rs = stmt.executeQuery();
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

    // GET /products/5 — single product by id
    @GetMapping("/{id}")
    public Map<String, Object> getProductById(
            @PathVariable int id) throws Exception {
        Map<String, Object> product = new LinkedHashMap<>();
        String sql = """
            SELECT p.id, p.name, c.name AS category,
                   p.description, p.price, p.stock_quantity
            FROM products p
            JOIN categories c ON p.category_id = c.id
            WHERE p.id = ?
        """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                product.put("id", rs.getInt("id"));
                product.put("name", rs.getString("name"));
                product.put("category", rs.getString("category"));
                product.put("description", rs.getString("description"));
                product.put("price", rs.getDouble("price"));
                product.put("stock_quantity", rs.getInt("stock_quantity"));
            }
        }
        return product;
    }
}