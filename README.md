# AIClass API - Student Performance Management System

A modern, enterprise-grade REST API for academic management and student performance analytics, built with Spring Boot 3.5 and PostgreSQL/Supabase.

## 🚀 Project Overview

AIClass API is a comprehensive backend system for managing educational data, tracking student performance, and providing AI-powered recommendations. The application follows clean architecture principles and modern best practices for maintainable, scalable code.

## ✨ Key Features

- **User Management** - Teachers and students with Supabase Auth integration
- **Subject Management** - Academic subjects/courses with code-based lookup
- **Class Management** - Class sections with teacher assignments and scheduling
- **Enrollment System** - Student enrollment with status tracking (active, dropped, completed)
- **Grade Management** - Student grades with automatic percentage calculation
- **AI Recommendations** - AI-generated recommendations for teachers and students
- **Clean Architecture** - Proper separation of concerns (Controller → Service → Repository)
- **UUID Primary Keys** - Modern, globally unique identifiers
- **JSONB Metadata** - Flexible metadata storage for extensibility
- **Comprehensive API Documentation** - Interactive Swagger/OpenAPI docs
- **Global Exception Handling** - Consistent error responses
- **Input Validation** - Bean Validation with detailed error messages

## 🛠️ Technology Stack

- **Java 21** - Latest LTS version
- **Spring Boot 3.5.5** - Modern Spring framework
- **Spring Data JPA** - Data persistence layer
- **Spring Web** - REST API endpoints
- **PostgreSQL** - Primary database (Supabase)
- **Lombok** - Code generation for boilerplate reduction
- **MapStruct** - DTO mapping
- **Hypersistence Utils** - JSONB support
- **SpringDoc OpenAPI** - API documentation
- **Maven** - Build and dependency management
- **Spring Boot DevTools** - Development productivity

## 📊 Data Model

### Core Entities

#### 👤 User
- **Table:** `users`
- **Primary Key:** UUID
- **Key Fields:** auth_user_id, full_name, email, role, metadata (JSONB)
- **Roles:** TEACHER, STUDENT
- **Relationships:** OneToMany with Class (as teacher), OneToMany with Enrollment, OneToMany with Grade, OneToMany with AiRecommendation
- **Features:** Supabase Auth integration, flexible metadata storage

#### 📚 Subject
- **Table:** `subjects`
- **Primary Key:** UUID
- **Key Fields:** code (unique), name, description, credits, metadata (JSONB)
- **Relationships:** OneToMany with Class
- **Features:** Unique subject codes for easy lookup

#### 🏫 Class
- **Table:** `classes`
- **Primary Key:** UUID
- **Key Fields:** group, year, semester, schedule, metadata (JSONB)
- **Relationships:** ManyToOne with Subject, ManyToOne with User (teacher), OneToMany with Enrollment, OneToMany with Grade, OneToMany with AiRecommendation
- **Features:** Scheduling support, semester-based organization

#### 👨‍🎓 Enrollment
- **Table:** `enrollments`
- **Primary Key:** UUID
- **Key Fields:** enrollment_status, enrolled_at
- **Status:** ACTIVE, DROPPED, COMPLETED
- **Relationships:** ManyToOne with Class, ManyToOne with User (student)
- **Features:** Enrollment date tracking, status management

#### 📝 Grade
- **Table:** `grades`
- **Primary Key:** UUID
- **Key Fields:** assessment_kind, assessment_name, score, max_score, percentage, graded_at
- **Relationships:** ManyToOne with User (student), ManyToOne with Class
- **Features:** Automatic percentage calculation, flexible assessment types

#### 🤖 AI Recommendation
- **Table:** `ai_recommendations`
- **Primary Key:** UUID
- **Key Fields:** message, audience, generated_at
- **Audience:** STUDENT, TEACHER
- **Relationships:** ManyToOne with User (recipient), ManyToOne with Class
- **Features:** Targeted recommendations, timestamp tracking

## 🔗 API Endpoints

### Base URL
```
http://localhost:8080/api
```

All responses follow the `ApiResponse<T>` wrapper pattern for consistency:

