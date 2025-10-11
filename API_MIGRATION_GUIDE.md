# API Migration Guide - Version 2.0.0

## Overview

This document provides a comprehensive guide for migrating from the old Spanish-named API (v1.x) to the new English-named, clean architecture API (v2.0.0).

## Major Changes

### 1. Database Schema Changes

#### Table Names
| Old (v1.x) | New (v2.0) |
|------------|------------|
| `usuarios` | `users` |
| `clases` | `classes` |
| `materias` | `subjects` |
| `notas` | `grades` |
| `estudiantes_clase` | `enrollments` |
| `recomendaciones_ia` | `ai_recommendations` |

#### Primary Key Type
- **Old**: `BIGINT` (Long) with auto-increment
- **New**: `UUID` with `gen_random_uuid()`

### 2. API Endpoint Changes

#### Base URL
All endpoints remain at `/api/*` but resource names have changed.

#### Users (formerly Usuarios)
| Old Endpoint | New Endpoint | Notes |
|--------------|--------------|-------|
| `GET /api/usuarios` | `GET /api/users` | Now supports `?role=TEACHER/STUDENT` filter |
| `GET /api/usuarios/{id}` | `GET /api/users/{id}` | ID is now UUID |
| `GET /api/usuarios/email/{email}` | `GET /api/users/email/{email}` | Same |
| `POST /api/usuarios` | `POST /api/users` | New request format |
| `PUT /api/usuarios/{id}` | `PUT /api/users/{id}` | ID is now UUID |
| `DELETE /api/usuarios/{id}` | `DELETE /api/users/{id}` | ID is now UUID |
| N/A | `GET /api/users/auth/{authUserId}` | **NEW**: Get by Supabase auth ID |

#### Subjects (formerly Materias)
| Old Endpoint | New Endpoint | Notes |
|--------------|--------------|-------|
| `GET /api/materias` | `GET /api/subjects` | |
| `GET /api/materias/{id}` | `GET /api/subjects/{id}` | ID is now UUID |
| `POST /api/materias` | `POST /api/subjects` | |
| `PUT /api/materias/{id}` | `PUT /api/subjects/{id}` | ID is now UUID |
| `DELETE /api/materias/{id}` | `DELETE /api/subjects/{id}` | ID is now UUID |
| N/A | `GET /api/subjects/code/{code}` | **NEW**: Get by subject code |

#### Classes (formerly Clases)
| Old Endpoint | New Endpoint | Notes |
|--------------|--------------|-------|
| `GET /api/clases` | `GET /api/classes` | Now supports multiple filters |
| `GET /api/clases/{id}` | `GET /api/classes/{id}` | ID is now UUID |
| `GET /api/clases/profesor/{profesorId}` | `GET /api/classes?teacherId={uuid}` | Query param instead of path |
| `POST /api/clases` | `POST /api/classes` | |
| `PUT /api/clases/{id}` | `PUT /api/classes/{id}` | ID is now UUID |
| `DELETE /api/clases/{id}` | `DELETE /api/classes/{id}` | ID is now UUID |

#### Grades (formerly Notas)
| Old Endpoint | New Endpoint | Notes |
|--------------|--------------|-------|
| `GET /api/notas/clase/{claseId}` | `GET /api/grades?classId={uuid}` | Query param |
| `GET /api/notas/estudiante/{estudianteId}` | `GET /api/grades?studentId={uuid}` | Query param |
| `GET /api/notas/{id}` | `GET /api/grades/{id}` | ID is now UUID |
| `POST /api/notas` | `POST /api/grades` | |
| `PUT /api/notas/{id}` | `PUT /api/grades/{id}` | ID is now UUID |
| `DELETE /api/notas/{id}` | `DELETE /api/grades/{id}` | ID is now UUID |

#### Enrollments (formerly Estudiantes_Clase)
| Old Endpoint | New Endpoint | Notes |
|--------------|--------------|-------|
| N/A | `POST /api/enrollments` | **NEW**: Enroll student |
| N/A | `GET /api/enrollments/{id}` | **NEW**: Get enrollment |
| N/A | `GET /api/enrollments?classId={uuid}` | **NEW**: Get by class |
| N/A | `GET /api/enrollments?studentId={uuid}` | **NEW**: Get by student |
| N/A | `PATCH /api/enrollments/{id}` | **NEW**: Update status |
| N/A | `DELETE /api/enrollments/{id}` | **NEW**: Delete enrollment |

