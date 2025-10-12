# 🚀 Complete Setup Instructions - From Zero to Production

This guide will walk you through setting up authentication for your AIClass API from scratch.

---

## 📋 Prerequisites

Before starting, ensure you have:

- ✅ Java 21 installed
- ✅ Maven installed (`mvn --version`)
- ✅ PostgreSQL client installed (`psql --version`)
- ✅ Access to your Supabase project dashboard
- ✅ Your database connection string from Supabase

---

## Step 1: Get Your Supabase Credentials

### 1.1 Login to Supabase Dashboard

Go to: https://app.supabase.com/

### 1.2 Select Your Project

Navigate to your project (the one with reference: `fwipiimnkxxcksvvspri`)

### 1.3 Get API Credentials

**Navigate to:** Project Settings (⚙️) → API

Copy these values:

```
Project URL: https://fwipiimnkxxcksvvspri.supabase.co
Project API URL: https://fwipiimnkxxcksvvspri.supabase.co
anon public key: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
service_role key: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9... (keep secret!)
```

### 1.4 Get Database Connection String

**Navigate to:** Project Settings (⚙️) → Database

Under "Connection string" → **Direct connection** (NOT Pooler):

```
Host: aws-0-us-east-2.pooler.supabase.com
Database: postgres
Port: 5432
User: postgres.fwipiimnkxxcksvvspri
Password: [Your password]
```

Or copy the full connection string:
```
postgresql://postgres.fwipiimnkxxcksvvspri:[YOUR-PASSWORD]@aws-0-us-east-2.pooler.supabase.com:5432/postgres
```

---

## Step 2: Configure Your Application

### 2.1 Update `application-local.properties`

Open the file: `src/main/resources/application-local.properties`

Replace the placeholder values with your actual Supabase credentials:

```properties
# Database Configuration - Use your actual Supabase connection
spring.datasource.url=jdbc:postgresql://aws-0-us-east-2.pooler.supabase.com:5432/postgres
spring.datasource.username=postgres.fwipiimnkxxcksvvspri
spring.datasource.password=YOUR_ACTUAL_PASSWORD
spring.datasource.driver-class-name=org.postgresql.Driver

# Hibernate Configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

# Server Configuration
server.port=8080

# Supabase Configuration - Replace with your actual values
supabase.jwt.jwk-set-uri=https://fwipiimnkxxcksvvspri.supabase.co/auth/v1/jwks
supabase.url=https://fwipiimnkxxcksvvspri.supabase.co
supabase.anon.key=YOUR_ANON_KEY_FROM_STEP_1.3
supabase.service.role.key=YOUR_SERVICE_ROLE_KEY_FROM_STEP_1.3

# CORS Configuration - Add your frontend URL
security.cors.allowed-origins=http://localhost:3000,http://localhost:5173,http://localhost:4200

# Logging - Enable security logging for debugging
logging.level.org.springframework.security=DEBUG
logging.level.com.viveek.aiclass.security=DEBUG
```

**Save the file.**

---

## Step 3: Run Database Migrations

### 3.1 Test Database Connection

First, verify you can connect to your database:

```bash
# Test connection (replace with your actual password)
psql "postgresql://postgres.fwipiimnkxxcksvvspri:YOUR_PASSWORD@aws-0-us-east-2.pooler.supabase.com:5432/postgres" -c "SELECT version();"
```

If this works, you should see PostgreSQL version information.

### 3.2 Run Migration #1 - RLS Policies and Security Functions

This creates Row Level Security policies for all tables.

```bash
# Navigate to your project directory
cd "/Users/santiago.torres/Library/CloudStorage/GoogleDrive-thdealer@gmail.com/My Drive/source/student-performance-api"

# Run the first migration
psql "postgresql://postgres.fwipiimnkxxcksvvspri:YOUR_PASSWORD@aws-0-us-east-2.pooler.supabase.com:5432/postgres" \
  -f supabase/migrations/20250111000000_comprehensive_auth_and_rls.sql
```

**Expected output:**
```
CREATE FUNCTION
CREATE FUNCTION
CREATE FUNCTION
CREATE FUNCTION
ALTER TABLE
ALTER TABLE
CREATE POLICY
CREATE POLICY
...
GRANT
CREATE INDEX
...
```

If you see errors, don't worry - check the troubleshooting section below.