### 👤 User Management (`/api/users`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/users` | Get all users (filter by role) |
| `GET` | `/api/users/{id}` | Get user by ID (UUID) |
| `GET` | `/api/users/auth/{authUserId}` | Get user by Supabase auth ID |
| `GET` | `/api/users/email/{email}` | Get user by email |
| `POST` | `/api/users` | Create new user |
| `PUT` | `/api/users/{id}` | Update user |
| `DELETE` | `/api/users/{id}` | Delete user |

**Create User Request:**
```json
{
  "authUserId": "550e8400-e29b-41d4-a716-446655440000",
  "fullName": "Dr. Ana Ruiz",
  "email": "ana.ruiz@university.edu",
  "role": "TEACHER",
  "metadata": {
    "department": "Mathematics",
    "office": "Building A, Room 305"
  }
}
```

**Response:**
```json
{
  "success": true,
  "message": "User created successfully",
  "data": {
    "id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
    "authUserId": "550e8400-e29b-41d4-a716-446655440000",
    "fullName": "Dr. Ana Ruiz",
    "email": "ana.ruiz@university.edu",
    "role": "TEACHER",
    "metadata": {
      "department": "Mathematics",
      "office": "Building A, Room 305"
    },
    "createdAt": "2025-10-11T10:00:00Z",
    "updatedAt": "2025-10-11T10:00:00Z"
  },
  "timestamp": "2025-10-11T10:00:00Z"
}
```

### 📚 Subject Management (`/api/subjects`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/subjects` | Get all subjects |
| `GET` | `/api/subjects/{id}` | Get subject by ID (UUID) |
| `GET` | `/api/subjects/code/{code}` | Get subject by code |
| `POST` | `/api/subjects` | Create new subject |
| `PUT` | `/api/subjects/{id}` | Update subject |
| `DELETE` | `/api/subjects/{id}` | Delete subject |

**Subject Example:**
```json
{
  "code": "MAT101",
  "name": "Calculus I",
  "description": "Introduction to differential calculus",
  "credits": 4,
  "metadata": {
    "prerequisites": ["ALG101"],
    "level": "undergraduate"
  }
}
```

### 🏫 Class Management (`/api/classes`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/classes` | Get all classes (filter by teacher, subject, year, semester) |
| `GET` | `/api/classes/{id}` | Get class by ID (UUID) |
| `POST` | `/api/classes` | Create new class |
| `PUT` | `/api/classes/{id}` | Update class |
| `DELETE` | `/api/classes/{id}` | Delete class |

**Class Example:**
```json
{
  "subjectId": "a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d",
  "teacherId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "group": "A",
  "year": 2025,
  "semester": "FIRST",
  "schedule": "Mon/Wed/Fri 10:00-11:30",
  "metadata": {
    "room": "Building C, Room 201",
    "capacity": 30
  }
}
```

### 👨‍🎓 Enrollment Management (`/api/enrollments`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/enrollments` | Get enrollments (filter by class, student, status) |
| `GET` | `/api/enrollments/{id}` | Get enrollment by ID (UUID) |
| `POST` | `/api/enrollments` | Enroll student in class |
| `PATCH` | `/api/enrollments/{id}` | Update enrollment status |
| `DELETE` | `/api/enrollments/{id}` | Delete enrollment |

**Enrollment Example:**
```json
{
  "classId": "a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d",
  "studentId": "f6e5d4c3-b2a1-4c5d-9e8f-7a6b5c4d3e2f",
  "enrollmentStatus": "ACTIVE"
}
```

### 📝 Grade Management (`/api/grades`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/grades` | Get grades (filter by class, student) |
| `GET` | `/api/grades/{id}` | Get grade by ID (UUID) |
| `POST` | `/api/grades` | Create new grade |
| `PUT` | `/api/grades/{id}` | Update grade |
| `DELETE` | `/api/grades/{id}` | Delete grade |

**Grade Request:**
```json
{
  "classId": "a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d",
  "studentId": "f6e5d4c3-b2a1-4c5d-9e8f-7a6b5c4d3e2f",
  "assessmentKind": "exam",
  "assessmentName": "Midterm Exam",
  "score": 85.5,
  "maxScore": 100,
  "gradedAt": "2025-10-10T14:30:00Z"
}
```