#### Recommendations (formerly Recomendaciones)
| Old Endpoint | New Endpoint | Notes |
|--------------|--------------|-------|
| `GET /api/recomendaciones/usuario/{usuarioId}` | `GET /api/recommendations?recipientId={uuid}` | |
| `GET /api/recomendaciones/clase/{claseId}` | `GET /api/recommendations?classId={uuid}` | |
| `GET /api/recomendaciones/tipo/{tipo}` | `GET /api/recommendations?audience={TEACHER/STUDENT}` | |
| `GET /api/recomendaciones/{id}` | `GET /api/recommendations/{id}` | ID is now UUID |
| `POST /api/recomendaciones` | `POST /api/recommendations` | |
| `DELETE /api/recomendaciones/{id}` | `DELETE /api/recommendations/{id}` | ID is now UUID |

### 3. Request/Response Format Changes

#### User (formerly Usuario)

**Old Request Format:**
```json
{
  "nombre": "John Doe",
  "email": "john@example.com",
  "rol": "profesor",
  "password": "secret123"
}
```

**New Request Format:**
```json
{
  "authUserId": "550e8400-e29b-41d4-a716-446655440000",
  "fullName": "John Doe",
  "email": "john@example.com",
  "role": "TEACHER",
  "metadata": {}
}
```

**Key Changes:**
- `nombre` → `fullName`
- `rol` → `role` (enum: `TEACHER` or `STUDENT`)
- `authUserId` is now required (UUID from Supabase)
- Password handling removed (handled by Supabase)
- Added `metadata` field (JSONB)

#### Class (formerly Clase)

**Old Request Format:**
```json
{
  "materiaId": 1,
  "profesorId": 2,
  "grupo": "A",
  "anio": 2024,
  "semestre": 1
}
```

**New Request Format:**
```json
{
  "subjectId": "550e8400-e29b-41d4-a716-446655440000",
  "teacherId": "660e8400-e29b-41d4-a716-446655440001",
  "year": 2024,
  "semester": "SPRING",
  "groupCode": "A",
  "metadata": {}
}
```

**Key Changes:**
- All IDs are now UUIDs
- `materiaId` → `subjectId`
- `profesorId` → `teacherId`
- `grupo` → `groupCode`
- `anio` → `year`
- `semestre` → `semester` (enum: `SPRING`, `SUMMER`, `FALL`, `WINTER`)
- Added `metadata` field

#### Grade (formerly Nota)

**Old Request Format:**
```json
{
  "estudianteId": 1,
  "claseId": 2,
  "tipo": "examen",
  "valor": 85.5
}
```

**New Request Format:**
```json
{
  "classId": "550e8400-e29b-41d4-a716-446655440000",
  "studentId": "660e8400-e29b-41d4-a716-446655440001",
  "assessmentKind": "exam",
  "assessmentName": "Midterm Exam",
  "score": 85.5,
  "maxScore": 100,
  "gradedAt": "2024-10-11T10:00:00Z"
}
```

**Key Changes:**
- All IDs are now UUIDs
- `estudianteId` → `studentId`
- `claseId` → `classId`
- `tipo` → `assessmentKind`
- `valor` → `score`
- Added `assessmentName` field
- Added `maxScore` field (required)
- Added `gradedAt` field

**Response includes percentage calculation:**
```json
{
  "id": "770e8400-e29b-41d4-a716-446655440002",
  "score": 85.5,
  "maxScore": 100,
  "percentage": 85.50,
  ...
}
```

#### Enrollment (NEW)

**Request Format:**
```json
{
  "classId": "550e8400-e29b-41d4-a716-446655440000",
  "studentId": "660e8400-e29b-41d4-a716-446655440001",
  "status": "ACTIVE"
}
```

**Status Enum**: `ACTIVE`, `DROPPED`, `COMPLETED`

#### Recommendation (formerly RecomendacionIA)

**Old Request Format:**
```json
{
  "usuarioId": 1,
  "claseId": 2,
  "mensaje": "Necesita mejorar en matemáticas",
  "tipo": "estudiante"
}
```

