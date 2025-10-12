# Authentication Testing Guide

This guide walks you through testing the AIClass API authentication system using **Postman** and **Swagger UI**.

## Prerequisites

✅ Application is running: `./mvnw spring-boot:run -Dspring-boot.run.profiles=local`  
✅ Database migrations have been applied  
✅ Supabase project is configured

---

## Option 1: Testing with Postman (Recommended)

### Step 1: Import the Collection

1. Open Postman
2. Click **Import** button
3. Select `AIClass_API_v2_with_Auth.postman_collection.json`
4. The collection will be imported with authentication pre-configured

### Step 2: Create Test Users in Supabase

You have two options to create test users:

#### Option A: Using Supabase SQL Editor (Quick)

1. Go to your Supabase Dashboard → **SQL Editor**
2. Run this SQL to create test users:

```sql
-- Create a test teacher user
INSERT INTO auth.users (
  id, 
  instance_id,
  email, 
  encrypted_password,
  email_confirmed_at,
  raw_app_meta_data,
  raw_user_meta_data,
  aud,
  role,
  created_at,
  updated_at,
  confirmation_token,
  recovery_token,
  email_change_token_new
)
VALUES (
  gen_random_uuid(),
  '00000000-0000-0000-0000-000000000000',
  'teacher@test.com',
  crypt('password123', gen_salt('bf')),
  now(),
  '{"provider":"email","providers":["email"]}'::jsonb,
  '{"full_name":"Test Teacher","role":"TEACHER"}'::jsonb,
  'authenticated',
  'authenticated',
  now(),
  now(),
  '',
  '',
  ''
);

-- Create a test student user
INSERT INTO auth.users (
  id, 
  instance_id,
  email, 
  encrypted_password,
  email_confirmed_at,
  raw_app_meta_data,
  raw_user_meta_data,
  aud,
  role,
  created_at,
  updated_at,
  confirmation_token,
  recovery_token,
  email_change_token_new
)
VALUES (
  gen_random_uuid(),
  '00000000-0000-0000-0000-000000000000',
  'student@test.com',
  crypt('password123', gen_salt('bf')),
  now(),
  '{"provider":"email","providers":["email"]}'::jsonb,
  '{"full_name":"Test Student","role":"STUDENT"}'::jsonb,
  'authenticated',
  'authenticated',
  now(),
  now(),
  '',
  '',
  ''
);
```

3. **Important**: After creating users in `auth.users`, the trigger will automatically create corresponding `public.users` records!

#### Option B: Using Postman Signup Request (Easier)

1. Open the **🔐 Authentication** folder in Postman
2. Run the **"Signup (Supabase)"** request
3. The JWT token will be **automatically saved** to the collection variable
4. You're ready to test!

**Request body example:**
```json
{
  "email": "teacher@test.com",
  "password": "password123",
  "data": {
    "full_name": "Test Teacher",
    "role": "TEACHER"
  }
}
```

### Step 3: Login and Get JWT Token

1. Open **🔐 Authentication** → **Login (Supabase)**
2. Update the request body with your test user credentials:
   ```json
   {
     "email": "teacher@test.com",
     "password": "password123"
   }
   ```
3. Click **Send**
4. The response will contain your JWT token:
   ```json
   {
     "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     "token_type": "bearer",
     "expires_in": 3600,
     "user": {
       "id": "550e8400-e29b-41d4-a716-446655440000",
       "email": "teacher@test.com",
       ...
     }
   }
   ```
5. **The JWT token is automatically saved** to the `jwt_token` collection variable by the test script!
6. Check the **Console** to see the confirmation: `✅ JWT token saved to collection variable`

### Step 4: Test Authenticated Endpoints

Now all requests in the collection will automatically use the JWT token!

#### Test 1: Get All Users (TEACHER only)

1. Open **👤 Users** → **Get All Users**
2. Click **Send**
3. ✅ **Expected**: 200 OK with list of users
4. ❌ **If 403 Forbidden**: You're not logged in as a TEACHER

#### Test 2: Create User Profile

1. Open **👤 Users** → **Create User**
2. Update the body to use your auth user ID (from login response):
   ```json
   {
     "authUserId": "YOUR_AUTH_USER_ID",
     "fullName": "Test Teacher",
     "email": "teacher@test.com",
     "role": "TEACHER",
     "metadata": {
       "department": "Computer Science"
     }
   }
   ```
3. Click **Send**
4. ✅ **Expected**: 201 Created with user details

#### Test 3: Create a Subject (TEACHER only)

1. Open **📚 Subjects** → **Create Subject**
2. Click **Send**
3. ✅ **Expected**: 201 Created
4. Save the `id` from the response as `subject_id` variable

#### Test 4: Create a Class (TEACHER only)

1. Open **🏫 Classes** → **Create Class**
2. Make sure `subject_id` and `user_id` are set in variables
3. Click **Send**
4. ✅ **Expected**: 201 Created

### Step 5: Test Student Role (Role-Based Access Control)

1. Run **Login** request again with **student@test.com**
2. Try to create a subject (should fail)
3. ✅ **Expected**: 403 Forbidden
4. Try to view grades (should succeed if enrolled)

---

## Option 2: Testing with Swagger UI

