# API Refactoring Plan - Database Schema Alignment

## Executive Summary

The current Java API uses Spanish naming conventions with `Long` IDs, while the new Supabase database uses English naming with `UUID` primary keys. This requires a **complete refactoring** of models, repositories, controllers, DTOs, and services to align with the new schema and implement clean code practices.

---

## Current State vs Target State Analysis

### 1. Table Name Mapping

| Current (Java) | Target (Database) | Status |
|----------------|-------------------|---------|
| `usuarios` | `users` | ❌ Mismatch |
| `clases` | `classes` | ❌ Mismatch |
| `materias` | `subjects` | ❌ Mismatch |
| `notas` | `grades` | ❌ Mismatch |
| `estudiantes_clase` | `enrollments` | ❌ Mismatch |
| `recomendaciones_ia` | `ai_recommendations` | ❌ Mismatch |

### 2. Primary Key Type Mismatch

| Entity | Current | Target | Impact |
|--------|---------|--------|--------|
| All entities | `Long` with `@GeneratedValue(IDENTITY)` | `UUID` with `gen_random_uuid()` | **CRITICAL** - All relationships affected |

### 3. Column Name Mapping

#### Users (usuarios → users)
| Current | Target | Type Change | Notes |
|---------|--------|-------------|-------|
| `id` (Long) | `id` (UUID) | ✅ Type change | Primary key |
| `auth_user_id` (UUID) | `auth_user_id` (UUID) | ✅ Match | Auth integration |
| `nombre` | `full_name` | ❌ Rename | |
| `email` | `email` | ✅ Match | |
| `rol` | `role` | ❌ Rename | |
| `password_hash` | ❌ Removed | - | Not in new schema |
| `last_password_changed` | ❌ Removed | - | Not in new schema |
| `fecha_creacion` | ❌ Removed | - | Legacy field |
| ❌ Missing | `metadata` (JSONB) | New field | Flexible data storage |
| `created_at` | `created_at` | ✅ Match | |
| `updated_at` | `updated_at` | ✅ Match | |

#### Classes (clases → classes)
| Current | Target | Type Change | Notes |
|---------|--------|-------------|-------|
| `id` (Long) | `id` (UUID) | ✅ Type change | |
| `materia_id` (Long) | `subject_id` (UUID) | ✅ Rename + Type | |
| `profesor_id` (Long) | `teacher_user_id` (UUID) | ✅ Rename + Type | |
| `grupo` | `group_code` | ❌ Rename | |
| `anio` (Integer) | `year` (Integer) | ❌ Rename | CHECK: 2000-2100 |
| `semestre` (Integer) | `semester` (Text) | ✅ Type + Rename | Enum: spring/summer/fall/winter |
| ❌ Missing | `metadata` (JSONB) | New field | |
| ❌ Missing | `created_at` | New field | |
| ❌ Missing | `updated_at` | New field | |

#### Subjects (materias → subjects)
| Current | Target | Type Change | Notes |
|---------|--------|-------------|-------|
| `id` (Long) | `id` (UUID) | ✅ Type change | |
| `nombre` | `name` | ❌ Rename | |
| `codigo` | `code` | ❌ Rename | UNIQUE constraint |
| `descripcion` | `description` | ❌ Rename | |
| ❌ Missing | `created_at` | New field | |
| ❌ Missing | `updated_at` | New field | |

#### Grades (notas → grades)
| Current | Target | Type Change | Notes |
|---------|--------|-------------|-------|
| `id` (Long) | `id` (UUID) | ✅ Type change | |
| `estudiante_id` (Long) | `student_user_id` (UUID) | ✅ Rename + Type | |
| `clase_id` (Long) | `class_id` (UUID) | ✅ Rename + Type | |
| `tipo` | `assessment_kind` | ❌ Rename | e.g., exam, quiz, homework |
| ❌ Missing | `assessment_name` (Text) | New field | e.g., "Midterm Exam" |
| `valor` (Double) | `score` (Numeric) | ❌ Rename + Type | |
| ❌ Missing | `max_score` (Numeric) | New field | For percentage calculation |
| `fecha_registro` | `graded_at` | ❌ Rename | |
| ❌ Missing | `created_at` | New field | |
| ❌ Missing | `updated_at` | New field | |