**New Request Format:**
```json
{
  "classId": "550e8400-e29b-41d4-a716-446655440000",
  "recipientId": "660e8400-e29b-41d4-a716-446655440001",
  "audience": "STUDENT",
  "message": "Needs improvement in mathematics",
  "metadata": {
    "confidence": 0.95,
    "model": "gpt-4",
    "category": "academic"
  }
}
```

**Key Changes:**
- `usuarioId` → `recipientId` (more descriptive)
- `tipo` → `audience` (enum: `TEACHER` or `STUDENT`)
- `mensaje` → `message`
- Added `metadata` field for AI-specific data

### 4. Response Format

All responses are now wrapped in a standard `ApiResponse` format:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "timestamp": "2024-10-11T10:00:00Z"
}
```

For errors:

```json
{
  "message": "User not found with id: '550e8400-e29b-41d4-a716-446655440000'",
  "status": 404,
  "error": "Not Found",
  "path": "/api/users/550e8400-e29b-41d4-a716-446655440000",
  "details": [],
  "timestamp": "2024-10-11T10:00:00Z"
}
```

### 5. HTTP Status Codes

Proper HTTP status codes are now used:

| Operation | Success Code | Error Codes |
|-----------|--------------|-------------|
| Create (POST) | 201 Created | 400, 409 |
| Read (GET) | 200 OK | 404 |
| Update (PUT/PATCH) | 200 OK | 400, 404 |
| Delete | 204 No Content | 404 |

### 6. Validation

All requests now have proper validation:

- Email format validation
- Required field validation
- Range validation (e.g., year between 2000-2100)
- UUID format validation
- Enum validation

Validation errors return 400 Bad Request with detailed error messages.

## Migration Steps

### Step 1: Update Client Code

1. Replace all Long IDs with UUID
2. Update all endpoint URLs
3. Update request/response field names
4. Update enum values to uppercase

### Step 2: Data Migration

If you have existing data with Long IDs:

1. Create a mapping table from Long IDs to UUIDs
2. Update all foreign key references
3. Update client applications to use new UUIDs

### Step 3: Authentication Integration

1. Integrate with Supabase Auth
2. Obtain `authUserId` from Supabase JWT
3. Use `authUserId` when creating users

### Step 4: Testing

1. Test all endpoints with UUID format
2. Verify enum values work correctly
3. Test error responses
4. Verify metadata fields work as expected

## Breaking Changes Summary

1. **All IDs are now UUIDs** - Cannot use numeric IDs
2. **Enum values are uppercase** - `profesor` → `TEACHER`
3. **Field names changed** - See tables above
4. **Password removed** - Auth handled by Supabase
5. **Response format wrapped** - All responses in `ApiResponse` wrapper
6. **Semester is now enum** - Was integer, now string enum
7. **Required fields added** - `authUserId`, `maxScore`, etc.

## New Features

1. **Metadata fields** - Flexible JSONB storage on Users, Classes, and Recommendations
2. **Enrollment management** - Proper enrollment tracking with status
3. **Grade percentages** - Automatic percentage calculation
4. **Assessment details** - `assessmentKind` and `assessmentName`
5. **Timestamp tracking** - All entities have `created_at` and `updated_at`
6. **Better error messages** - Detailed, helpful error responses
7. **Swagger documentation** - Interactive API docs at `/swagger-ui.html`

## Code Examples

### Creating a User (Old vs New)

**Old:**
```bash
curl -X POST http://localhost:8080/api/usuarios \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "John Doe",
    "email": "john@example.com",
    "rol": "profesor",
    "password": "secret123"
  }'
```

**New:**
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "authUserId": "550e8400-e29b-41d4-a716-446655440000",
    "fullName": "John Doe",
    "email": "john@example.com",
    "role": "TEACHER"
  }'
```

### Querying Classes (Old vs New)

**Old:**
```bash
curl http://localhost:8080/api/clases/profesor/123
```

**New:**
```bash
curl "http://localhost:8080/api/classes?teacherId=550e8400-e29b-41d4-a716-446655440000"
```

## Support

For questions or issues with migration, please contact the development team or refer to the full API documentation at `/swagger-ui.html`.

## Changelog

### Version 2.0.0 (October 2024)

- Complete API refactoring
- English naming convention
- UUID primary keys
- Clean architecture implementation
- Supabase Auth integration
- Improved error handling
- Comprehensive validation
- JSONB metadata fields
- Proper enrollment management
- Enhanced documentation