**Grade Response:**
```json
{
  "success": true,
  "message": "Grade created successfully",
  "data": {
    "id": "b2c3d4e5-f6a7-4b5c-9d8e-1f2a3b4c5d6e",
    "classId": "a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d",
    "className": "Calculus I - Group A",
    "studentId": "f6e5d4c3-b2a1-4c5d-9e8f-7a6b5c4d3e2f",
    "studentName": "Carlos Gomez",
    "assessmentKind": "exam",
    "assessmentName": "Midterm Exam",
    "score": 85.5,
    "maxScore": 100,
    "percentage": 85.50,
    "gradedAt": "2025-10-10T14:30:00Z",
    "createdAt": "2025-10-11T10:00:00Z",
    "updatedAt": "2025-10-11T10:00:00Z"
  },
  "timestamp": "2025-10-11T10:00:00Z"
}
```

### 🤖 AI Recommendations (`/api/recommendations`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/recommendations` | Get recommendations (filter by recipient, class, audience) |
| `GET` | `/api/recommendations/{id}` | Get recommendation by ID (UUID) |
| `POST` | `/api/recommendations` | Create new recommendation |
| `DELETE` | `/api/recommendations/{id}` | Delete recommendation |

**Recommendation Example:**
```json
{
  "recipientId": "f6e5d4c3-b2a1-4c5d-9e8f-7a6b5c4d3e2f",
  "classId": "a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d",
  "message": "Your performance in Calculus is below average. Focus on derivatives and limits concepts.",
  "audience": "STUDENT"
}
```

### 🔄 Error Response Format

**Validation Error:**
```json
{
  "message": "Validation failed",
  "status": 400,
  "error": "Bad Request",
  "path": "/api/users",
  "details": [
    "fullName: Full name is required",
    "email: Email must be valid"
  ],
  "timestamp": "2025-10-11T10:00:00Z"
}
```

**Not Found Error:**
```json
{
  "message": "User not found with id: '550e8400-e29b-41d4-a716-446655440000'",
  "status": 404,
  "error": "Not Found",
  "path": "/api/users/550e8400-e29b-41d4-a716-446655440000",
  "details": [],
  "timestamp": "2025-10-11T10:00:00Z"
}
```

## 📚 API Documentation

Once the application is running, access the comprehensive interactive API documentation:

### Swagger UI (Interactive)
```
http://localhost:8080/swagger-ui.html
```

### OpenAPI Specification (JSON)
```
http://localhost:8080/v3/api-docs
```

### Documentation Features
- ✅ **Interactive Interface**: Test endpoints directly from your browser
- ✅ **Complete Documentation**: Detailed description of every endpoint
- ✅ **Request/Response Examples**: Sample data and status codes
- ✅ **Documented Parameters**: Full description of all input parameters
- ✅ **Data Models**: Schemas for all entities and DTOs
- ✅ **Authentication Ready**: Support for Supabase Auth integration
- ✅ **Error Response Examples**: Validation and error handling examples

## ⚙️ Installation & Setup

### 1. Prerequisites
- **Java 21** installed (OpenJDK recommended)
- **Maven 3.6+** (or use included Maven wrapper `./mvnw`)
- **PostgreSQL 14+** (Supabase account configured)
- **IDE** (IntelliJ IDEA, Eclipse, or VS Code recommended)

### 2. Clone Repository
```bash
git clone <your-repository-url>
cd student-performance-api
```

### 3. Environment Variables Setup

This project uses environment variables for configuration following Spring Boot best practices.

#### Step 1: Copy and Configure Environment File

```bash
# Copy the template
cp setup-env.sh.template setup-env.sh

# Edit with your actual credentials
nano setup-env.sh  # or use your preferred editor
```

#### Step 2: Update Your Supabase Credentials

Edit `setup-env.sh` and replace the placeholder values:

