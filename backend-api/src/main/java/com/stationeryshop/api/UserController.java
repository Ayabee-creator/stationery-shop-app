package com.stationeryshop.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private DataSource dataSource;

    // POST /users/register — create a new account
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(
            @RequestBody Map<String, String> body) throws Exception {

        String username = body.get("username");
        String email    = body.get("email");
        String password = body.get("password");
        String phone    = body.get("phone");
        String address  = body.get("address");

        Map<String, Object> response = new LinkedHashMap<>();

        // Check if email already exists
        String checkSql = "SELECT id FROM users WHERE email = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(checkSql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                response.put("success", false);
                response.put("message", "Email already registered");
                return ResponseEntity.status(409).body(response);
            }
        }

        // Insert new user
        String insertSql = """
            INSERT INTO users (username, email, password, phone, address)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     insertSql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, password);
            stmt.setString(4, phone);
            stmt.setString(5, address);
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                response.put("success", true);
                response.put("message", "Account created successfully");
                response.put("user_id", keys.getInt(1));
            }
        }
        return ResponseEntity.status(201).body(response);
    }

    // POST /users/login — log in
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody Map<String, String> body) throws Exception {

        String email    = body.get("email");
        String password = body.get("password");

        Map<String, Object> response = new LinkedHashMap<>();

        String sql = """
            SELECT id, username, email, phone, address
            FROM users
            WHERE email = ? AND password = ?
        """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                response.put("success", true);
                response.put("message", "Login successful");
                response.put("user_id", rs.getInt("id"));
                response.put("username", rs.getString("username"));
                response.put("email", rs.getString("email"));
                response.put("phone", rs.getString("phone"));
                response.put("address", rs.getString("address"));
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Invalid email or password");
                return ResponseEntity.status(401).body(response);
            }
        }
    }

    // GET /users/1 — get a user's profile
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUser(
            @PathVariable int id) throws Exception {

        Map<String, Object> response = new LinkedHashMap<>();
        String sql = "SELECT id, username, email, phone, address FROM users WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                response.put("id", rs.getInt("id"));
                response.put("username", rs.getString("username"));
                response.put("email", rs.getString("email"));
                response.put("phone", rs.getString("phone"));
                response.put("address", rs.getString("address"));
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "User not found");
                return ResponseEntity.status(404).body(response);
            }
        }
    }
}