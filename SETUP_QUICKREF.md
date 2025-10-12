# AIClass API - Setup Quick Reference Card

**⏱️ Total Time**: ~10 minutes

---

## 1️⃣ Get Supabase Credentials (5 min)

### Dashboard → Project Settings → API
```
✅ URL:              https://xxx.supabase.co
✅ Anon Key:         eyJhbGc...
✅ Service Role Key: eyJhbGc...
```

### Dashboard → Project Settings → API → JWT Settings (scroll down)
```
✅ JWT Secret:       abc123def456...
```

### Dashboard → Project Settings → Database
```
✅ Connection String (Session Pooler):
   postgresql://postgres.xxx:password@aws-0-us-east-0.pooler.supabase.com:5432/postgres
```

### Dashboard → Authentication → Providers → Email
```
✅ Uncheck "Confirm email" (dev only!)
```

---

## 2️⃣ Configure Application (2 min)

```bash
# Copy template
cp src/main/resources/application-local.properties.template \
   src/main/resources/application-local.properties

# Edit file
nano src/main/resources/application-local.properties
```

**Replace these placeholders:**
- `YOUR_PASSWORD_URL_ENCODED` → URL-encode password (spaces = %20)
- `YOUR_PROJECT_REF` → Your project reference (e.g., `jrhpocpbeshnbnviehuq`)
- `YOUR_DATABASE_PASSWORD` → Your database password
- `YOUR_JWT_SECRET_HERE` → JWT Secret from step 1
- `YOUR_ANON_KEY_HERE` → Anon Key from step 1
- `YOUR_SERVICE_ROLE_KEY_HERE` → Service Role Key from step 1

---

## 3️⃣ Run Migrations (1 min)

```bash
# Direct connection (port 6543) for migrations
psql "postgresql://postgres.YOUR_REF:YOUR_PASSWORD@aws-0-us-east-0.pooler.supabase.com:6543/postgres" \
  -f supabase/migrations/20250111000000_comprehensive_auth_and_rls.sql

psql "postgresql://postgres.YOUR_REF:YOUR_PASSWORD@aws-0-us-east-0.pooler.supabase.com:6543/postgres" \
  -f supabase/migrations/20250111000001_auto_create_user_profile.sql
```

**Alternative**: Copy-paste SQL into Supabase SQL Editor

---

## 4️⃣ Start Application (1 min)

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

**Success indicators:**
```
✅ Tomcat started on port 8080 (http)
✅ Started AiclassApplication in X.XXX seconds
```

---

## 5️⃣ Test Authentication (1 min)

### Option A: Postman
1. Import `AIClass_API_v2_with_Auth.postman_collection.json`
2. Set collection variables:
   - `supabase_url`: Your Supabase URL
   - `supabase_anon_key`: Your anon key
3. Run "Signup (Supabase)" with:
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
4. Token auto-saves! Try "Get All Users"

### Option B: Swagger UI
1. Open http://localhost:8080/swagger-ui.html
2. Click "Authorize" button
3. Enter: `Bearer <your-jwt-token>`
4. Try any endpoint

---

## 🔧 Quick Troubleshooting

### Connection Refused
```bash
# Check if port is correct (5432 for Session Pooler, 6543 for Direct)
# URL-encode password: spaces = %20
```

### JWT Validation Fails
```bash
# Verify JWT Secret (not the anon key!)
# Check token not expired
# Format: Authorization: Bearer <token>
```

### 401 Unauthorized
```bash
# Confirm email in dashboard OR disable confirmation
# Check user exists in both auth.users and public.users
# Verify role is lowercase: "teacher" not "TEACHER"
```

---

## 📚 Need More Help?

- **Detailed Guide**: [AUTHENTICATION_GUIDE.md](AUTHENTICATION_GUIDE.md)
- **Testing Guide**: [AUTH_TESTING_GUIDE.md](AUTH_TESTING_GUIDE.md)
- **Full README**: [README.md](README.md)

---

**🎯 Quick Test Command**
```bash
curl http://localhost:8080/swagger-ui.html
# Should load Swagger UI
```

**🚀 You're ready to code!**