```bash
# Database Configuration - Get these from Supabase Dashboard
export DB_URL="jdbc:postgresql://your-pooler-host.pooler.supabase.com:6543/postgres"
export DB_USERNAME="postgres.your-project-ref"
export DB_PASSWORD="your-secure-password"
export DB_DRIVER="org.postgresql.Driver"

# Hibernate Configuration
export HIBERNATE_DDL_AUTO="validate"  # use 'update' for dev, 'validate' for prod
export HIBERNATE_SHOW_SQL="true"
export HIBERNATE_FORMAT_SQL="true"
export HIBERNATE_DIALECT="org.hibernate.dialect.PostgreSQLDialect"

# Server Configuration
export SERVER_PORT="8080"

# Application Configuration
export APP_NAME="aiclass"
```

**⚠️ Security Note:** The `setup-env.sh` file is ignored by Git. Never commit sensitive credentials to version control.

### 4. Running the Application

#### Option 1: One-Line Command (Recommended)
```bash
source setup-env.sh && ./mvnw spring-boot:run
```

#### Option 2: Separate Commands
```bash
# Load environment variables
source setup-env.sh

# Start the application
./mvnw spring-boot:run
```

#### Successful Startup
When running successfully, you'll see:
```
...
INFO --- Tomcat started on port 8080 (http) with context path '/'
INFO --- Started AiclassApplication in X.XXX seconds
```

The application will be available at:
- **API Base:** `http://localhost:8080/api`
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI Docs:** `http://localhost:8080/v3/api-docs`

### 5. Verify Installation

Test the API is running:
```bash
# Should return API information
curl http://localhost:8080/api/users
```

## 🗄️ Database

### Configuration
- **Provider:** Supabase PostgreSQL
- **Version:** PostgreSQL 17.6
- **Connection:** Pooler connection for better performance
- **DDL Mode:** 
  - Development: `update` or `validate`
  - Production: `validate` or `none` (recommended)
- **Dialect:** PostgreSQL

### Database Schema
The application uses the following tables with UUID primary keys:

| Table | Purpose | Key Features |
|-------|---------|--------------|
| `users` | Teachers and students | Supabase Auth integration, JSONB metadata |
| `subjects` | Academic subjects | Unique subject codes, credits tracking |
| `classes` | Class sections | Teacher assignment, scheduling, semester-based |
| `enrollments` | Student enrollments | Status tracking (ACTIVE/DROPPED/COMPLETED) |
| `grades` | Student assessments | Automatic percentage calculation |
| `ai_recommendations` | AI suggestions | Targeted by audience (STUDENT/TEACHER) |

### Row Level Security (RLS)
For production deployment with Supabase:
- Enable RLS on all tables
- Configure policies based on Supabase Auth user roles
- Use service role key for backend operations
- See `scripts/add_auth_fields_and_rls.sql` for RLS setup

### Database Migrations
Database schema changes are managed through:
- Supabase migrations in `supabase/migrations/`
- Hibernate DDL for development (set to `update` or `validate`)
- Manual SQL scripts in `scripts/` directory

## 🧪 Testing

### Run All Tests
```bash
./mvnw test
```

### Run Specific Test
```bash
./mvnw test -Dtest=DatabaseConnectionTest
```

### Test with Coverage
```bash
./mvnw clean test jacoco:report
```

### Manual API Testing

**Using cURL:**
```bash
# Get all users
curl http://localhost:8080/api/users

# Create a user
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Test User",
    "email": "test@example.com",
    "role": "STUDENT"
  }'
```

**Using the Swagger UI:**
Navigate to `http://localhost:8080/swagger-ui.html` for interactive testing.

## 📦 Building for Production

### Build JAR Package
```bash
./mvnw clean package -DskipTests
```

### Run Production JAR
```bash
# Make sure environment variables are set
export DB_URL="jdbc:postgresql://your-production-db:5432/postgres"
export DB_USERNAME="your-username"
export DB_PASSWORD="your-password"
export HIBERNATE_DDL_AUTO="validate"
export HIBERNATE_SHOW_SQL="false"
export SERVER_PORT="8080"

# Run the JAR
java -jar target/aiclass-0.0.1-SNAPSHOT.jar
```

### Docker Deployment (Optional)

