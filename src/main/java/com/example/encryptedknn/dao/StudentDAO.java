package com.example.encryptedknn.dao;

import com.example.encryptedknn.model.Student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/encrypted_knn";
    private static final String DB_USER = "root"; // Change as needed
    private static final String DB_PASSWORD = ""; // Change as needed

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // Insert a new student record
    public boolean insertStudent(Student student) {
        String sql = "INSERT INTO students (student_id, name_encrypted, data_encrypted, iv, meta) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, student.getStudentId());
            stmt.setBytes(2, student.getNameEncrypted());
            stmt.setBytes(3, student.getDataEncrypted());
            stmt.setBytes(4, student.getIv());
            stmt.setString(5, student.getMeta());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get a student by student ID
    public Student getStudentByStudentId(String studentId) {
        String sql = "SELECT * FROM students WHERE student_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, studentId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToStudent(rs);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Get all students
    public List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM students ORDER BY created_at DESC";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                students.add(mapResultSetToStudent(rs));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }

    // Update student data
    public boolean updateStudent(Student student) {
        String sql = "UPDATE students SET name_encrypted = ?, data_encrypted = ?, iv = ?, meta = ? WHERE student_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBytes(1, student.getNameEncrypted());
            stmt.setBytes(2, student.getDataEncrypted());
            stmt.setBytes(3, student.getIv());
            stmt.setString(4, student.getMeta());
            stmt.setString(5, student.getStudentId());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete a student by student ID
    public boolean deleteStudent(String studentId) {
        String sql = "DELETE FROM students WHERE student_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, studentId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Check if a student exists
    public boolean studentExists(String studentId) {
        String sql = "SELECT COUNT(*) FROM students WHERE student_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, studentId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Helper method to map ResultSet to Student object
    private Student mapResultSetToStudent(ResultSet rs) throws SQLException {
        Student student = new Student();
        student.setId(rs.getInt("id"));
        student.setStudentId(rs.getString("student_id"));
        student.setNameEncrypted(rs.getBytes("name_encrypted"));
        student.setDataEncrypted(rs.getBytes("data_encrypted"));
        student.setIv(rs.getBytes("iv"));
        student.setMeta(rs.getString("meta"));
        student.setCreatedAt(rs.getTimestamp("created_at"));
        return student;
    }
}
