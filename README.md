# AIClass API - Student Performance Management System

A modern, enterprise-grade REST API for academic management and student performance analytics with **JWT authentication**, built with Spring Boot 3.5 and Supabase PostgreSQL.

## 🚀 Quick Start

### Prerequisites
- **Java 21** (OpenJDK recommended)
- **Maven 3.6+** (or use included wrapper `./mvnw`)
- **Supabase Account** with PostgreSQL database

### 1. Clone Repository
```bash
git clone <your-repository-url>
cd student-performance-api
```

### 2. Configure Database & Auth

#### Get Your Supabase Credentials
1. Go to [Supabase Dashboard](https://supabase.com/dashboard)
2. Select your project
3. Navigate to **Project Settings** → **API**:
   - Copy **URL** (e.g., `https://xxx.supabase.co`)
   - Copy **anon/public key**
   - Copy **service_role key**
4. Navigate to **Project Settings** → **Database**:
   - Copy **Connection string** (Session Pooler, port 5432)
5. Navigate to **Project Settings** → **API** → scroll to **JWT Settings**:
   - Copy **JWT Secret**

#### Configure Application
```bash
# Copy the template
cp src/main/resources/application-local.properties.template src/main/resources/application-local.properties

# Edit and add your credentials
nano src/main/resources/application-local.properties
```

Replace all `YOUR_*` placeholders with actual values from Supabase.

### 3. Run Database Migrations
```bash
# Apply authentication and RLS policies
psql "postgresql://postgres.YOUR_PROJECT_REF:YOUR_PASSWORD@aws-0-us-east-0.pooler.supabase.com:6543/postgres" \
  -f supabase/migrations/20250111000000_comprehensive_auth_and_rls.sql

# Apply auto-profile creation trigger
psql "postgresql://postgres.YOUR_PROJECT_REF:YOUR_PASSWORD@aws-0-us-east-0.pooler.supabase.com:6543/postgres" \
  -f supabase/migrations/20250111000001_auto_create_user_profile.sql
```

**Alternative**: Run migrations directly in Supabase SQL Editor (copy-paste the SQL files).

### 4. Disable Email Confirmation (Development Only)
In Supabase Dashboard → **Authentication** → **Providers** → **Email**:
- Uncheck "Confirm email"

### 5. Start the Application
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### 6. Access the API
- **API Base**: http://localhost:8080/api
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Docs**: http://localhost:8080/v3/api-docs

## 🔐 Authentication & Authorization

### How Authentication Works
1. **User Signs Up** via Supabase Auth → Creates user in `auth.users` + triggers auto-creation in `public.users`
2. **User Logs In** → Receives JWT token
3. **API Requests** → Include `Authorization: Bearer <jwt-token>` header
4. **Spring Security** validates JWT and enforces role-based access

### Quick Test with Postman

Import the collection: `AIClass_API_v2_with_Auth.postman_collection.json`

1. **Signup** (`POST {{supabase_url}}/auth/v1/signup`):
```json
{
  "email": "teacher@university.edu",
  "password": "password123",
  "data": {
    "full_name": "Test Teacher",
    "role": "teacher"
  }
}
```

2. **Login** (`POST {{supabase_url}}/auth/v1/token?grant_type=password`):
```json
{
  "email": "teacher@university.edu",
  "password": "password123"
}
```
The collection auto-saves the JWT token.

3. **Use API** - All requests automatically include the token.

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
- **+RLS**: Row Level Security policies enforce data filtering at database level

📖 **Detailed Guide**: [AUTHENTICATION_GUIDE.md](AUTHENTICATION_GUIDE.md)

## ✨ Key Features

- **JWT Authentication** - Supabase Auth with HS256 token validation
- **Role-Based Access Control (RBAC)** - `@PreAuthorize` method-level security
- **Row Level Security (RLS)** - Database-level policies for data isolation
- **User Management** - Teachers and students
- **Subject Management** - Academic subjects/courses
- **Class Management** - Class sections with teacher assignments
- **Enrollment System** - Student enrollment with status tracking
- **Grade Management** - Automatic percentage calculation
- **AI Recommendations** - AI-generated recommendations
- **Clean Architecture** - Controller → Service → Repository
- **UUID Primary Keys** - Globally unique identifiers
- **JSONB Metadata** - Flexible extensibility
- **Interactive API Docs** - Swagger/OpenAPI with auth support
- **Global Exception Handling** - Consistent error responses

## 📚 API Endpoints

### Base URL
```
http://localhost:8080/api
```

### Core Resources

| Resource | Endpoints |
|----------|-----------|
| **Users** | `GET/POST /api/users`, `GET/PUT/DELETE /api/users/{id}`, `GET /api/users/auth/{authUserId}`, `GET /api/users/email/{email}` |
| **Subjects** | `GET/POST /api/subjects`, `GET/PUT/DELETE /api/subjects/{id}`, `GET /api/subjects/code/{code}` |
| **Classes** | `GET/POST /api/classes`, `GET/PUT/DELETE /api/classes/{id}` (filters: teacher, subject, year, semester) |
| **Enrollments** | `GET/POST /api/enrollments`, `GET/PATCH/DELETE /api/enrollments/{id}` (filters: class, student, status) |
| **Grades** | `GET/POST /api/grades`, `GET/PUT/DELETE /api/grades/{id}` (filters: class, student) |
| **Recommendations** | `GET/POST /api/recommendations`, `GET/DELETE /api/recommendations/{id}` (filters: recipient, class, audience) |

All responses follow the `ApiResponse<T>` wrapper pattern:
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { /* your data */ },
  "timestamp": "2025-10-11T10:00:00Z"
}
```

**📖 Full API Documentation**: http://localhost:8080/swagger-ui.html

## 🛠️ Technology Stack

- **Java 21** - Latest LTS
- **Spring Boot 3.5.5** - Framework
- **Spring Security** - JWT authentication & RBAC
- **Spring Data JPA** - Persistence
- **PostgreSQL (Supabase)** - Database with RLS
- **Lombok** - Boilerplate reduction
- **MapStruct** - DTO mapping
- **SpringDoc OpenAPI** - API docs
- **Maven** - Build tool

## 🗄️ Data Model

### Core Entities

- **User** (`users`) - Teachers and students with Supabase auth integration, role (TEACHER/STUDENT)
- **Subject** (`subjects`) - Academic subjects with unique codes, credits
- **Class** (`classes`) - Class sections with teacher, subject, schedule, year, semester
- **Enrollment** (`enrollments`) - Student enrollments with status (ACTIVE/DROPPED/COMPLETED)
- **Grade** (`grades`) - Student grades with automatic percentage calculation
- **AI Recommendation** (`ai_recommendations`) - AI-generated recommendations by audience

All entities use **UUID primary keys** and include **JSONB metadata** fields for extensibility.

## 🏗️ Project Structure

```
src/main/java/com/viveek/aiclass/
├── api/controller/          # REST Controllers
├── domain/
│   ├── model/              # JPA Entities
│   └── repository/         # JPA Repositories
├── dto/
│   ├── request/            # Request DTOs
│   └── response/           # Response DTOs (includes ApiResponse wrapper)
├── service/                # Business logic
│   └── impl/               # Service implementations
├── security/               # JWT auth, converters, helpers
├── exception/              # Custom exceptions & global handler
├── mapper/                 # MapStruct mappers
└── config/                 # Spring configuration (Security, Swagger, JPA)

