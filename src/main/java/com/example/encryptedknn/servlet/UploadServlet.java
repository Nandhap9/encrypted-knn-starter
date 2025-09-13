package com.example.encryptedknn.servlet;

import com.example.encryptedknn.dao.StudentDAO;
import com.example.encryptedknn.model.Student;
import com.example.encryptedknn.util.AESUtil;

import javax.crypto.SecretKey;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/upload")
@MultipartConfig
public class UploadServlet extends HttpServlet {
    private StudentDAO studentDAO;

    @Override
    public void init() throws ServletException {
        studentDAO = new StudentDAO();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Check if user is authenticated and has admin role
        HttpSession session = request.getSession(false);
        if (session == null || !Boolean.TRUE.equals(session.getAttribute("authenticated")) ||
            !"ADMIN".equals(session.getAttribute("role"))) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"error\": \"Admin access required\"}");
            return;
        }

        String studentId = request.getParameter("studentId");
        String studentName = request.getParameter("studentName");
        String studentData = request.getParameter("studentData");
        String encryptionKey = request.getParameter("encryptionKey");
        String meta = request.getParameter("meta");

        if (studentId == null || studentName == null || studentData == null || encryptionKey == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Missing required fields\"}");
            return;
        }

        try {
            // Check if student already exists
            if (studentDAO.studentExists(studentId)) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                response.getWriter().write("{\"error\": \"Student ID already exists\"}");
                return;
            }

            // Convert the key string to SecretKey
            SecretKey key = AESUtil.keyFromString(encryptionKey);
            
            // Generate a random IV
            byte[] iv = AESUtil.generateIV();

            // Encrypt the student name and data
            byte[] encryptedName = AESUtil.encrypt(studentName.getBytes(), key, iv);
            byte[] encryptedData = AESUtil.encrypt(studentData.getBytes(), key, iv);

            // Create student object
            Student student = new Student(studentId, encryptedName, encryptedData, iv, meta);

            // Save to database
            if (studentDAO.insertStudent(student)) {
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": true, \"message\": \"Student data uploaded successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\": \"Failed to save student data\"}");
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Error processing student data: " + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Check if user is authenticated and has admin role
        HttpSession session = request.getSession(false);
        if (session == null || !Boolean.TRUE.equals(session.getAttribute("authenticated")) ||
            !"ADMIN".equals(session.getAttribute("role"))) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"error\": \"Admin access required\"}");
            return;
        }

        String studentId = request.getParameter("studentId");
        String studentName = request.getParameter("studentName");
        String studentData = request.getParameter("studentData");
        String encryptionKey = request.getParameter("encryptionKey");
        String meta = request.getParameter("meta");

        if (studentId == null || studentName == null || studentData == null || encryptionKey == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Missing required fields\"}");
            return;
        }

        try {
            // Check if student exists
            if (!studentDAO.studentExists(studentId)) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\": \"Student ID not found\"}");
                return;
            }

            // Convert the key string to SecretKey
            SecretKey key = AESUtil.keyFromString(encryptionKey);
            
            // Generate a new IV for updated data
            byte[] iv = AESUtil.generateIV();

            // Encrypt the student name and data
            byte[] encryptedName = AESUtil.encrypt(studentName.getBytes(), key, iv);
            byte[] encryptedData = AESUtil.encrypt(studentData.getBytes(), key, iv);

            // Create student object with updated data
            Student student = new Student(studentId, encryptedName, encryptedData, iv, meta);

            // Update in database
            if (studentDAO.updateStudent(student)) {
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": true, \"message\": \"Student data updated successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\": \"Failed to update student data\"}");
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Error updating student data: " + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Check if user is authenticated and has admin role
        HttpSession session = request.getSession(false);
        if (session == null || !Boolean.TRUE.equals(session.getAttribute("authenticated")) ||
            !"ADMIN".equals(session.getAttribute("role"))) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"error\": \"Admin access required\"}");
            return;
        }

        try {
            response.setContentType("application/json");
            response.getWriter().write("{\"message\": \"Upload endpoint ready\", \"totalStudents\": " + 
                studentDAO.getAllStudents().size() + "}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Error retrieving student count\"}");
        }
    }
}
