# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project Overview

This is a Java Web Application that demonstrates AES-CBC encryption for student data storage with K-NN classification capabilities. It's built as a Maven WAR project deployable to Tomcat 8/9, featuring encrypted data storage in MySQL and REST API endpoints for data management and machine learning operations.

## Architecture

### Core Components

- **Encryption Layer** (`util/AESUtil.java`): AES-256-CBC encryption with random IVs for all sensitive data
- **Data Layer** (`dao/StudentDAO.java`): JDBC-based persistence with encrypted blob storage
- **Model** (`model/Student.java`): Entity representing encrypted student records
- **ML Engine** (`knn/EncryptedKNN.java`): K-NN classifier that operates on decrypted data in memory
- **API Layer** (servlet package): RESTful servlets handling authentication, CRUD operations, and ML inference

### Security Architecture

- **Session-based Authentication**: HttpSession with role-based access control (ADMIN/STUDENT)
- **Encrypted Storage**: All student names and data encrypted at rest with AES-256-CBC
- **IV per Record**: Each encrypted record uses a unique random initialization vector
- **Role Segregation**: Admin operations (upload/bulk decrypt) vs Student operations (individual access)

### Data Flow

1. **Upload**: Admin encrypts student data → stores encrypted blob + IV in MySQL
2. **Classification**: Retrieve encrypted records → decrypt in memory → run K-NN → return prediction
3. **Download**: Decrypt specific student data using provided encryption key

## Common Development Commands

### Build & Deploy

```bash
# Clean build and package WAR
mvn clean package

# Deploy to local Tomcat (copy WAR method)
cp target/encrypted-knn-starter-1.0.0.war $TOMCAT_HOME/webapps/

# Run tests (if implemented)
mvn test

# Compile only (faster during development)
mvn compile

# Clean build artifacts
mvn clean
```

### Database Setup

```bash
# Initialize database with schema
mysql -u root -p < sql/schema.sql

# Check database connection
mysql -u root -p -e "USE encrypted_knn; SHOW TABLES;"
```

### Testing API Endpoints

```bash
# Login and store session
curl -c cookies.txt -X POST "http://localhost:8080/encrypted-knn-starter-1.0.0/login" -d "username=admin&password=admin_hashed_placeholder"

# Upload encrypted student data
curl -b cookies.txt -X POST "http://localhost:8080/encrypted-knn-starter-1.0.0/upload" -d "studentId=STU001&studentName=Alice&studentData=90,85,92,GRADE_A&encryptionKey=YOUR_BASE64_KEY&meta=Math scores"

# Run K-NN classification
curl -b cookies.txt -X POST "http://localhost:8080/encrypted-knn-starter-1.0.0/classify" -d "action=classify&features=88,83,89&encryptionKey=YOUR_BASE64_KEY"
```

### Development Workflow

```bash
# Watch for Java file changes during development
find src -name "*.java" | entr -r mvn compile

# Tail Tomcat logs for debugging
tail -f $TOMCAT_HOME/logs/catalina.out

# Quick redeploy cycle
mvn clean package && cp target/*.war $TOMCAT_HOME/webapps/
```

## Configuration Points

### Database Connection
Update these constants in both `StudentDAO.java` and `LoginServlet.java`:
```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/encrypted_knn";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "";
```

### Application Settings
- **K-NN k value**: Default 3, configurable via `/classify?action=config` endpoint
- **Session timeout**: 30 minutes (configurable in `web.xml`)
- **File upload limits**: 10MB max file size, 20MB max request (in `web.xml`)
- **AES key length**: 256 bits (configurable in `web.xml` context params)

## Key Development Patterns

### Adding New Encrypted Data Fields
1. Update `Student` model with new encrypted byte array field
2. Modify `StudentDAO` SQL statements to include new column
3. Update encryption/decryption logic in servlets
4. Add database migration script to `sql/schema.sql`

### Extending K-NN Algorithm
- Modify `EncryptedKNN.java` to add new distance metrics or classification methods
- Update `DataPoint` class structure for different feature types
- Consider memory optimization for large datasets (currently loads all data into memory)

### Adding New API Endpoints
1. Create servlet in `servlet` package
2. Add servlet mapping in `web.xml`
3. Implement session-based authentication checks
4. Follow existing JSON response patterns for consistency

## Security Considerations

- **Password Hashing**: Currently uses placeholder comparison - implement bcrypt for production
- **HTTPS Enforcement**: Uncomment security constraints in `web.xml` for HTTPS-only operation
- **Key Management**: Encryption keys should be managed externally, not passed in API calls for production
- **SQL Injection**: Uses PreparedStatements throughout - maintain this pattern
- **Session Management**: 30-minute timeout configured - adjust based on security requirements

## Testing Strategy

### Unit Testing Areas
- `AESUtil` encryption/decryption functions
- `EncryptedKNN` distance calculations and classification logic
- `StudentDAO` CRUD operations with test database

### Integration Testing
- End-to-end encrypted data flow (upload → classify → download)
- Session authentication across multiple requests
- Error handling for invalid encryption keys

### Data Format Requirements
Student data must follow CSV format with label at end:
```
feature1,feature2,feature3,...,label
85.5,90.2,78.8,GRADE_A
```
