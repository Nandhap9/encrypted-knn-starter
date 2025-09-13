# Encrypted-KNN Starter Project

> Java Web Application scaffold: AES-CBC encryption for student data + K-NN classification module.

---

## What you'll find in this starter project

* Maven `pom.xml` to build a WAR deployable to Tomcat 8/9
* Java utility for AES (CBC) encryption/decryption with random IV and secure key generation
* Simple JDBC-based DAO to store encrypted student records in MySQL
* Servlets for Admin upload, Student download (with secure session-based decryption), and K-NN classification
* SQL script to create tables
* `web.xml` for servlet mapping
* Complete documentation with usage examples

---

## Project Structure

```
encrypted-knn-starter/
├─ pom.xml
├─ README.md
├─ src/main/java/com/example/encryptedknn/
│  ├─ util/AESUtil.java
│  ├─ model/Student.java
│  ├─ dao/StudentDAO.java
│  ├─ knn/EncryptedKNN.java
│  ├─ servlet/LoginServlet.java
│  ├─ servlet/UploadServlet.java
│  ├─ servlet/DownloadServlet.java
│  └─ servlet/ClassifyServlet.java
├─ src/main/webapp/WEB-INF/web.xml
└─ sql/schema.sql
```

---

## Prerequisites

* **Java 8+** (JDK)
* **Maven 3.6+**
* **MySQL 5.7+** or **MySQL 8.0**
* **Apache Tomcat 8.5+** or **Tomcat 9.0**

---

## Setup Instructions

### 1. Database Setup

1. Start your MySQL server
2. Create the database and tables:

```bash
mysql -u root -p < sql/schema.sql
```

3. Update database credentials in:
   - `src/main/java/com/example/encryptedknn/dao/StudentDAO.java`
   - `src/main/java/com/example/encryptedknn/servlet/LoginServlet.java`

Default credentials in the code:
```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/encrypted_knn";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "";
```

### 2. Build the Project

```bash
# Compile and package
mvn clean package

# This creates target/encrypted-knn-starter-1.0.0.war
```

### 3. Deploy to Tomcat

**Option A: Copy WAR to Tomcat**
```bash
cp target/encrypted-knn-starter-1.0.0.war $TOMCAT_HOME/webapps/
```

**Option B: Use Maven Tomcat Plugin**
Add to your `pom.xml`:
```xml
<plugin>
    <groupId>org.apache.tomcat.maven</groupId>
    <artifactId>tomcat7-maven-plugin</artifactId>
    <version>2.2</version>
    <configuration>
        <url>http://localhost:8080/manager/text</url>
        <server>tomcat</server>
        <path>/encrypted-knn-starter</path>
    </configuration>
</plugin>
```

Deploy:
```bash
mvn tomcat7:deploy
```

### 4. Access the Application

Base URL: `http://localhost:8080/encrypted-knn-starter-1.0.0/`

---

## API Documentation

### Authentication Endpoints

#### POST /login
Authenticate user and create session.

**Request:**
```bash
curl -X POST "http://localhost:8080/encrypted-knn-starter-1.0.0/login" \
  -d "username=admin&password=admin_hashed_placeholder"
```

**Response:**
```json
{
  "success": true,
  "role": "ADMIN"
}
```

#### GET /login
Check current session status.

**Response:**
```json
{
  "authenticated": true,
  "username": "admin",
  "role": "ADMIN"
}
```

#### DELETE /login
Logout and destroy session.

**Response:**
```json
{
  "success": true,
  "message": "Logged out successfully"
}
```

---

### Data Management Endpoints

#### POST /upload
Upload encrypted student data (Admin only).

**Request:**
```bash
curl -X POST "http://localhost:8080/encrypted-knn-starter-1.0.0/upload" \
  -d "studentId=STU001" \
  -d "studentName=John Doe" \
  -d "studentData=85.5,90.2,78.8,GRADE_A" \
  -d "encryptionKey=BASE64_ENCODED_AES_KEY" \
  -d "meta=Math scores"
```