**Dockerfile:**
```dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/aiclass-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Build and Run:**
```bash
docker build -t aiclass-api .
docker run -p 8080:8080 \
  -e DB_URL="jdbc:postgresql://..." \
  -e DB_USERNAME="..." \
  -e DB_PASSWORD="..." \
  aiclass-api
```

## 🏗️ Project Structure

The project follows clean architecture principles with clear separation of concerns:

```
src/main/java/com/viveek/aiclass/
├── AiclassApplication.java              # Main application entry point
│
├── api/                                 # API Layer
│   └── controller/                      # REST Controllers
│       ├── UserController.java          # User management endpoints
│       ├── SubjectController.java       # Subject management endpoints
│       ├── ClassController.java         # Class management endpoints
│       ├── EnrollmentController.java    # Enrollment management endpoints
│       ├── GradeController.java         # Grade management endpoints
│       └── RecommendationController.java # AI recommendations endpoints
│
├── domain/                              # Domain Layer
│   ├── model/                           # JPA Entities (UUID primary keys)
│   │   ├── BaseEntity.java              # Base entity with timestamps
│   │   ├── User.java                    # User entity (teachers/students)
│   │   ├── Subject.java                 # Subject entity
│   │   ├── Class.java                   # Class entity
│   │   ├── Enrollment.java              # Enrollment entity
│   │   ├── Grade.java                   # Grade entity
│   │   ├── AiRecommendation.java        # AI recommendation entity
│   │   └── enums/                       # Enumerations
│   │       ├── UserRole.java            # TEACHER, STUDENT
│   │       ├── Semester.java            # FIRST, SECOND, SUMMER
│   │       ├── EnrollmentStatus.java    # ACTIVE, DROPPED, COMPLETED
│   │       └── RecommendationAudience.java # STUDENT, TEACHER
│   │
│   └── repository/                      # JPA Repositories
│       ├── UserRepository.java
│       ├── SubjectRepository.java
│       ├── ClassRepository.java
│       ├── EnrollmentRepository.java
│       ├── GradeRepository.java
│       └── AiRecommendationRepository.java
│
├── dto/                                 # Data Transfer Objects
│   ├── request/                         # Request DTOs
│   │   ├── CreateUserRequest.java
│   │   ├── UpdateUserRequest.java
│   │   ├── CreateSubjectRequest.java
│   │   ├── UpdateSubjectRequest.java
│   │   ├── CreateClassRequest.java
│   │   ├── UpdateClassRequest.java
│   │   ├── CreateEnrollmentRequest.java
│   │   ├── UpdateEnrollmentRequest.java
│   │   ├── CreateGradeRequest.java
│   │   ├── UpdateGradeRequest.java
│   │   └── CreateRecommendationRequest.java
│   │
│   └── response/                        # Response DTOs
│       ├── ApiResponse.java             # Generic wrapper for all responses
│       ├── ErrorResponse.java           # Error response format
│       ├── UserResponse.java
│       ├── SubjectResponse.java
│       ├── ClassResponse.java
│       ├── EnrollmentResponse.java
│       ├── GradeResponse.java
│       └── RecommendationResponse.java
│
├── service/                             # Service Layer (interfaces)
│   ├── UserService.java
│   ├── SubjectService.java
│   ├── ClassService.java
│   ├── EnrollmentService.java
│   ├── GradeService.java
│   ├── RecommendationService.java
│   │
│   └── impl/                            # Service implementations
│       ├── UserServiceImpl.java
│       ├── SubjectServiceImpl.java
│       ├── ClassServiceImpl.java
│       ├── EnrollmentServiceImpl.java
│       ├── GradeServiceImpl.java
│       └── RecommendationServiceImpl.java
│
├── mapper/                              # DTO Mappers
│   └── EntityMapper.java                # MapStruct mapper interface
│
├── exception/                           # Exception Handling
│   ├── ResourceNotFoundException.java   # 404 errors
│   ├── ResourceAlreadyExistsException.java # 409 conflicts
│   ├── InvalidRequestException.java     # 400 bad requests
│   ├── BusinessException.java           # Business logic errors
│   └── GlobalExceptionHandler.java      # Global exception handler
│
└── config/                              # Configuration
    ├── SwaggerConfig.java               # OpenAPI/Swagger configuration
    └── JpaAuditingConfig.java          # JPA auditing configuration

