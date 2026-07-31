package com.stationeryshop.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private DataSource dataSource;

    // GET /categories — all categories
    @GetMapping
    public List<Map<String, Object>> getAllCategories() throws Exception {
        List<Map<String, Object>> categories = new ArrayList<>();
        String sql = "SELECT id, name, description FROM categories ORDER BY name";

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> c = new LinkedHashMap<>();
                c.put("id", rs.getInt("id"));
                c.put("name", rs.getString("name"));
                c.put("description", rs.getString("description"));
                categories.add(c);
            }
        }
        return categories;
    }

    // GET /categories/{id} — single category
    @GetMapping("/{id}")
    public Map<String, Object> getCategoryById(@PathVariable int id) throws Exception {
        Map<String, Object> category = new LinkedHashMap<>();
        String sql = "SELECT id, name, description FROM categories WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                category.put("id", rs.getInt("id"));
                category.put("name", rs.getString("name"));
                category.put("description", rs.getString("description"));
            }
        }
        return category;
    }
}