**Response:**
```json
{
  "success": true,
  "message": "Student data uploaded successfully"
}
```

#### PUT /upload
Update existing student data (Admin only).

**Request:** Same as POST /upload

**Response:**
```json
{
  "success": true,
  "message": "Student data updated successfully"
}
```

#### GET /download?action=list
List all students (basic info only).

**Response:**
```json
{
  "success": true,
  "students": [
    {
      "studentId": "STU001",
      "meta": "Math scores",
      "createdAt": "2025-01-15 10:30:45"
    }
  ]
}
```

#### GET /download?studentId=STU001&encryptionKey=KEY
Download and decrypt specific student data.

**Response:**
```json
{
  "success": true,
  "studentId": "STU001",
  "studentName": "John Doe",
  "studentData": "85.5,90.2,78.8,GRADE_A",
  "meta": "Math scores",
  "createdAt": "2025-01-15 10:30:45"
}
```

#### POST /download?action=decrypt
Bulk decrypt all students (Admin only).

**Request:**
```bash
curl -X POST "http://localhost:8080/encrypted-knn-starter-1.0.0/download" \
  -d "action=decrypt" \
  -d "encryptionKey=BASE64_ENCODED_AES_KEY"
```

**Response:**
```json
{
  "success": true,
  "students": [
    {
      "studentId": "STU001",
      "studentName": "John Doe",
      "studentData": "85.5,90.2,78.8,GRADE_A",
      "meta": "Math scores",
      "createdAt": "2025-01-15 10:30:45"
    }
  ]
}
```

---

### Classification Endpoints

#### GET /classify
Get current K-NN classifier configuration.

**Response:**
```json
{
  "success": true,
  "k": 3,
  "message": "KNN Classifier ready"
}
```

#### POST /classify?action=classify
Classify new data point using K-NN.

**Request:**
```bash
curl -X POST "http://localhost:8080/encrypted-knn-starter-1.0.0/classify" \
  -d "action=classify" \
  -d "features=87.0,89.5,82.3" \
  -d "encryptionKey=BASE64_ENCODED_AES_KEY"
```

**Response:**
```json
{
  "success": true,
  "predictedLabel": "GRADE_A",
  "features": [87.0, 89.5, 82.3],
  "k": 3
}
```

#### POST /classify?action=neighbors
Get K nearest neighbors for a data point.

**Request:**
```bash
curl -X POST "http://localhost:8080/encrypted-knn-starter-1.0.0/classify" \
  -d "action=neighbors" \
  -d "features=87.0,89.5,82.3" \
  -d "encryptionKey=BASE64_ENCODED_AES_KEY"
```

**Response:**
```json
{
  "success": true,
  "queryFeatures": [87.0, 89.5, 82.3],
  "k": 3,
  "neighbors": [
    {
      "studentId": "STU001",
      "label": "GRADE_A",
      "distance": 2.34,
      "features": [85.5, 90.2, 78.8]
    }
  ]
}
```

#### POST /classify?action=config
Update K-NN configuration (Admin only).

**Request:**
```bash
curl -X POST "http://localhost:8080/encrypted-knn-starter-1.0.0/classify" \
  -d "action=config" \
  -d "k=5"
```

**Response:**
```json
{
  "success": true,
  "message": "k value updated",
  "k": 5
}
```

---

## Usage Examples

### 1. Generate AES Key

```java
import com.example.encryptedknn.util.AESUtil;
import javax.crypto.SecretKey;

// Generate a 256-bit AES key
SecretKey key = AESUtil.generateKey(256);
String keyString = AESUtil.keyToString(key);
System.out.println("Base64 Key: " + keyString);
```

### 2. Complete Workflow Example