#### Enrollments (estudiantes_clase → enrollments)
| Current | Target | Type Change | Notes |
|---------|--------|-------------|-------|
| `id` (Long) | `id` (UUID) | ✅ Type change | |
| `clase_id` (Long) | `class_id` (UUID) | ✅ Rename + Type | |
| `estudiante_id` (Long) | `student_user_id` (UUID) | ✅ Rename + Type | |
| ❌ Missing | `status` (Text) | New field | Enum: active/dropped/completed |
| ❌ Missing | `enrolled_at` | New field | |
| ❌ Missing | `created_at` | New field | |
| ❌ Missing | `updated_at` | New field | |

#### AI Recommendations (recomendaciones_ia → ai_recommendations)
| Current | Target | Type Change | Notes |
|---------|--------|-------------|-------|
| `id` (Long) | `id` (UUID) | ✅ Type change | |
| `usuario_id` (Long) | `recipient_user_id` (UUID) | ✅ Rename + Type | More descriptive |
| `clase_id` (Long) | `class_id` (UUID) | ✅ Rename + Type | |
| `mensaje` | `message` | ❌ Rename | |
| `tipo` | `audience` (Text) | ❌ Rename + Semantic | Enum: teacher/student |
| `fecha_generacion` | ❌ Removed | - | Use created_at |
| ❌ Missing | `metadata` (JSONB) | New field | AI model info, confidence, etc. |
| ❌ Missing | `created_at` | New field | |
| ❌ Missing | `updated_at` | New field | |

---

## Refactoring Strategy

### Phase 1: Foundation Layer (Models & Repositories)

#### 1.1 Create New Model Package Structure
```
com.viveek.aiclass.domain.model/
├── User.java (replaces Usuario)
├── Class.java (replaces Clase)
├── Subject.java (replaces Materia)
├── Grade.java (replaces Nota)
├── Enrollment.java (replaces EstudianteClase)
└── AiRecommendation.java (replaces RecomendacionIA)

com.viveek.aiclass.domain.model.enums/
├── UserRole.java (enum: TEACHER, STUDENT)
├── Semester.java (enum: SPRING, SUMMER, FALL, WINTER)
├── EnrollmentStatus.java (enum: ACTIVE, DROPPED, COMPLETED)
└── RecommendationAudience.java (enum: TEACHER, STUDENT)
```

#### 1.2 UUID Strategy
- All entities use `UUID` as primary key
- Use `@GeneratedValue(generator = "UUID")` with Hibernate UUID generator
- Foreign keys also use UUID

#### 1.3 Auditing Strategy
- Create `@MappedSuperclass` for common fields (id, created_at, updated_at)
- Use Spring Data JPA `@CreatedDate` and `@LastModifiedDate`
- Enable JPA auditing with `@EnableJpaAuditing`

#### 1.4 JSONB Handling
- Use `@JdbcTypeCode(SqlTypes.JSON)` for metadata fields
- Create typed wrapper classes for metadata when needed

### Phase 2: DTOs & Request/Response Objects

#### 2.1 Request DTOs
```
com.viveek.aiclass.dto.request/
├── CreateUserRequest.java
├── UpdateUserRequest.java
├── CreateClassRequest.java
├── CreateSubjectRequest.java
├── CreateGradeRequest.java
├── EnrollStudentRequest.java
└── CreateRecommendationRequest.java
```

#### 2.2 Response DTOs
```
com.viveek.aiclass.dto.response/
├── UserResponse.java
├── ClassResponse.java
├── SubjectResponse.java
├── GradeResponse.java
├── EnrollmentResponse.java
├── RecommendationResponse.java
├── ApiErrorResponse.java
└── ApiSuccessResponse.java
```

#### 2.3 DTO Mapping
- Use MapStruct or implement manual mapping
- Never expose entities directly in API responses

### Phase 3: Service Layer (Clean Architecture)

#### 3.1 Service Interfaces
```
com.viveek.aiclass.service/
├── UserService.java
├── ClassService.java
├── SubjectService.java
├── GradeService.java
├── EnrollmentService.java
└── RecommendationService.java
```

#### 3.2 Service Implementation
```
com.viveek.aiclass.service.impl/
├── UserServiceImpl.java
├── ClassServiceImpl.java
├── SubjectServiceImpl.java
├── GradeServiceImpl.java
├── EnrollmentServiceImpl.java
└── RecommendationServiceImpl.java
```

