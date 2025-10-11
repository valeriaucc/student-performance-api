# Quick Start Guide - AIClass API v2.0

## 🚀 Get Started in 5 Minutes

### Prerequisites
- Java 21 installed
- PostgreSQL/Supabase database access
- Maven (included via wrapper)

### 1. Clone & Setup (1 min)

```bash
# Clone the repository
git clone <repository-url>
cd student-performance-api

# Make scripts executable
chmod +x mvnw setup-env.sh
```

### 2. Configure Environment (1 min)

```bash
# Copy template and edit
cp setup-env.sh.template setup-env.sh

# Edit setup-env.sh with your database credentials
nano setup-env.sh

# Load environment variables
source setup-env.sh
```

**Or set them manually:**

```bash
export DB_URL="jdbc:postgresql://your-db-url:5432/postgres"
export DB_USERNAME="postgres"
export DB_PASSWORD="your-password"
```

### 3. Build & Run (3 min)

```bash
# Build the project
./mvnw clean install -DskipTests

# Run the application
./mvnw spring-boot:run
```

That's it! 🎉 The API is running at http://localhost:8080

### 4. Test the API

Open Swagger UI in your browser:
```
http://localhost:8080/swagger-ui.html
```

Or test with curl:

```bash
# Health check
curl http://localhost:8080/actuator/health

# Get all subjects
curl http://localhost:8080/api/subjects

# Create a user
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "authUserId": "550e8400-e29b-41d4-a716-446655440000",
    "fullName": "Test User",
    "email": "test@example.com",
    "role": "TEACHER"
  }'
```

---

## 📖 Common Commands

### Development

```bash
# Run in development mode
./mvnw spring-boot:run

# Run with specific profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# Build without tests
./mvnw clean package -DskipTests

# Run tests only
./mvnw test
```

### Production

```bash
# Build production JAR
./mvnw clean package

# Run production JAR
java -jar target/aiclass-0.0.1-SNAPSHOT.jar
```

---

## 🔑 Key Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/swagger-ui.html` | GET | API Documentation |
| `/api/users` | GET | List all users |
| `/api/subjects` | GET | List all subjects |
| `/api/classes` | GET | List all classes |
| `/api/enrollments` | GET | List enrollments |
| `/api/grades` | GET | List grades |
| `/api/recommendations` | GET | List recommendations |

---

## 🐛 Troubleshooting

### Build Fails

```bash
# Clean Maven cache
./mvnw clean

# Force update dependencies
./mvnw clean install -U
```

### Database Connection Error

```bash
# Check environment variables
echo $DB_URL
echo $DB_USERNAME

# Test database connection
psql -h your-host -U your-username -d your-database
```

### Port Already in Use

```bash
# Change port in application.properties
echo "server.port=8081" >> src/main/resources/application.properties

# Or set environment variable
export SERVER_PORT=8081
```

---

## 📚 Next Steps

1. **Read the docs**: Check `README_v2.md` for full documentation
2. **Explore API**: Open Swagger UI and try the endpoints
3. **Review schema**: See `REFACTORING_PLAN.md` for database details
4. **Migration**: If upgrading from v1, read `API_MIGRATION_GUIDE.md`

---

## 💡 Tips

- Use Swagger UI for interactive API testing
- All IDs are UUIDs (not integers)
- Responses are wrapped in `ApiResponse<T>`
- Check error responses for detailed validation messages
- Enable SQL logging with `export SQL_LOG_LEVEL=DEBUG`

---

## 🆘 Need Help?

- **Swagger Docs**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/v3/api-docs
- **Full README**: README_v2.md
- **Migration Guide**: API_MIGRATION_GUIDE.md

Happy coding! 🚀

