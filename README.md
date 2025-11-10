# AIClass API - Student Performance Management System

A modern REST API for academic management and student performance analytics built with **Spring Boot 3.5**, **Supabase PostgreSQL**, and **JWT authentication**.

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

**Version**: 2.0.0 | **Phase**: Phase 3 - Maintainability & DevOps

---

## 🎉 What's New in v2.0

- **API Versioning**: All endpoints use `/api/v1/*` for semantic versioning
- **Monitoring**: Actuator endpoints with health checks and Prometheus metrics
- **Performance**: MapStruct mapping, optimized queries, strategic database indexes
- **Soft Delete**: Non-destructive deletion with automatic filtering
- **Enhanced Security**: Rate limiting and improved JWT validation

---

## 🚀 Quick Start

### Prerequisites
- Java 21 or higher
- Maven 3.6+ (or use included `./mvnw`)
- Supabase account with PostgreSQL database

### Step 1: Clone Repository
```bash
git clone https://github.com/ethx42/student-performance-api.git
cd student-performance-api
```

### Step 2: Configure Environment

#### Get Supabase Credentials
1. Go to [Supabase Dashboard](https://supabase.com/dashboard)
2. Navigate to **Project Settings** → **Database**:
   - Copy connection string (Session Pooler, port 5432)
3. Navigate to **Project Settings** → **API**:
   - Copy JWT Secret
   - Copy Project URL
   - Copy anon (public) key
   - Copy service_role key

#### Setup Configuration File
```bash
# Copy template
cp src/main/resources/application-local.properties.template \
   src/main/resources/application-local.properties

# Edit with your credentials
nano src/main/resources/application-local.properties
```

**Replace these values:**
```properties
spring.datasource.url=jdbc:postgresql://YOUR_HOST:5432/postgres?password=YOUR_PASSWORD
spring.datasource.username=postgres.YOUR_PROJECT_REF
spring.datasource.password=YOUR_PASSWORD

supabase.jwt.secret=YOUR_JWT_SECRET
supabase.url=https://YOUR_PROJECT_REF.supabase.co
supabase.anon.key=YOUR_ANON_KEY
supabase.service.role.key=YOUR_SERVICE_ROLE_KEY
```

**Note**: URL-encode special characters in password (spaces = `%20`)

### Step 3: Run Database Migrations (only if you are running  a local DB)
```bash
# Option A: Using script
chmod +x run-migrations.sh
./run-migrations.sh

# Option B: Manual with psql
psql "YOUR_CONNECTION_STRING" -f supabase/migrations/20250111000000_comprehensive_auth_and_rls.sql
psql "YOUR_CONNECTION_STRING" -f supabase/migrations/20250111000001_auto_create_user_profile.sql
psql "YOUR_CONNECTION_STRING" -f supabase/migrations/20250113010000_add_performance_indexes.sql
psql "YOUR_CONNECTION_STRING" -f supabase/migrations/20250115000000_add_soft_delete_support.sql
```

### Step 4: Build & Test
```bash
./mvnw clean install
./mvnw test

```

### Step 5: Start Application
```bash
./mvnw spring-boot:run -Dspring.profiles.active=local
```

### Step 6: Verify Setup
```bash
# Check health
curl http://localhost:8080/actuator/health
# Expected: {"status":"UP"}

# Access Swagger UI
open http://localhost:8080/swagger-ui.html
```

---

## 🔐 Authentication

### How It Works
1. User signs up via Supabase Auth → Creates user in `auth.users` + auto-creates in `public.users`
2. User logs in → Receives JWT token
3. API requests include `Authorization: Bearer <token>` header
4. Spring Security validates JWT and enforces role-based access

### Quick Test with Postman

**Import Collection**: `postman-collection/AIClass_API_v2_with_Auth.postman_collection.json`

#### 1. Signup
```bash
POST {{supabase_url}}/auth/v1/signup
Content-Type: application/json
apikey: {{supabase_anon_key}}

{
  "email": "teacher@university.edu",
  "password": "password123",
  "data": {
    "full_name": "Test Teacher",
    "role": "teacher"
  }
}
```

#### 2. Login
```bash
POST {{supabase_url}}/auth/v1/token?grant_type=password
Content-Type: application/json
apikey: {{supabase_anon_key}}

{
  "email": "teacher@university.edu",
  "password": "password123"
}
```

Copy the `access_token` from response.

#### 3. Use API
```bash
GET http://localhost:8080/api/v1/users
Authorization: Bearer YOUR_JWT_TOKEN
```

### Access Control

| Resource | List | View | Create | Update | Delete |
|----------|------|------|--------|--------|--------|
| **Users** | Teacher | Teacher | Any Auth | Own | Own |
| **Subjects** | Any Auth | Any Auth | Teacher | Teacher | Teacher |
| **Classes** | Auth + RLS | Auth + RLS | Teacher | Teacher | Teacher |
| **Enrollments** | Auth + RLS | Auth + RLS | Teacher | Teacher | Teacher |
| **Grades** | Auth + RLS | Auth + RLS | Teacher | Teacher | Teacher |
| **Recommendations** | Auth + RLS | Auth + RLS | Any Auth | - | Own |

- **Teacher**: Only teachers allowed
- **Any Auth**: Any authenticated user
- **Own**: Users can only access their own data
- **+RLS**: Row Level Security enforced at database level

---

## 📊 API Endpoints

**Base URL**: `http://localhost:8080/api/v1`

### Users
```
GET    /api/v1/users                    List users (paginated)
POST   /api/v1/users                    Create user
GET    /api/v1/users/{id}               Get user by ID
GET    /api/v1/users/auth/{authId}      Get user by Supabase auth ID
GET    /api/v1/users/email/{email}      Get user by email
PUT    /api/v1/users/{id}               Update user
DELETE /api/v1/users/{id}               Soft delete user
```

### Subjects
```
GET    /api/v1/subjects                 List subjects (paginated)
POST   /api/v1/subjects                 Create subject
GET    /api/v1/subjects/{id}            Get subject by ID
GET    /api/v1/subjects/code/{code}     Get subject by code
PUT    /api/v1/subjects/{id}            Update subject
DELETE /api/v1/subjects/{id}            Soft delete subject
```

### Classes
```
GET    /api/v1/classes                  List classes (filtered, paginated)
POST   /api/v1/classes                  Create class
GET    /api/v1/classes/{id}             Get class by ID
PUT    /api/v1/classes/{id}             Update class
DELETE /api/v1/classes/{id}             Soft delete class
```

### Enrollments
```
GET    /api/v1/enrollments              List enrollments (filtered)
POST   /api/v1/enrollments              Enroll student
GET    /api/v1/enrollments/{id}         Get enrollment by ID
PUT    /api/v1/enrollments/{id}         Update enrollment
DELETE /api/v1/enrollments/{id}         Soft delete enrollment
```

### Grades
```
GET    /api/v1/grades                   List grades (filtered)
POST   /api/v1/grades                   Create grade
GET    /api/v1/grades/{id}              Get grade by ID
PUT    /api/v1/grades/{id}              Update grade
DELETE /api/v1/grades/{id}              Soft delete grade
```

### AI Recommendations
```
GET    /api/v1/recommendations          List recommendations (filtered)
POST   /api/v1/recommendations          Create recommendation
GET    /api/v1/recommendations/{id}     Get recommendation by ID
DELETE /api/v1/recommendations/{id}     Soft delete recommendation
```

### Monitoring (Phase 3)
```
GET    /actuator/health                 Health status
GET    /actuator/metrics                Available metrics
GET    /actuator/prometheus             Prometheus metrics export
GET    /actuator/info                   Application info
```

**Interactive Docs**: http://localhost:8080/swagger-ui.html

---

## ✨ Key Features

- **JWT Authentication** - Supabase Auth with HS256 validation
- **Role-Based Access** - Teacher and Student roles
- **Row Level Security** - Database-level data isolation
- **API Versioning** - `/api/v1/*` with deprecation strategy
- **Soft Delete** - Non-destructive deletion
- **Monitoring** - Health checks, metrics, Prometheus
- **Clean Architecture** - Controller → Service → Repository
- **Interactive Docs** - Swagger UI with auth support

---

## 🛠️ Technology Stack

- Java 21
- Spring Boot 3.5.5
- Spring Security (JWT)
- Spring Data JPA
- PostgreSQL (Supabase)
- MapStruct
- Lombok
- SpringDoc OpenAPI
- Maven
- JUnit 5 + Mockito

---

## 🗄️ Database Schema

### Tables
- **users** - Teachers and students
- **subjects** - Academic subjects
- **classes** - Class sections
- **enrollments** - Student enrollments
- **grades** - Student grades
- **ai_recommendations** - AI-generated recommendations

**Features**: UUID keys, audit timestamps (`created_at`, `updated_at`), soft delete (`deleted_at`), JSONB metadata, Row Level Security

---

## 📦 Building

### Development
```bash
./mvnw spring-boot:run -Dspring.profiles.active=local
```

### Testing
```bash
# Run all tests
./mvnw test

# Run tests with coverage report
./mvnw clean test jacoco:report

# View coverage report (after running above command)
open target/site/jacoco/index.html

# Run full build with tests and coverage checks
./mvnw clean install
# Note: Build will fail if coverage is below 50% threshold
```

### Production
```bash
./mvnw clean package -DskipTests
java -jar target/aiclass-0.0.1-SNAPSHOT.jar
```

---

## 🚀 Deployment

### Environment Variables
```bash
# Database
DB_URL=jdbc:postgresql://host:port/database
DB_USERNAME=postgres.xxx
DB_PASSWORD=your-password

# Supabase Auth
SUPABASE_JWT_SECRET=your-jwt-secret
SUPABASE_URL=https://xxx.supabase.co
SUPABASE_ANON_KEY=your-anon-key
SUPABASE_SERVICE_ROLE_KEY=your-service-role-key

# Security
ALLOWED_ORIGINS=https://your-frontend.com
```

### Production Checklist
- [ ] Use HTTPS/TLS
- [ ] Set `spring.jpa.hibernate.ddl-auto=validate`
- [ ] Disable SQL logging
- [ ] Configure CORS for production
- [ ] Use production Supabase keys
- [ ] Set up monitoring dashboards
- [ ] Enable database backups
- [ ] Configure rate limiting
- [ ] Set up log aggregation

---

## 🔧 Troubleshooting

### Database connection fails
- Verify connection string (use Session Pooler, port 5432)
- URL-encode special chars in password
- Check Supabase project is active

### JWT validation fails
- Ensure `supabase.jwt.secret` matches your Supabase project
- Get fresh token from Supabase Auth
- Check token format: `Authorization: Bearer <token>`

### 404 on `/api/*` endpoints
- Update to `/api/v1/*` - old paths removed in v2.0
- Import latest Postman collection

### Tests failing
- Run `./mvnw clean install` first
- Ensure H2 database is available

### Can't access Swagger UI
- App should run on port 8080
- Visit: http://localhost:8080/swagger-ui.html
- Check logs for startup errors

---

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Follow code conventions
4. Write unit tests
5. Update API documentation
6. Commit changes (`git commit -m 'Add amazing feature'`)
7. Push to branch (`git push origin feature/amazing-feature`)
8. Open Pull Request

---

## 📄 License

MIT License - see [LICENSE](LICENSE) file for details.

---

## 📈 Roadmap

- [x] Phase 1: Core functionality
- [x] Phase 2: Authentication & security
- [x] Phase 3: Maintainability & DevOps
- [ ] Phase 4: Advanced analytics
- [ ] Phase 5: Real-time features
- [ ] Phase 6: ML integration

---

**Version**: 2.0.0  
**Last Updated**: October 2025  
**Status**: ✅ Production Ready

**Made with ❤️ by the AIClass Team**