src/main/resources/
├── application.properties               # Main configuration
└── application-local.properties         # Local development overrides

src/test/java/com/viveek/aiclass/
├── AiclassApplicationTests.java
└── DatabaseConnectionTest.java

Additional Files:
├── scripts/                             # Database scripts
│   ├── add_auth_fields_and_rls.sql     # RLS setup for Supabase
│   └── run-supabase-migration.sh       # Migration runner
├── supabase/                            # Supabase configuration
│   ├── config.toml
│   └── migrations/                      # Database migrations
├── setup-env.sh.template               # Environment setup template
├── pom.xml                             # Maven dependencies
└── README.md                           # This file
```

### Architecture Highlights

- **Clean Architecture**: Clear separation between API, Domain, Service, and Data layers
- **DTOs**: Request/Response objects separate from domain entities
- **Service Layer**: Business logic isolated from controllers
- **Global Exception Handling**: Consistent error responses across all endpoints
- **MapStruct**: Automatic DTO ↔ Entity mapping
- **UUID Primary Keys**: Modern, distributed-friendly identifiers
- **JSONB Support**: Flexible metadata fields for extensibility

## 🔧 Advanced Configuration

### Environment Variables Reference

| Variable | Description | Default | Recommended (Dev) | Recommended (Prod) |
|----------|-------------|---------|-------------------|-------------------|
| `DB_URL` | JDBC connection URL | *(required)* | `jdbc:postgresql://...` | `jdbc:postgresql://...` |
| `DB_USERNAME` | Database username | *(required)* | `postgres.xxx` | `postgres.xxx` |
| `DB_PASSWORD` | Database password | *(required)* | Your password | Secure password |
| `DB_DRIVER` | Database driver class | `org.postgresql.Driver` | Default | Default |
| `HIBERNATE_DDL_AUTO` | Schema management | `validate` | `update` or `validate` | `validate` or `none` |
| `HIBERNATE_SHOW_SQL` | Log SQL statements | `true` | `true` | `false` |
| `HIBERNATE_FORMAT_SQL` | Format SQL logs | `true` | `true` | `false` |
| `HIBERNATE_DIALECT` | SQL dialect | `PostgreSQLDialect` | Default | Default |
| `SERVER_PORT` | HTTP server port | `8080` | `8080` | `8080` or `80` |
| `APP_NAME` | Application name | `aiclass` | `aiclass` | `aiclass` |
| `LOG_LEVEL` | Logging level | `INFO` | `DEBUG` | `INFO` or `WARN` |
| `SQL_LOG_LEVEL` | SQL logging level | `DEBUG` | `DEBUG` | `WARN` |

### Hibernate DDL Modes

- **`none`**: No schema management (production recommended)
- **`validate`**: Validate schema matches entities (safest for production)
- **`update`**: Update schema to match entities (development only)
- **`create`**: Drop and recreate schema on startup (dangerous!)
- **`create-drop`**: Drop schema on shutdown (testing only)

### Production Best Practices

```bash
# Production environment variables
export DB_URL="jdbc:postgresql://production-host.pooler.supabase.com:6543/postgres"
export DB_USERNAME="postgres.production-ref"
export DB_PASSWORD="$(cat /secrets/db-password)"  # From secrets manager
export HIBERNATE_DDL_AUTO="validate"               # Never use 'update' in production
export HIBERNATE_SHOW_SQL="false"                  # Don't log SQL in production
export SERVER_PORT="8080"
export LOG_LEVEL="WARN"
export SQL_LOG_LEVEL="WARN"
```

### Local Development Setup

```bash
# Development environment variables
export DB_URL="jdbc:postgresql://localhost:5432/aiclass_dev"
export DB_USERNAME="postgres"
export DB_PASSWORD="dev_password"
export HIBERNATE_DDL_AUTO="update"
export HIBERNATE_SHOW_SQL="true"
export SERVER_PORT="8080"
export LOG_LEVEL="DEBUG"
```