### Step 1: Access Swagger UI

1. Make sure the application is running
2. Open your browser and go to: **http://localhost:8080/swagger-ui/index.html**
3. You should see the AIClass API documentation with a 🔓 **Authorize** button at the top right

### Step 2: Get JWT Token

Since you can't login directly from Swagger, you need to get a token first:

**Using cURL:**
```bash
curl -X POST 'https://jrhpocpbeshnbnviehuq.supabase.co/auth/v1/token?grant_type=password' \
  -H 'apikey: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImpyaHBvY3BiZXNobmJudmllaHVxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTkzNzY5NTIsImV4cCI6MjA3NDk1Mjk1Mn0.XlSLPs6MwFIV2N-Z_hnrYfGiGI3FJV-gRZkOWIW7S0U' \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "teacher@test.com",
    "password": "password123"
  }'
```

Copy the `access_token` from the response.

### Step 3: Authorize in Swagger

1. Click the 🔓 **Authorize** button (top right)
2. In the popup, enter: `Bearer YOUR_ACCESS_TOKEN`
   - Example: `Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`
3. Click **Authorize**
4. Click **Close**

### Step 4: Test Endpoints

Now you can test any endpoint:

1. Expand **Users** → **GET /api/users**
2. Click **Try it out**
3. Click **Execute**
4. ✅ **Expected**: 200 OK with user list

The Authorization header will be automatically included in all requests!

---

## Expected Results Summary

| Endpoint | Teacher | Student | Anonymous |
|----------|---------|---------|-----------|
| `GET /api/users` | ✅ 200 OK | ❌ 403 Forbidden | ❌ 401 Unauthorized |
| `POST /api/users` | ✅ 201 Created | ✅ 201 Created | ❌ 401 Unauthorized |
| `POST /api/subjects` | ✅ 201 Created | ❌ 403 Forbidden | ❌ 401 Unauthorized |
| `GET /api/subjects` | ✅ 200 OK | ✅ 200 OK | ❌ 401 Unauthorized |
| `POST /api/classes` | ✅ 201 Created | ❌ 403 Forbidden | ❌ 401 Unauthorized |
| `POST /api/grades` | ✅ 201 Created | ❌ 403 Forbidden | ❌ 401 Unauthorized |
| `GET /api/grades` | ✅ 200 OK (all) | ✅ 200 OK (own only) | ❌ 401 Unauthorized |

---

## Troubleshooting

### ❌ 401 Unauthorized

**Problem**: No JWT token or invalid token

**Solutions**:
1. Make sure you ran the **Login** request in Postman
2. Check that `jwt_token` variable is set (View → Show Postman Console)
3. Token might be expired (expires after 1 hour) - login again
4. In Swagger, make sure you included `Bearer` prefix

### ❌ 403 Forbidden

**Problem**: User doesn't have the required role

**Solutions**:
1. Check the user's role in the JWT token (decode it at jwt.io)
2. Make sure you created the user with the correct role in `user_metadata`
3. Login as a TEACHER to test teacher-only endpoints

### ❌ 500 Internal Server Error

**Problem**: Database or application error

**Solutions**:
1. Check application logs in the terminal
2. Make sure database migrations were applied
3. Verify database connection is working

### JWT Token Decode (Debug)

Go to **https://jwt.io/** and paste your token to see the claims:

```json
{
  "sub": "550e8400-e29b-41d4-a716-446655440000",
  "email": "teacher@test.com",
  "role": "authenticated",
  "user_metadata": {
    "full_name": "Test Teacher",
    "role": "TEACHER"
  },
  ...
}
```

The `user_metadata.role` should be `TEACHER` or `STUDENT`.

---

## Testing Checklist

- [ ] Application is running on http://localhost:8080
- [ ] Swagger UI is accessible
- [ ] Test users created (teacher and student)
- [ ] Can login and get JWT token
- [ ] JWT token is automatically saved in Postman
- [ ] Can access authenticated endpoints with valid token
- [ ] TEACHER can create subjects, classes, grades
- [ ] STUDENT cannot create subjects, classes, grades
- [ ] Anonymous requests return 401 Unauthorized
- [ ] Row Level Security is working (students see only their own data)

---

## Next Steps

After confirming authentication is working:

1. ✅ Test all CRUD operations for each entity
2. ✅ Test role-based access control (TEACHER vs STUDENT)
3. ✅ Test Row Level Security (students can only see their own data)
4. ✅ Test error responses (401, 403, 404, etc.)
5. ✅ Test with frontend application integration

---

## Quick Reference

**Supabase Project URL**: `https://jrhpocpbeshnbnviehuq.supabase.co`  
**Supabase Anon Key**: `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImpyaHBvY3BiZXNobmJudmllaHVxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTkzNzY5NTIsImV4cCI6MjA3NDk1Mjk1Mn0.XlSLPs6MwFIV2N-Z_hnrYfGiGI3FJV-gRZkOWIW7S0U`  
**API Base URL**: `http://localhost:8080`  
**Swagger UI**: `http://localhost:8080/swagger-ui/index.html`

**Test Credentials**:
- **Teacher**: `teacher@test.com` / `password123`
- **Student**: `student@test.com` / `password123`

