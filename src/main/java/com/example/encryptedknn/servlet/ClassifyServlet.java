package com.example.encryptedknn.servlet;

import com.example.encryptedknn.knn.EncryptedKNN;
import com.example.encryptedknn.util.AESUtil;

import javax.crypto.SecretKey;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/classify")
public class ClassifyServlet extends HttpServlet {
    private EncryptedKNN knnClassifier;

    @Override
    public void init() throws ServletException {
        // Default k=3, can be configured
        knnClassifier = new EncryptedKNN(3);
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
        
        if ("classify".equals(action)) {
            handleClassification(request, response, session);
        } else if ("neighbors".equals(action)) {
            handleGetNeighbors(request, response, session);
        } else if ("config".equals(action)) {
            handleConfiguration(request, response, session);
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Invalid action. Use 'classify', 'neighbors', or 'config'\"}");
        }
    }

    private void handleClassification(HttpServletRequest request, HttpServletResponse response, HttpSession session) 
            throws IOException {
        
        String featuresParam = request.getParameter("features");
        String encryptionKey = request.getParameter("encryptionKey");

        if (featuresParam == null || encryptionKey == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Features and encryption key are required\"}");
            return;
        }

        try {
            // Parse features from comma-separated string
            String[] featureStrings = featuresParam.split(",");
            double[] features = new double[featureStrings.length];
            
            for (int i = 0; i < featureStrings.length; i++) {
                features[i] = Double.parseDouble(featureStrings[i].trim());
            }

            // Convert the key string to SecretKey
            SecretKey key = AESUtil.keyFromString(encryptionKey);

            // Perform classification
            String predictedLabel = knnClassifier.classify(features, key);

            // Build JSON response
            StringBuilder jsonResponse = new StringBuilder();
            jsonResponse.append("{")
                .append("\"success\": true, ")
                .append("\"predictedLabel\": \"").append(predictedLabel).append("\", ")
                .append("\"features\": [");
            
            for (int i = 0; i < features.length; i++) {
                if (i > 0) jsonResponse.append(", ");
                jsonResponse.append(features[i]);
            }
            
            jsonResponse.append("], ")
                .append("\"k\": ").append(knnClassifier.getK())
                .append("}");

            response.setContentType("application/json");
            response.getWriter().write(jsonResponse.toString());

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Invalid feature format. Use comma-separated numbers\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Classification error: " + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }

    private void handleGetNeighbors(HttpServletRequest request, HttpServletResponse response, HttpSession session) 
            throws IOException {
        
        String featuresParam = request.getParameter("features");
        String encryptionKey = request.getParameter("encryptionKey");

        if (featuresParam == null || encryptionKey == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Features and encryption key are required\"}");
            return;
        }

        try {
            // Parse features from comma-separated string
            String[] featureStrings = featuresParam.split(",");
            double[] features = new double[featureStrings.length];
            
            for (int i = 0; i < featureStrings.length; i++) {
                features[i] = Double.parseDouble(featureStrings[i].trim());
            }

            // Convert the key string to SecretKey
            SecretKey key = AESUtil.keyFromString(encryptionKey);

            // Get k nearest neighbors
            List<EncryptedKNN.NeighborInfo> neighbors = knnClassifier.getKNearestNeighbors(features, key);

            // Build JSON response
            StringBuilder jsonResponse = new StringBuilder();
            jsonResponse.append("{")
                .append("\"success\": true, ")
                .append("\"queryFeatures\": [");
            
            for (int i = 0; i < features.length; i++) {
                if (i > 0) jsonResponse.append(", ");
                jsonResponse.append(features[i]);
            }
            
            jsonResponse.append("], ")
                .append("\"k\": ").append(knnClassifier.getK()).append(", ")
                .append("\"neighbors\": [");

            for (int i = 0; i < neighbors.size(); i++) {
                if (i > 0) jsonResponse.append(", ");
                
                EncryptedKNN.NeighborInfo neighbor = neighbors.get(i);
                jsonResponse.append("{")
                    .append("\"studentId\": \"").append(neighbor.studentId).append("\", ")
                    .append("\"label\": \"").append(neighbor.label).append("\", ")
                    .append("\"distance\": ").append(neighbor.distance).append(", ")
                    .append("\"features\": [");
                
                for (int j = 0; j < neighbor.features.length; j++) {
                    if (j > 0) jsonResponse.append(", ");
                    jsonResponse.append(neighbor.features[j]);
                }
                
                jsonResponse.append("]")
                    .append("}");
            }
            
            jsonResponse.append("]")
                .append("}");

            response.setContentType("application/json");
            response.getWriter().write(jsonResponse.toString());

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Invalid feature format. Use comma-separated numbers\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Neighbors retrieval error: " + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }

    private void handleConfiguration(HttpServletRequest request, HttpServletResponse response, HttpSession session) 
            throws IOException {
        
        String userRole = (String) session.getAttribute("role");
        
        // Only admins can change configuration
        if (!"ADMIN".equals(userRole)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"error\": \"Admin access required for configuration changes\"}");
            return;
        }

        String kParam = request.getParameter("k");
        
        if (kParam != null) {
            try {
                int newK = Integer.parseInt(kParam);
                if (newK < 1) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"error\": \"k must be a positive integer\"}");
                    return;
                }
                
                knnClassifier.setK(newK);
                
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": true, \"message\": \"k value updated\", \"k\": " + newK + "}");
                
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Invalid k value. Must be an integer\"}");
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"k parameter required for configuration\"}");
        }
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

        // Return current configuration
        response.setContentType("application/json");
        response.getWriter().write("{" +
            "\"success\": true, " +
            "\"k\": " + knnClassifier.getK() + ", " +
            "\"message\": \"KNN Classifier ready\"" +
            "}");
    }
}
