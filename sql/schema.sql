CREATE DATABASE IF NOT EXISTS encrypted_knn;
USE encrypted_knn;

CREATE TABLE IF NOT EXISTS users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(100) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role ENUM('ADMIN','STUDENT') NOT NULL DEFAULT 'STUDENT'
);

CREATE TABLE IF NOT EXISTS students (
  id INT AUTO_INCREMENT PRIMARY KEY,
  student_id VARCHAR(100) NOT NULL UNIQUE,
  name_encrypted BLOB NOT NULL,
  data_encrypted BLOB NOT NULL,
  iv VARBINARY(16) NOT NULL,
  meta VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- sample admin
INSERT INTO users (username, password_hash, role) VALUES ('admin', 'admin_hashed_placeholder','ADMIN');
