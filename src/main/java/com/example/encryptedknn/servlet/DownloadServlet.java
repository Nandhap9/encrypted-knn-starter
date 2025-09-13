package com.example.encryptedknn.servlet;

import com.example.encryptedknn.dao.StudentDAO;
import com.example.encryptedknn.model.Student;
import com.example.encryptedknn.util.AESUtil;

import javax.crypto.SecretKey;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

@WebServlet("/download")
public class DownloadServlet extends HttpServlet {
    private StudentDAO studentDAO;

    @Override
    public void init() throws ServletException {
        studentDAO = new StudentDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Check if user is authenticated
        HttpSession session = request.getSession(false);
        if (session == null || !Boolean.TRUE.equals(session.getAttribute("authenticated"))) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Authentication required\"}");
            return;
        }

        String studentId = request.getParameter("studentId");
        String encryptionKey = request.getParameter("encryptionKey");
        String action = request.getParameter("action");

        // Handle different actions
        if ("list".equals(action)) {
            handleListStudents(request, response, session);
        } else if (studentId != null && encryptionKey != null) {
            handleGetStudent(request, response, session, studentId, encryptionKey);
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Missing required parameters\"}");
        }
    }

    private void handleListStudents(HttpServletRequest request, HttpServletResponse response, HttpSession session) 
            throws IOException {
        
        try {
            List<Student> students = studentDAO.getAllStudents();
            
            StringBuilder jsonResponse = new StringBuilder();
            jsonResponse.append("{\"success\": true, \"students\": [");
            
            for (int i = 0; i < students.size(); i++) {
                Student student = students.get(i);
                if (i > 0) jsonResponse.append(", ");
                
                jsonResponse.append("{")
                    .append("\"studentId\": \"").append(student.getStudentId()).append("\", ")
                    .append("\"meta\": \"").append(student.getMeta() != null ? student.getMeta() : "").append("\", ")
                    .append("\"createdAt\": \"").append(student.getCreatedAt()).append("\"")
                    .append("}");
            }
            
            jsonResponse.append("]}");
            
            response.setContentType("application/json");
            response.getWriter().write(jsonResponse.toString());
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Error retrieving student list\"}");
            e.printStackTrace();
        }
    }

    private void handleGetStudent(HttpServletRequest request, HttpServletResponse response, 
                                 HttpSession session, String studentId, String encryptionKey) 
            throws IOException {
        
        String userRole = (String) session.getAttribute("role");
        String sessionUsername = (String) session.getAttribute("username");

        // Students can only access their own data (assuming studentId matches username)
        // Admins can access any student data
        if ("STUDENT".equals(userRole) && !studentId.equals(sessionUsername)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"error\": \"Access denied to this student's data\"}");
            return;
        }

        try {
            Student student = studentDAO.getStudentByStudentId(studentId);
            
            if (student == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\": \"Student not found\"}");
                return;
            }

            // Convert the key string to SecretKey
            SecretKey key = AESUtil.keyFromString(encryptionKey);

            // Decrypt the student data
            byte[] decryptedName = AESUtil.decrypt(student.getNameEncrypted(), key, student.getIv());
            byte[] decryptedData = AESUtil.decrypt(student.getDataEncrypted(), key, student.getIv());

            String studentName = new String(decryptedName);
            String studentData = new String(decryptedData);

            // Build JSON response
            StringBuilder jsonResponse = new StringBuilder();
            jsonResponse.append("{")
                .append("\"success\": true, ")
                .append("\"studentId\": \"").append(student.getStudentId()).append("\", ")
                .append("\"studentName\": \"").append(studentName).append("\", ")
                .append("\"studentData\": \"").append(studentData).append("\", ")
                .append("\"meta\": \"").append(student.getMeta() != null ? student.getMeta() : "").append("\", ")
                .append("\"createdAt\": \"").append(student.getCreatedAt()).append("\"")
                .append("}");

            response.setContentType("application/json");
            response.getWriter().write(jsonResponse.toString());

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Error decrypting student data: " + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Check if user is authenticated
        HttpSession session = request.getSession(false);
        if (session == null || !Boolean.TRUE.equals(session.getAttribute("authenticated"))) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Authentication required\"}");
            return;
        }

        String action = request.getParameter("action");
        
        if ("decrypt".equals(action)) {
            handleBulkDecryption(request, response, session);
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Invalid action\"}");
        }
    }

    private void handleBulkDecryption(HttpServletRequest request, HttpServletResponse response, HttpSession session) 
            throws IOException {
        
        String userRole = (String) session.getAttribute("role");
        String encryptionKey = request.getParameter("encryptionKey");
        
        // Only admins can perform bulk operations
        if (!"ADMIN".equals(userRole)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"error\": \"Admin access required\"}");
            return;
        }

        if (encryptionKey == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Encryption key required\"}");
            return;
        }

        try {
            List<Student> students = studentDAO.getAllStudents();
            SecretKey key = AESUtil.keyFromString(encryptionKey);
            
            StringBuilder jsonResponse = new StringBuilder();
            jsonResponse.append("{\"success\": true, \"students\": [");
            
            for (int i = 0; i < students.size(); i++) {
                Student student = students.get(i);
                if (i > 0) jsonResponse.append(", ");
                
                try {
                    byte[] decryptedName = AESUtil.decrypt(student.getNameEncrypted(), key, student.getIv());
                    byte[] decryptedData = AESUtil.decrypt(student.getDataEncrypted(), key, student.getIv());

                    String studentName = new String(decryptedName);
                    String studentData = new String(decryptedData);

                    jsonResponse.append("{")
                        .append("\"studentId\": \"").append(student.getStudentId()).append("\", ")
                        .append("\"studentName\": \"").append(studentName).append("\", ")
                        .append("\"studentData\": \"").append(studentData).append("\", ")
                        .append("\"meta\": \"").append(student.getMeta() != null ? student.getMeta() : "").append("\", ")
                        .append("\"createdAt\": \"").append(student.getCreatedAt()).append("\"")
                        .append("}");
                } catch (Exception e) {
                    // If decryption fails for this student, include error info
                    jsonResponse.append("{")
                        .append("\"studentId\": \"").append(student.getStudentId()).append("\", ")
                        .append("\"error\": \"Decryption failed\"")
                        .append("}");
                }
            }
            
            jsonResponse.append("]}");
            
            response.setContentType("application/json");
            response.getWriter().write(jsonResponse.toString());
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Error during bulk decryption: " + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }
}