#### 3.3 Business Logic
- Move all business logic from controllers to services
- Implement proper validation
- Handle exceptions with custom exceptions
- Transaction management with `@Transactional`

### Phase 4: Exception Handling

#### 4.1 Custom Exceptions
```
com.viveek.aiclass.exception/
├── ResourceNotFoundException.java
├── ResourceAlreadyExistsException.java
├── InvalidRequestException.java
├── AuthenticationException.java
└── BusinessException.java
```

#### 4.2 Global Exception Handler
```
com.viveek.aiclass.exception.handler/
└── GlobalExceptionHandler.java (@ControllerAdvice)
```

### Phase 5: Controller Layer (API Endpoints)

#### 5.1 RESTful Controller Design
```
com.viveek.aiclass.controller/
├── UserController.java (/api/users)
├── ClassController.java (/api/classes)
├── SubjectController.java (/api/subjects)
├── GradeController.java (/api/grades)
├── EnrollmentController.java (/api/enrollments)
└── RecommendationController.java (/api/recommendations)
```

#### 5.2 API Design Principles
- Use proper HTTP methods (GET, POST, PUT, PATCH, DELETE)
- Use plural nouns for resources
- Use UUID in path parameters
- Proper HTTP status codes (200, 201, 204, 400, 404, 500)
- Consistent response structure

### Phase 6: Repository Layer

#### 6.1 JPA Repositories
```
com.viveek.aiclass.repository/
├── UserRepository.java
├── ClassRepository.java
├── SubjectRepository.java
├── GradeRepository.java
├── EnrollmentRepository.java
└── RecommendationRepository.java
```

#### 6.2 Custom Queries
- Use derived query methods where possible
- `@Query` annotation for complex queries
- Consider query performance and indexing

### Phase 7: Configuration & Security

#### 7.1 Database Configuration
- Update Hibernate DDL to `validate` (not `update`)
- Ensure proper connection pooling
- Configure for UUID generation

#### 7.2 Supabase Auth Integration
- Create filter/interceptor for JWT validation
- Extract `auth_user_id` from JWT claims
- Row Level Security (RLS) considerations

#### 7.3 CORS Configuration
- Configure allowed origins
- Proper headers for authentication

### Phase 8: Testing

#### 8.1 Unit Tests
- Service layer tests with mocked repositories
- DTO mapping tests
- Validation tests

#### 8.2 Integration Tests
- Repository tests with test containers
- API endpoint tests
- End-to-end workflow tests

### Phase 9: Migration & Deployment

#### 9.1 Data Migration
- Script to migrate existing data (if any)
- Handle ID transformation (Long → UUID)

#### 9.2 API Versioning
- Consider versioning strategy (`/api/v1/`, `/api/v2/`)
- Maintain backward compatibility if needed

#### 9.3 Documentation
- Update Swagger/OpenAPI documentation
- Update README
- API usage examples
- Postman collection update

---

## Clean Code Practices to Implement

### 1. SOLID Principles
- **S**ingle Responsibility: Each class has one reason to change
- **O**pen/Closed: Open for extension, closed for modification
- **L**iskov Substitution: Proper inheritance hierarchies
- **I**nterface Segregation: Small, focused interfaces
- **D**ependency Inversion: Depend on abstractions, not concretions

### 2. Naming Conventions
- Use English for all code (classes, methods, variables)
- Descriptive names: `findUserByAuthUserId()` not `getUserByAuth()`
- Follow Java naming conventions (camelCase, PascalCase)

### 3. Code Organization
- Package by feature/domain, not by layer
- Keep related code together
- Minimize dependencies between packages

### 4. Error Handling
- Never return null; use `Optional<>`
- Throw specific exceptions
- Global exception handling
- Meaningful error messages

### 5. Validation
- Use Bean Validation (`@Valid`, `@NotNull`, etc.)
- Validate at DTO level
- Business rule validation in service layer

### 6. Logging
- Use SLF4J with Logback
- Appropriate log levels (DEBUG, INFO, WARN, ERROR)
- Structured logging for important operations

### 7. Documentation
- JavaDoc for public APIs
- Swagger annotations for REST endpoints
- README for setup and usage

---

## Implementation Order (Task Breakdown)

