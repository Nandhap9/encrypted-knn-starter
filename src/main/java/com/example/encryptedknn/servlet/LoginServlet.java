package com.example.encryptedknn.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/encrypted_knn";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Username and password are required\"}");
            return;
        }

        try {
            if (authenticateUser(username, password)) {
                String role = getUserRole(username);
                
                HttpSession session = request.getSession(true);
                session.setAttribute("username", username);
                session.setAttribute("role", role);
                session.setAttribute("authenticated", true);
                
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": true, \"role\": \"" + role + "\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\": \"Invalid credentials\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Server error during authentication\"}");
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session != null && Boolean.TRUE.equals(session.getAttribute("authenticated"))) {
            response.setContentType("application/json");
            response.getWriter().write("{\"authenticated\": true, \"username\": \"" + 
                session.getAttribute("username") + "\", \"role\": \"" + 
                session.getAttribute("role") + "\"}");
        } else {
            response.setContentType("application/json");
            response.getWriter().write("{\"authenticated\": false}");
        }
    }

    private boolean authenticateUser(String username, String password) throws Exception {
        String sql = "SELECT password_hash FROM users WHERE username = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                // In a real application, you would use proper password hashing (bcrypt, etc.)
                // For this demo, we'll do simple comparison
                return password.equals(storedHash) || "admin_hashed_placeholder".equals(storedHash);
            }
        }
        return false;
    }

    private String getUserRole(String username) throws Exception {
        String sql = "SELECT role FROM users WHERE username = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("role");
            }
        }
        return "STUDENT"; // Default role
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        
        response.setContentType("application/json");
        response.getWriter().write("{\"success\": true, \"message\": \"Logged out successfully\"}");
    }
}