### 3.3 Run Migration #2 - Auto-Create User Profile Trigger

This automatically creates a user profile when someone signs up via Supabase Auth.

```bash
# Run the second migration
psql "postgresql://postgres.fwipiimnkxxcksvvspri:YOUR_PASSWORD@aws-0-us-east-2.pooler.supabase.com:5432/postgres" \
  -f supabase/migrations/20250111000001_auto_create_user_profile.sql
```

**Expected output:**
```
CREATE FUNCTION
CREATE TRIGGER
GRANT
COMMENT
```

### 3.4 Verify Migrations Were Applied

```bash
# Check that RLS is enabled
psql "postgresql://postgres.fwipiimnkxxcksvvspri:YOUR_PASSWORD@aws-0-us-east-2.pooler.supabase.com:5432/postgres" -c "
SELECT tablename, rowsecurity 
FROM pg_tables 
WHERE schemaname = 'public' 
AND tablename IN ('users', 'classes', 'grades', 'enrollments', 'subjects', 'ai_recommendations');"
```

**Expected output:**
```
      tablename       | rowsecurity 
---------------------+-------------
 users               | t
 classes             | t
 grades              | t
 enrollments         | t
 subjects            | t
 ai_recommendations  | t
```

All should show `t` (true) for RLS enabled.

```bash
# Check that policies were created
psql "postgresql://postgres.fwipiimnkxxcksvvspri:YOUR_PASSWORD@aws-0-us-east-2.pooler.supabase.com:5432/postgres" -c "
SELECT schemaname, tablename, policyname 
FROM pg_policies 
WHERE schemaname = 'public' 
ORDER BY tablename, policyname;"
```

You should see multiple policies listed for each table.

```bash
# Check that the trigger was created
psql "postgresql://postgres.fwipiimnkxxcksvvspri:YOUR_PASSWORD@aws-0-us-east-2.pooler.supabase.com:5432/postgres" -c "
SELECT tgname, tgtype, tgenabled 
FROM pg_trigger 
WHERE tgname = 'on_auth_user_created';"
```

**Expected output:**
```
      tgname          | tgtype | tgenabled 
---------------------+--------+-----------
 on_auth_user_created |      5 | O
```

---

## Step 4: Build and Start Your Application

### 4.1 Clean and Build

```bash
# Clean previous builds
./mvnw clean

# Compile and package
./mvnw package -DskipTests
```

**Expected output:**
```
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

### 4.2 Start the Application

```bash
# Start Spring Boot
./mvnw spring-boot:run
```

**Wait for:**
```
... Tomcat started on port(s): 8080 (http)
... Started AiclassApplication in X.XXX seconds
```

**Leave this terminal running!** Open a new terminal for testing.

---

## Step 5: Test Authentication

### 5.1 Test Unauthenticated Access (Should Fail)

Open a new terminal and run:

```bash
# This should return 401 Unauthorized
curl -i http://localhost:8080/api/users
```

**Expected response:**
```
HTTP/1.1 401 
...
```

✅ **Good!** This means security is working - unauthenticated requests are blocked.

### 5.2 Create a Test User in Supabase

**Option A: Via Supabase Dashboard (Easiest)**

1. Go to Supabase Dashboard → Authentication → Users
2. Click "Add User" → "Create new user"
3. Fill in:
   - Email: `teacher@test.com`
   - Password: `TestPassword123!`
   - Auto Confirm User: ✅ (check this)
   - User Metadata: 
     ```json
     {
       "full_name": "Test Teacher",
       "role": "teacher"
     }
     ```
4. Click "Create user"

**Option B: Via SQL (Advanced)**

```bash
psql "postgresql://postgres.fwipiimnkxxcksvvspri:YOUR_PASSWORD@aws-0-us-east-2.pooler.supabase.com:5432/postgres" -c "
INSERT INTO auth.users (
  instance_id,
  id,
  aud,
  role,
  email,
  encrypted_password,
  email_confirmed_at,
  raw_user_meta_data,
  created_at,
  updated_at,
  confirmation_token,
  recovery_token
) VALUES (
  '00000000-0000-0000-0000-000000000000',
  gen_random_uuid(),
  'authenticated',
  'authenticated',
  'teacher@test.com',
  crypt('TestPassword123!', gen_salt('bf')),
  now(),
  '{\"full_name\": \"Test Teacher\", \"role\": \"teacher\"}'::jsonb,
  now(),
  now(),
  '',
  ''
) RETURNING id, email;
"
```

### 5.3 Get JWT Token

**Option A: Using Supabase REST API (Easiest)**

```bash
# Login to get JWT token
curl -X POST 'https://fwipiimnkxxcksvvspri.supabase.co/auth/v1/token?grant_type=password' \
  -H "apikey: YOUR_ANON_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "teacher@test.com",
    "password": "TestPassword123!"
  }'