## 🚀 Features & Status

### ✅ Completed (v2.0)
- [x] **Clean Architecture**: Proper layering (API → Service → Repository)
- [x] **Complete REST API**: All CRUD operations for 6 entities
- [x] **UUID Primary Keys**: Modern, distributed-friendly identifiers
- [x] **DTOs**: Request/Response objects with validation
- [x] **Global Exception Handling**: Consistent error responses
- [x] **API Documentation**: Interactive Swagger/OpenAPI docs
- [x] **JSONB Support**: Flexible metadata fields
- [x] **Supabase Integration**: PostgreSQL with pooler connection
- [x] **Automatic Auditing**: Created/updated timestamps
- [x] **Environment Configuration**: Externalized configuration
- [x] **MapStruct Mapping**: Automatic DTO ↔ Entity conversion
- [x] **Input Validation**: Bean Validation annotations
- [x] **Enrollment System**: Student enrollment with status tracking
- [x] **Grade Calculation**: Automatic percentage calculation
- [x] **Query Filtering**: Filter endpoints by various criteria
- [x] **JWT Authentication**: Supabase Auth JWT token validation
- [x] **Authorization (RBAC)**: Role-based access control with @PreAuthorize
- [x] **Row Level Security**: Database-level security policies
- [x] **CORS Configuration**: Frontend integration support
- [x] **Security Context**: Helper utilities for accessing current user

### 🔄 In Progress
- [ ] **Unit Tests**: Comprehensive test coverage
- [ ] **Integration Tests**: End-to-end API testing
- [ ] **Performance Metrics**: Spring Boot Actuator integration

### 📋 Roadmap (Future Versions)

#### v2.1 - Testing & Quality
- [ ] Comprehensive unit test suite
- [ ] Integration test suite with security tests
- [ ] API rate limiting
- [ ] Performance benchmarks
- [ ] Load testing

#### v2.2 - Analytics & Reporting
- [ ] Student performance analytics
- [ ] Class performance metrics
- [ ] Grade distribution reports
- [ ] CSV/Excel export functionality
- [ ] Batch operations support

#### v2.3 - AI & Advanced Features
- [ ] AI-powered recommendations engine
- [ ] Predictive analytics for student performance
- [ ] Real-time notifications (WebSocket)
- [ ] Email notifications
- [ ] Advanced search and filtering

#### v3.0 - Enterprise Features
- [ ] Redis caching layer
- [ ] Multi-tenancy support
- [ ] Monitoring and logging (ELK stack)
- [ ] Kubernetes deployment
- [ ] API versioning
- [ ] GraphQL support

## 🔐 Security & Authentication

### ✅ Implemented Security Features

The AIClass API implements enterprise-grade security:

- ✅ **JWT Authentication**: Validates Supabase Auth tokens using JWK Set
- ✅ **Role-Based Access Control (RBAC)**: `TEACHER` and `STUDENT` roles with method-level security
- ✅ **Row Level Security (RLS)**: Database-level policies ensure data isolation
- ✅ **CORS Configuration**: Secure cross-origin requests for frontend apps
- ✅ **Stateless Sessions**: No server-side session storage (JWT-based)
- ✅ **Input Validation**: Bean Validation prevents injection attacks
- ✅ **SQL Injection Protection**: JPA/Hibernate parameterized queries

### 🚀 Quick Setup

1. **Configure Supabase credentials** in `application-local.properties`:
   ```properties
   supabase.jwt.jwk-set-uri=https://your-project.supabase.co/auth/v1/jwks
   supabase.url=https://your-project.supabase.co
   supabase.anon.key=your-anon-key
   security.cors.allowed-origins=http://localhost:3000
   ```

2. **Apply RLS policies to database**:
   ```bash
   psql $DB_URL -f supabase/migrations/20250111000000_comprehensive_auth_and_rls.sql
   ```

3. **Test authentication**:
   ```bash
   curl http://localhost:8080/api/users \
     -H "Authorization: Bearer YOUR_SUPABASE_JWT"
   ```

📖 **[Complete Authentication Guide](AUTHENTICATION_GUIDE.md)** - Detailed setup, testing, and troubleshooting

