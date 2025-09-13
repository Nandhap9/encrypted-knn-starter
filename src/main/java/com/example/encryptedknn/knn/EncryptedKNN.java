package com.example.encryptedknn.knn;

import com.example.encryptedknn.dao.StudentDAO;
import com.example.encryptedknn.model.Student;
import com.example.encryptedknn.util.AESUtil;

import javax.crypto.SecretKey;
import java.util.*;

public class EncryptedKNN {
    private StudentDAO studentDAO;
    private int k;

    public EncryptedKNN(int k) {
        this.k = k;
        this.studentDAO = new StudentDAO();
    }

    // Simple data point representation for KNN
    public static class DataPoint {
        public double[] features;
        public String label;
        public String studentId;

        public DataPoint(double[] features, String label, String studentId) {
            this.features = features;
            this.label = label;
            this.studentId = studentId;
        }
    }

    // Distance calculation (Euclidean distance)
    private double calculateDistance(double[] point1, double[] point2) {
        if (point1.length != point2.length) {
            throw new IllegalArgumentException("Feature vectors must have the same length");
        }

        double sum = 0.0;
        for (int i = 0; i < point1.length; i++) {
            sum += Math.pow(point1[i] - point2[i], 2);
        }
        return Math.sqrt(sum);
    }

    // Decrypt and parse student data into feature vector
    private DataPoint decryptStudentData(Student student, SecretKey key) {
        try {
            // Decrypt the data
            byte[] decryptedData = AESUtil.decrypt(student.getDataEncrypted(), key, student.getIv());
            byte[] decryptedName = AESUtil.decrypt(student.getNameEncrypted(), key, student.getIv());

            String dataStr = new String(decryptedData);
            String name = new String(decryptedName);

            // Parse the data string into features
            // Expected format: "feature1,feature2,feature3,...,label"
            String[] parts = dataStr.split(",");
            if (parts.length < 2) {
                throw new IllegalArgumentException("Invalid data format");
            }

            double[] features = new double[parts.length - 1];
            for (int i = 0; i < features.length; i++) {
                features[i] = Double.parseDouble(parts[i].trim());
            }

            String label = parts[parts.length - 1].trim();

            return new DataPoint(features, label, student.getStudentId());

        } catch (Exception e) {
            System.err.println("Error decrypting data for student " + student.getStudentId() + ": " + e.getMessage());
            return null;
        }
    }

    // Classify a new data point using K-NN
    public String classify(double[] queryFeatures, SecretKey key) {
        List<Student> allStudents = studentDAO.getAllStudents();
        List<DataPoint> trainingData = new ArrayList<>();

        // Decrypt all student data
        for (Student student : allStudents) {
            DataPoint point = decryptStudentData(student, key);
            if (point != null) {
                trainingData.add(point);
            }
        }

        if (trainingData.isEmpty()) {
            return "UNKNOWN";
        }

        // Calculate distances and find k nearest neighbors
        List<DistanceLabel> distances = new ArrayList<>();
        for (DataPoint point : trainingData) {
            double distance = calculateDistance(queryFeatures, point.features);
            distances.add(new DistanceLabel(distance, point.label));
        }

        // Sort by distance
        distances.sort(Comparator.comparingDouble(dl -> dl.distance));

        // Take k nearest neighbors
        Map<String, Integer> labelCounts = new HashMap<>();
        int actualK = Math.min(k, distances.size());

        for (int i = 0; i < actualK; i++) {
            String label = distances.get(i).label;
            labelCounts.put(label, labelCounts.getOrDefault(label, 0) + 1);
        }

        // Return the most frequent label
        return labelCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("UNKNOWN");
    }

    // Get k nearest neighbors with their details
    public List<NeighborInfo> getKNearestNeighbors(double[] queryFeatures, SecretKey key) {
        List<Student> allStudents = studentDAO.getAllStudents();
        List<DataPoint> trainingData = new ArrayList<>();

        // Decrypt all student data
        for (Student student : allStudents) {
            DataPoint point = decryptStudentData(student, key);
            if (point != null) {
                trainingData.add(point);
            }
        }

        if (trainingData.isEmpty()) {
            return new ArrayList<>();
        }

        // Calculate distances
        List<DistanceLabelInfo> distances = new ArrayList<>();
        for (DataPoint point : trainingData) {
            double distance = calculateDistance(queryFeatures, point.features);
            distances.add(new DistanceLabelInfo(distance, point.label, point.studentId, point.features));
        }

        // Sort by distance and take k nearest
        distances.sort(Comparator.comparingDouble(dli -> dli.distance));
        int actualK = Math.min(k, distances.size());

        List<NeighborInfo> neighbors = new ArrayList<>();
        for (int i = 0; i < actualK; i++) {
            DistanceLabelInfo dli = distances.get(i);
            neighbors.add(new NeighborInfo(dli.studentId, dli.label, dli.distance, dli.features));
        }

        return neighbors;
    }

    // Helper classes
    private static class DistanceLabel {
        double distance;
        String label;

        DistanceLabel(double distance, String label) {
            this.distance = distance;
            this.label = label;
        }
    }

    private static class DistanceLabelInfo {
        double distance;
        String label;
        String studentId;
        double[] features;

        DistanceLabelInfo(double distance, String label, String studentId, double[] features) {
            this.distance = distance;
            this.label = label;
            this.studentId = studentId;
            this.features = features;
        }
    }

    // Public class for neighbor information
    public static class NeighborInfo {
        public String studentId;
        public String label;
        public double distance;
        public double[] features;

        public NeighborInfo(String studentId, String label, double distance, double[] features) {
            this.studentId = studentId;
            this.label = label;
            this.distance = distance;
            this.features = features;
        }

        @Override
        public String toString() {
            return "NeighborInfo{" +
                    "studentId='" + studentId + '\'' +
                    ", label='" + label + '\'' +
                    ", distance=" + distance +
                    ", features=" + Arrays.toString(features) +
                    '}';
        }
    }

    // Getters and setters
    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }
}