```

**Response:**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "bearer",
  "expires_in": 3600,
  "refresh_token": "...",
  "user": { ... }
}
```

Copy the `access_token` value!

**Option B: Using JavaScript/Frontend (Production Way)**

```javascript
import { createClient } from '@supabase/supabase-js'

const supabase = createClient(
  'https://fwipiimnkxxcksvvspri.supabase.co',
  'YOUR_ANON_KEY'
)

const { data, error } = await supabase.auth.signInWithPassword({
  email: 'teacher@test.com',
  password: 'TestPassword123!'
})

console.log('JWT Token:', data.session.access_token)
```

### 5.4 Test Authenticated Access (Should Work)

```bash
# Replace YOUR_JWT_TOKEN with the actual token from step 5.3
export JWT_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# Test getting all users (teachers can do this)
curl -i http://localhost:8080/api/users \
  -H "Authorization: Bearer $JWT_TOKEN"
```

**Expected response:**
```
HTTP/1.1 200 
Content-Type: application/json

{
  "success": true,
  "data": [
    {
      "id": "...",
      "authUserId": "...",
      "email": "teacher@test.com",
      "fullName": "Test Teacher",
      "role": "TEACHER",
      ...
    }
  ],
  ...
}
```

✅ **Success!** Authentication is working!

### 5.5 Test Role-Based Authorization

```bash
# Test creating a class (only teachers can do this)
curl -X POST http://localhost:8080/api/classes \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "subjectId": "some-uuid",
    "teacherUserId": "your-user-uuid",
    "year": 2025,
    "semester": "SPRING",
    "groupCode": "A1"
  }'
```

If you get 404 for subject not found, that's fine - it means authorization passed! Create a subject first.

---

## Step 6: Test User Auto-Creation

### 6.1 Sign Up a New User

```bash
# Sign up a new student
curl -X POST 'https://fwipiimnkxxcksvvspri.supabase.co/auth/v1/signup' \
  -H "apikey: YOUR_ANON_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "student@test.com",
    "password": "StudentPassword123!",
    "data": {
      "full_name": "Test Student",
      "role": "student"
    }
  }'
```

### 6.2 Verify User Was Auto-Created in public.users

```bash
psql "postgresql://postgres.fwipiimnkxxcksvvspri:YOUR_PASSWORD@aws-0-us-east-2.pooler.supabase.com:5432/postgres" -c "
SELECT id, auth_user_id, email, full_name, role 
FROM public.users 
WHERE email = 'student@test.com';"
```

**Expected output:**
```
                  id                  |            auth_user_id            |      email       |   full_name   |  role   
--------------------------------------+------------------------------------+------------------+---------------+---------
 xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx | yyyyyyyy-yyyy-yyyy-yyyy-yyyyyyyy   | student@test.com | Test Student  | student
```

✅ **Perfect!** The trigger automatically created the user profile!

---

## Step 7: Configure Supabase Auth Settings (Optional but Recommended)

### 7.1 Go to Supabase Dashboard

Navigate to: Authentication → Settings

### 7.2 Configure Email Settings

- **Enable Email Signup**: ✅ (if you want users to sign up)
- **Enable Email Confirmations**: ✅ (recommended for production)
- **Site URL**: Set to your frontend URL (e.g., `http://localhost:3000`)
- **Redirect URLs**: Add allowed redirect URLs after login

### 7.3 Configure Email Templates (Optional)

Customize confirmation, password reset, and magic link email templates.

### 7.4 Configure OAuth Providers (Optional)

If you want Google/GitHub/etc. login:
1. Go to Authentication → Providers
2. Enable desired providers
3. Configure OAuth credentials from Google/GitHub/etc.

---

## 🎉 You're Done!

Your authentication system is now fully configured and running!