### Authorization Matrix

| Resource | GET (List) | GET (Single) | POST | PUT/PATCH | DELETE |
|----------|-----------|-------------|------|-----------|--------|
| **Users** | 🔑 TEACHER | 🔑 TEACHER | ✅ Auth | ✅ Own | ✅ Own |
| **Classes** | ✅ Auth + RLS | ✅ Auth + RLS | 🔑 TEACHER | 🔑 TEACHER | 🔑 TEACHER |
| **Grades** | ✅ Auth + RLS | ✅ Auth + RLS | 🔑 TEACHER | 🔑 TEACHER | 🔑 TEACHER |
| **Enrollments** | ✅ Auth + RLS | ✅ Auth + RLS | 🔑 TEACHER | 🔑 TEACHER | 🔑 TEACHER |
| **Subjects** | ✅ Auth | ✅ Auth | 🔑 TEACHER | 🔑 TEACHER | 🔑 TEACHER |
| **Recommendations** | ✅ Auth + RLS | ✅ Auth + RLS | ✅ Auth | ❌ No | ✅ Own |

- 🔑 **TEACHER**: Only teachers can access
- ✅ **Auth**: Any authenticated user
- ✅ **Own**: Users can only access/modify their own data
- **+RLS**: Row Level Security policies enforce data filtering

### Production Checklist

Before deploying to production:

- [ ] **Enable HTTPS**: Use TLS/SSL certificates (Let's Encrypt, CloudFlare)
- [ ] **Update Supabase Keys**: Use production keys (not local dev keys)
- [ ] **Configure CORS**: Set `security.cors.allowed-origins` to production URL
- [ ] **Secrets Management**: Move keys to environment variables or secrets manager
- [ ] **Database Connection Pooling**: Configure connection pool size
- [ ] **Rate Limiting**: Implement API rate limiting (Spring Cloud Gateway, Kong)
- [ ] **Monitoring**: Set up APM (New Relic, Datadog, or Elastic APM)
- [ ] **Logging**: Configure centralized logging (ELK Stack, CloudWatch)
- [ ] **Backup Strategy**: Automated database backups (Supabase handles this)
- [ ] **Security Headers**: Add HSTS, CSP, X-Frame-Options headers
- [ ] **Dependency Updates**: Regularly update Spring Boot and dependencies
- [ ] **Penetration Testing**: Security audit before launch

## 📚 Additional Documentation

- **[🔐 Authentication Guide](AUTHENTICATION_GUIDE.md)**: Complete security setup and troubleshooting
- **[🚀 Quick Start Guide](QUICK_START.md)**: Get up and running in 5 minutes
- **[📬 Postman Guide](POSTMAN_GUIDE.md)**: API testing with Postman
- **[🔄 API Migration Guide](API_MIGRATION_GUIDE.md)**: Migrating from v1.x
- **[📝 Refactoring Plan](REFACTORING_PLAN.md)**: Detailed refactoring documentation
- **[📊 Refactoring Summary](REFACTORING_SUMMARY.md)**: Summary of changes made
- **[📖 Swagger Documentation](SWAGGER_DOCUMENTATION_REPORT.md)**: API documentation report

## 🤝 Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines
- Follow clean architecture principles
- Write unit tests for new features
- Update API documentation
- Follow Java coding conventions
- Use meaningful commit messages

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👥 Authors & Contributors

- **Santiago Torres** - Initial development and refactoring
- **AIClass Team** - Ongoing development

## 🆘 Support & Contact

If you encounter any issues or have questions:

1. **Check Documentation**: Review this README and other docs
2. **Search Issues**: Look for existing issues in the repository
3. **Create Issue**: Open a new issue with detailed information
4. **Contact Team**: Reach out to the development team

### Reporting Issues

When reporting issues, please include:
- Spring Boot version
- Java version
- Database version
- Error messages and stack traces
- Steps to reproduce
- Expected vs actual behavior

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- Supabase team for the database platform
- All contributors and users of this project

---

**Version**: 2.0.0  
**Last Updated**: October 2025  
**Status**: Active Development

**Happy Coding! 🎓💻** 