src/main/resources/
├── application.properties                    # Base config with placeholders
└── application-local.properties.template     # Template for local dev

supabase/migrations/
├── 20250111000000_comprehensive_auth_and_rls.sql  # RLS policies
└── 20250111000001_auto_create_user_profile.sql    # Auto-create profile trigger
```

## 📦 Building for Production

### Build JAR
```bash
./mvnw clean package -DskipTests
```

### Run Production
```bash
export DB_URL="jdbc:postgresql://..."
export DB_USERNAME="postgres.xxx"
export DB_PASSWORD="your-password"
export HIBERNATE_DDL_AUTO="validate"
export HIBERNATE_SHOW_SQL="false"

java -jar target/aiclass-0.0.1-SNAPSHOT.jar
```

### Production Checklist

- [ ] Enable HTTPS with TLS/SSL
- [ ] Use production Supabase keys (not dev keys)
- [ ] Configure `security.cors.allowed-origins` for production domains
- [ ] Set `spring.jpa.hibernate.ddl-auto=validate` or `none`
- [ ] Disable SQL logging (`spring.jpa.show-sql=false`)
- [ ] Move secrets to environment variables or secrets manager
- [ ] Configure connection pool sizing
- [ ] Enable RLS on all database tables
- [ ] Set up monitoring (APM, logs)
- [ ] Configure automated database backups

## 📚 Additional Documentation

- [🔐 Authentication Guide](AUTHENTICATION_GUIDE.md) - Complete auth setup, testing, and troubleshooting
- [🧪 Testing Guide](AUTH_TESTING_GUIDE.md) - Postman and Swagger testing
- [📮 Postman Guide](POSTMAN_GUIDE.md) - API testing with Postman
- [🚀 Quick Start](QUICK_START.md) - Get running in 5 minutes

## 🔧 Troubleshooting

### Common Issues

**Database connection fails**:
- Verify connection string (use Session Pooler port 5432 for IPv4)
- URL-encode special characters in password (spaces = `%20`)
- Check firewall allows outbound to Supabase

**JWT validation fails**:
- Ensure `supabase.jwt.secret` matches your Supabase JWT Secret (not anon key!)
- Verify token is not expired
- Check token format: `Authorization: Bearer <token>`

**401 Unauthorized after login**:
- Confirm email in Supabase dashboard or disable email confirmation
- Verify user exists in both `auth.users` and `public.users` tables
- Check application logs for JWT decoding errors

**User creation fails with constraint error**:
- Ensure role is lowercase (`"role": "teacher"` not `"role": "TEACHER"`)
- Verify trigger `handle_new_user` exists and is enabled

**More help**: See [AUTHENTICATION_GUIDE.md](AUTHENTICATION_GUIDE.md)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Guidelines
- Follow clean architecture principles
- Write unit tests for new features
- Update API documentation
- Follow Java coding conventions

## 📄 License

This project is licensed under the MIT License.

## 🆘 Support

For issues or questions:
1. Check [AUTHENTICATION_GUIDE.md](AUTHENTICATION_GUIDE.md) and other docs
2. Search existing issues
3. Open a new issue with:
   - Spring Boot version
   - Java version
   - Database version
   - Error messages and stack traces
   - Steps to reproduce

---

**Version**: 2.0.0  
**Last Updated**: October 2025  
**Status**: Active Development