### Week 1: Foundation
1. ✅ Create base entity class with UUID and timestamps
2. ✅ Create all enum classes
3. ✅ Create all entity models (6 entities)
4. ✅ Create all repository interfaces
5. ✅ Configure JPA for UUID support
6. ✅ Test database connectivity

### Week 2: DTOs & Mapping
7. ✅ Create all request DTOs
8. ✅ Create all response DTOs
9. ✅ Implement DTO mapping (MapStruct or manual)
10. ✅ Create validation annotations
11. ✅ Test DTO validation

### Week 3: Service Layer
12. ✅ Create service interfaces
13. ✅ Implement UserService
14. ✅ Implement ClassService
15. ✅ Implement SubjectService
16. ✅ Implement GradeService
17. ✅ Implement EnrollmentService
18. ✅ Implement RecommendationService

### Week 4: Exception Handling & Controllers
19. ✅ Create custom exceptions
20. ✅ Create global exception handler
21. ✅ Implement UserController
22. ✅ Implement ClassController
23. ✅ Implement SubjectController
24. ✅ Implement GradeController
25. ✅ Implement EnrollmentController
26. ✅ Implement RecommendationController

### Week 5: Testing & Documentation
27. ✅ Write unit tests for services
28. ✅ Write integration tests
29. ✅ Update Swagger documentation
30. ✅ Update Postman collection
31. ✅ Update README

### Week 6: Cleanup & Deployment
32. ✅ Remove old Spanish-named classes
33. ✅ Remove DatabaseSeeder if needed
34. ✅ Final testing
35. ✅ Deploy to staging
36. ✅ User acceptance testing

---

## Migration Considerations

### Backward Compatibility
- Consider keeping old endpoints temporarily
- Deprecate old endpoints
- Provide migration guide for API consumers

### Database Migration
- **CRITICAL**: The database already uses the new schema
- No need to migrate database schema
- Just need to align Java code with existing schema

### Environment Variables
- Update `.env` files
- Document required environment variables
- Update deployment configurations

---

## Risk Assessment

| Risk | Impact | Likelihood | Mitigation |
|------|--------|------------|------------|
| UUID incompatibility with existing data | High | Medium | No existing production data to migrate |
| Breaking API changes | High | High | Implement API versioning |
| Performance degradation | Medium | Low | Proper indexing, connection pooling |
| Auth integration issues | High | Medium | Thorough testing with Supabase |
| Missing business logic | Medium | Medium | Document and validate requirements |

---

## Success Criteria

1. ✅ All entities align with database schema
2. ✅ All API endpoints use UUID
3. ✅ Proper error handling and validation
4. ✅ Clean separation of concerns (Controller → Service → Repository)
5. ✅ Comprehensive test coverage (>80%)
6. ✅ Updated API documentation
7. ✅ Zero Spanish naming in new code
8. ✅ Authentication integration working
9. ✅ All existing functionality preserved
10. ✅ Code passes SonarQube/Checkstyle standards

---

## Next Steps

1. **Review and approve this plan**
2. **Set up development environment**
3. **Create feature branch for refactoring**
4. **Begin Phase 1 implementation**
5. **Regular code reviews**
6. **Continuous testing**

---

## Questions for Stakeholder

1. Do we need to maintain backward compatibility with existing API?
2. Is there existing production data that needs migration?
3. What is the timeline for this refactoring?
4. Should we implement API versioning (/api/v1/, /api/v2/)?
5. Are there any additional business requirements not captured in the schema?
6. What authentication/authorization requirements exist beyond Supabase?
7. Do we need role-based access control (RBAC)?
8. What are the performance requirements?

---

## Appendix: Key Dependencies

```xml
<!-- UUID Support -->
<dependency>
    <groupId>org.hibernate.orm</groupId>
    <artifactId>hibernate-core</artifactId>
</dependency>

<!-- JSONB Support -->
<dependency>
    <groupId>io.hypersistence</groupId>
    <artifactId>hypersistence-utils-hibernate-63</artifactId>
    <version>3.7.0</version>
</dependency>

<!-- MapStruct for DTO Mapping -->
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.5.5.Final</version>
</dependency>

<!-- JWT for Supabase Auth -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
```

---

**Document Version:** 1.0  
**Date:** October 11, 2025  
**Author:** AI Assistant  
**Status:** Pending Approval