### What You Have Now:

✅ JWT authentication with Supabase  
✅ Role-based authorization (TEACHER/STUDENT)  
✅ Row Level Security on all tables  
✅ Auto-creation of user profiles  
✅ CORS configured for frontend  
✅ Production-ready security  

---

## 📋 Quick Reference

### Starting the Application

```bash
cd "/Users/santiago.torres/Library/CloudStorage/GoogleDrive-thdealer@gmail.com/My Drive/source/student-performance-api"
./mvnw spring-boot:run
```

### Getting a JWT Token

```bash
curl -X POST 'https://fwipiimnkxxcksvvspri.supabase.co/auth/v1/token?grant_type=password' \
  -H "apikey: YOUR_ANON_KEY" \
  -H "Content-Type: application/json" \
  -d '{"email": "teacher@test.com", "password": "TestPassword123!"}'
```

### Making Authenticated Requests

```bash
export JWT_TOKEN="your-jwt-token-here"

curl http://localhost:8080/api/users \
  -H "Authorization: Bearer $JWT_TOKEN"
```

---

## ⚠️ Troubleshooting

### Issue: "psql: command not found"

**Solution:** Install PostgreSQL client

```bash
# macOS
brew install postgresql

# Ubuntu/Debian
sudo apt-get install postgresql-client

# Windows
# Download from: https://www.postgresql.org/download/windows/
```

### Issue: "connection refused" or "timeout"

**Solution:** Check your connection string

1. Verify the connection string in Supabase Dashboard
2. Make sure you're using the **Direct connection** (not Pooler) for migrations
3. Check firewall/network settings

### Issue: "permission denied" when running migrations

**Solution:** You need superuser access

```bash
# Try using the service role key instead
# Or contact Supabase support to grant necessary permissions
```

### Issue: Migration says "relation already exists"

**Solution:** Some tables/functions already exist

This is OK! The migrations use `IF NOT EXISTS` and `CREATE OR REPLACE`, so they're idempotent. You can safely ignore these messages.

### Issue: Application won't start - "Bean creation error"

**Solution:** Check your `application-local.properties`

Make sure all Supabase properties are filled in correctly:
- `supabase.jwt.jwk-set-uri`
- `supabase.url`
- `supabase.anon.key`
- `security.cors.allowed-origins`

### Issue: "401 Unauthorized" even with token

**Possible causes:**

1. **Token expired** - Get a new token (tokens expire after 1 hour by default)
2. **Wrong JWK URI** - Verify `supabase.jwt.jwk-set-uri` is correct
3. **Token format** - Make sure header is `Authorization: Bearer <token>`

**Debug:**
```bash
# Decode your JWT to check expiration
echo "YOUR_JWT_TOKEN" | cut -d. -f2 | base64 -d | jq
```

### Issue: "403 Forbidden" with valid token

**Cause:** User doesn't have required role

**Solution:** Check user's role in database:

```bash
psql "YOUR_CONNECTION_STRING" -c "
SELECT email, role FROM public.users WHERE email = 'your-email@test.com';"
```

Update role if needed:

```bash
psql "YOUR_CONNECTION_STRING" -c "
UPDATE public.users SET role = 'teacher' WHERE email = 'your-email@test.com';"
```

---

## 📚 Next Steps

1. **Read the guides:**
   - [AUTHENTICATION_GUIDE.md](AUTHENTICATION_GUIDE.md) - Complete security documentation
   - [SUPABASE_AUTH_ALIGNMENT.md](SUPABASE_AUTH_ALIGNMENT.md) - How everything fits together

2. **Integrate with your frontend:**
   - Install `@supabase/supabase-js`
   - Implement login/signup UI
   - Store JWT token securely
   - Send token with all API requests

3. **Test thoroughly:**
   - Test signup flow
   - Test login flow
   - Test role-based access
   - Test RLS policies

4. **Prepare for production:**
   - Enable HTTPS
   - Configure email confirmations
   - Set up monitoring
   - Review security checklist in README

---

## 🆘 Need Help?

1. Check the [AUTHENTICATION_GUIDE.md](AUTHENTICATION_GUIDE.md) troubleshooting section
2. Review application logs for error details
3. Check Supabase Auth logs in dashboard
4. Verify all configuration values are correct

---

**You're all set! Happy coding! 🚀**