```bash
# 1. Login as admin
curl -c cookies.txt -X POST "http://localhost:8080/encrypted-knn-starter-1.0.0/login" \
  -d "username=admin&password=admin_hashed_placeholder"

# 2. Upload training data
curl -b cookies.txt -X POST "http://localhost:8080/encrypted-knn-starter-1.0.0/upload" \
  -d "studentId=STU001" \
  -d "studentName=Alice" \
  -d "studentData=90,85,92,GRADE_A" \
  -d "encryptionKey=YOUR_BASE64_KEY" \
  -d "meta=Math scores"

curl -b cookies.txt -X POST "http://localhost:8080/encrypted-knn-starter-1.0.0/upload" \
  -d "studentId=STU002" \
  -d "studentName=Bob" \
  -d "studentData=70,75,68,GRADE_B" \
  -d "encryptionKey=YOUR_BASE64_KEY" \
  -d "meta=Math scores"

# 3. Classify new data point
curl -b cookies.txt -X POST "http://localhost:8080/encrypted-knn-starter-1.0.0/classify" \
  -d "action=classify" \
  -d "features=88,83,89" \
  -d "encryptionKey=YOUR_BASE64_KEY"

# 4. Get nearest neighbors
curl -b cookies.txt -X POST "http://localhost:8080/encrypted-knn-starter-1.0.0/classify" \
  -d "action=neighbors" \
  -d "features=88,83,89" \
  -d "encryptionKey=YOUR_BASE64_KEY"

# 5. Logout
curl -b cookies.txt -X DELETE "http://localhost:8080/encrypted-knn-starter-1.0.0/login"
```

---

## Data Format

### Student Data Format
Student data should be stored as comma-separated values with the label at the end:
```
feature1,feature2,feature3,...,label
```

**Example:**
```
85.5,90.2,78.8,GRADE_A
```

This represents a student with:
- Feature 1: 85.5
- Feature 2: 90.2  
- Feature 3: 78.8
- Classification: GRADE_A

---

## Security Features

1. **AES-CBC Encryption**: All student data is encrypted using AES-256-CBC with random IVs
2. **Session-based Authentication**: Server-side session management
3. **Role-based Access Control**: Admin and Student roles with different permissions
4. **Secure Key Management**: Keys are never logged or exposed in responses
5. **HTTPS Ready**: Can be configured for HTTPS enforcement via web.xml

---

## Configuration

### Database Configuration
Update connection details in:
- `StudentDAO.java` (lines 10-12)
- `LoginServlet.java` (lines 17-19)

### Default Settings
- K-NN k value: 3 (configurable via API)
- Session timeout: 30 minutes
- AES key length: 256 bits
- Max upload size: 10MB

---

## Troubleshooting

### Common Issues

**1. MySQL Connection Issues**
- Check MySQL service is running
- Verify database credentials
- Ensure database `encrypted_knn` exists

**2. Tomcat Deployment Issues**
- Check Tomcat logs in `logs/catalina.out`
- Verify WAR file is not corrupted
- Ensure sufficient disk space

**3. ClassNotFoundException**
- Ensure MySQL connector JAR is included
- Check Maven dependencies are resolved

**4. Encryption/Decryption Errors**
- Verify the encryption key is valid Base64
- Ensure the same key is used for encrypt/decrypt operations
- Check that IV is properly stored and retrieved

### Logs Location
- Application logs: Tomcat console output
- Access logs: `$TOMCAT_HOME/logs/`
- Database errors: Check MySQL error log

---

## Development Notes

### Extending the Project

1. **Add New Features**: Create new servlets in the `servlet` package
2. **Database Changes**: Update `schema.sql` and DAO classes
3. **New Algorithms**: Extend the `knn` package
4. **Security**: Implement proper password hashing (bcrypt recommended)

### Testing
- Use Postman or curl for API testing
- Create unit tests with JUnit
- Test with different data sizes and encryption keys

---

## License

This is a starter project template. Customize as needed for your specific use case.

---

## Support

For questions or issues:
1. Check the troubleshooting section above
2. Review server logs for error details
3. Verify all prerequisites are met
4. Test with simple data first before complex scenarios
