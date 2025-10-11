# AIClass API v2.0 - Postman Testing Guide

## 📥 Import Collection & Environment

### Step 1: Import the Collection
1. Open Postman
2. Click **Import** button (top left)
3. Select the file: `AIClass_API_v2.postman_collection.json`
4. Click **Import**

### Step 2: Import the Environment
1. Click **Import** again
2. Select the file: `AIClass_API_Local.postman_environment.json`
3. Click **Import**

### Step 3: Select the Environment
1. In the top right corner, click the environment dropdown
2. Select **"AIClass API - Local"**

## 🔑 Key Changes from v1.x

| v1.x (Old) | v2.0 (New) | Notes |
|------------|------------|-------|
| `/api/usuarios` | `/api/users` | All endpoints now in English |
| `/api/materias` | `/api/subjects` | Subject management |
| `/api/clases` | `/api/classes` | Class sections |
| N/A | `/api/enrollments` | **New!** Student enrollment system |
| `/api/notas` | `/api/grades` | Grade management |
| `/api/recomendaciones` | `/api/recommendations` | AI recommendations |

### Response Format Change

**Old (v1.x):**
```json
{
  "id": 1,
  "nombre": "John",
  "rol": "profesor"
}
```

**New (v2.0):**
```json
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "id": "b2ff3b86-5c6a-4c19-826e-61696649c4e8",
    "fullName": "John",
    "role": "TEACHER"
  },
  "timestamp": "2025-10-11T14:00:00Z"
}
```

## 📚 Complete Endpoint Reference

### 👤 Users (`/api/users`)
- `GET /api/users` - Get all users
- `GET /api/users?role=TEACHER` - Filter by role
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users/email/{email}` - Get user by email
- `GET /api/users/auth/{authUserId}` - Get user by Supabase Auth ID
- `POST /api/users` - Create new user
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

### 📚 Subjects (`/api/subjects`)
- `GET /api/subjects` - Get all subjects
- `GET /api/subjects/{id}` - Get subject by ID
- `GET /api/subjects/code/{code}` - Get subject by code
- `POST /api/subjects` - Create new subject
- `PUT /api/subjects/{id}` - Update subject
- `DELETE /api/subjects/{id}` - Delete subject

### 🏫 Classes (`/api/classes`)
- `GET /api/classes` - Get all classes
- `GET /api/classes?teacherId={id}` - Filter by teacher
- `GET /api/classes?subjectId={id}` - Filter by subject
- `GET /api/classes?year={year}&semester={semester}` - Filter by year/semester
- `GET /api/classes/{id}` - Get class by ID
- `POST /api/classes` - Create new class
- `PUT /api/classes/{id}` - Update class
- `DELETE /api/classes/{id}` - Delete class

### 👨‍🎓 Enrollments (`/api/enrollments`) **NEW!**
- `GET /api/enrollments` - Get all enrollments
- `GET /api/enrollments?classId={id}` - Filter by class
- `GET /api/enrollments?studentId={id}` - Filter by student
- `GET /api/enrollments?status=ACTIVE` - Filter by status
- `GET /api/enrollments/{id}` - Get enrollment by ID
- `POST /api/enrollments` - Enroll student in class
- `PATCH /api/enrollments/{id}` - Update enrollment status
- `DELETE /api/enrollments/{id}` - Delete enrollment

### 📝 Grades (`/api/grades`)
- `GET /api/grades` - Get all grades
- `GET /api/grades?classId={id}` - Filter by class
- `GET /api/grades?studentId={id}` - Filter by student
- `GET /api/grades/{id}` - Get grade by ID
- `POST /api/grades` - Create new grade
- `PUT /api/grades/{id}` - Update grade
- `DELETE /api/grades/{id}` - Delete grade

### 🤖 AI Recommendations (`/api/recommendations`)
- `GET /api/recommendations` - Get all recommendations
- `GET /api/recommendations?recipientId={id}` - Filter by recipient
- `GET /api/recommendations?classId={id}` - Filter by class
- `GET /api/recommendations?audience=STUDENT` - Filter by audience
- `GET /api/recommendations/{id}` - Get recommendation by ID
- `POST /api/recommendations` - Create new recommendation
- `DELETE /api/recommendations/{id}` - Delete recommendation

## 🎯 Quick Test Flow

### 1. Get Existing Users
```
GET /api/users
```
Copy a user ID to use in next steps.

### 2. Create a Subject
```
POST /api/subjects
{
  "code": "CS101",
  "name": "Introduction to Computer Science",
  "description": "Fundamental concepts",
  "credits": 4
}
```
Copy the returned `id` to use as `subject_id`.

### 3. Create a Class
```
POST /api/classes
{
  "subjectId": "{{subject_id}}",
  "teacherId": "{{user_id}}",
  "year": 2025,
  "semester": "SPRING",
  "groupCode": "A",
  "schedule": "Mon/Wed/Fri 10:00-11:30"
}
```
Copy the returned `id` to use as `class_id`.

### 4. Enroll a Student
```
POST /api/enrollments
{
  "classId": "{{class_id}}",
  "studentId": "{{student_id}}",
  "enrollmentStatus": "ACTIVE"
}
```

### 5. Create a Grade
```
POST /api/grades
{
  "classId": "{{class_id}}",
  "studentId": "{{student_id}}",
  "assessmentKind": "exam",
  "assessmentName": "Midterm",
  "score": 85.5,
  "maxScore": 100
}
```

### 6. Create a Recommendation
```
POST /api/recommendations
{
  "recipientId": "{{student_id}}",
  "classId": "{{class_id}}",
  "audience": "STUDENT",
  "message": "Keep up the great work!"
}
```

## 🔧 Enum Values Reference

### UserRole
- `TEACHER`
- `STUDENT`

### Semester
- `SPRING` (spring)
- `SUMMER` (summer)
- `FALL` (fall)
- `WINTER` (winter)

### EnrollmentStatus
- `ACTIVE` (active)
- `DROPPED` (dropped)
- `COMPLETED` (completed)

### RecommendationAudience
- `TEACHER` (teacher)
- `STUDENT` (student)

**Note:** You can use either uppercase (in JSON requests) or lowercase (database values). The API handles conversion automatically.

## 🐛 Common Errors

### Error: "No static resource api/usuarios"
**Problem:** Using old v1.x endpoint  
**Solution:** Use `/api/users` instead

### Error: "No enum constant UserRole.profesor"
**Problem:** Using old Spanish enum values  
**Solution:** Use `TEACHER` or `STUDENT` (uppercase)

### Error: "Port 8080 already in use"
**Problem:** Another instance is running  
**Solution:** Kill the process: `lsof -ti:8080 | xargs kill -9`

## 📖 Additional Resources

- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **OpenAPI Spec**: http://localhost:8080/v3/api-docs
- **README**: See `README.md` for complete documentation

## 🆘 Support

If you encounter issues:
1. Check the application is running: `curl http://localhost:8080/api/users`
2. Verify environment variables are set: `source setup-env.sh`
3. Check Swagger UI for interactive testing
4. Review server logs for error messages

---

**Version**: 2.0.0  
**Last Updated**: October 2025

