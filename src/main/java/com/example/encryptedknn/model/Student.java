package com.example.encryptedknn.model;

import java.sql.Timestamp;

public class Student {
    private int id;
    private String studentId;
    private byte[] nameEncrypted;
    private byte[] dataEncrypted;
    private byte[] iv;
    private String meta;
    private Timestamp createdAt;

    // Default constructor
    public Student() {}

    // Constructor with all fields
    public Student(int id, String studentId, byte[] nameEncrypted, byte[] dataEncrypted, 
                   byte[] iv, String meta, Timestamp createdAt) {
        this.id = id;
        this.studentId = studentId;
        this.nameEncrypted = nameEncrypted;
        this.dataEncrypted = dataEncrypted;
        this.iv = iv;
        this.meta = meta;
        this.createdAt = createdAt;
    }

    // Constructor without id and timestamp (for insertion)
    public Student(String studentId, byte[] nameEncrypted, byte[] dataEncrypted, 
                   byte[] iv, String meta) {
        this.studentId = studentId;
        this.nameEncrypted = nameEncrypted;
        this.dataEncrypted = dataEncrypted;
        this.iv = iv;
        this.meta = meta;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public byte[] getNameEncrypted() {
        return nameEncrypted;
    }

    public void setNameEncrypted(byte[] nameEncrypted) {
        this.nameEncrypted = nameEncrypted;
    }

    public byte[] getDataEncrypted() {
        return dataEncrypted;
    }

    public void setDataEncrypted(byte[] dataEncrypted) {
        this.dataEncrypted = dataEncrypted;
    }

    public byte[] getIv() {
        return iv;
    }

    public void setIv(byte[] iv) {
        this.iv = iv;
    }

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", studentId='" + studentId + '\'' +
                ", meta='" + meta + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
