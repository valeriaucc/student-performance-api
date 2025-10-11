-- Migration Script: Update enum values and constraints from v1.x to v2.0
-- Run this in Supabase SQL Editor or via psql

BEGIN;

-- 1. DROP OLD CONSTRAINTS
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;
ALTER TABLE classes DROP CONSTRAINT IF EXISTS classes_semester_check;
ALTER TABLE enrollments DROP CONSTRAINT IF EXISTS enrollments_enrollment_status_check;
ALTER TABLE ai_recommendations DROP CONSTRAINT IF EXISTS ai_recommendations_audience_check;

-- 2. UPDATE DATA TO NEW ENUM VALUES

-- Update UserRole: profesor/teacher → TEACHER, estudiante/student → STUDENT
UPDATE users 
SET role = CASE 
    WHEN role IN ('profesor', 'teacher') THEN 'TEACHER'
    WHEN role IN ('estudiante', 'student') THEN 'STUDENT'
    ELSE role
END;

-- Update Semester: 1/first/PRIMERO → FIRST, 2/second/SEGUNDO → SECOND
UPDATE classes 
SET semester = CASE 
    WHEN semester IN ('1', 'first', 'PRIMERO') THEN 'FIRST'
    WHEN semester IN ('2', 'second', 'SEGUNDO') THEN 'SECOND'
    WHEN semester IN ('summer', 'VERANO') THEN 'SUMMER'
    ELSE semester
END
WHERE semester IS NOT NULL;

-- Update EnrollmentStatus: activo/active → ACTIVE
UPDATE enrollments 
SET enrollment_status = CASE 
    WHEN enrollment_status IN ('activo', 'active') THEN 'ACTIVE'
    WHEN enrollment_status IN ('retirado', 'dropped') THEN 'DROPPED'
    WHEN enrollment_status IN ('completado', 'completed') THEN 'COMPLETED'
    ELSE enrollment_status
END
WHERE enrollment_status IS NOT NULL;

-- Update RecommendationAudience: profesor/teacher → TEACHER, estudiante/student → STUDENT
UPDATE ai_recommendations 
SET audience = CASE 
    WHEN audience IN ('profesor', 'teacher') THEN 'TEACHER'
    WHEN audience IN ('estudiante', 'student') THEN 'STUDENT'
    ELSE audience
END
WHERE audience IS NOT NULL;

-- 3. ADD NEW CONSTRAINTS WITH CORRECT ENUM VALUES

ALTER TABLE users 
ADD CONSTRAINT users_role_check 
CHECK (role IN ('TEACHER', 'STUDENT'));

ALTER TABLE classes 
ADD CONSTRAINT classes_semester_check 
CHECK (semester IN ('FIRST', 'SECOND', 'SUMMER'));

ALTER TABLE enrollments 
ADD CONSTRAINT enrollments_enrollment_status_check 
CHECK (enrollment_status IN ('ACTIVE', 'DROPPED', 'COMPLETED'));

ALTER TABLE ai_recommendations 
ADD CONSTRAINT ai_recommendations_audience_check 
CHECK (audience IN ('TEACHER', 'STUDENT'));

-- 4. VERIFY THE MIGRATION
SELECT 'Users by role:' as info, role, COUNT(*) as count FROM users GROUP BY role;
SELECT 'Classes by semester:' as info, semester, COUNT(*) as count FROM classes WHERE semester IS NOT NULL GROUP BY semester;
SELECT 'Enrollments by status:' as info, enrollment_status, COUNT(*) as count FROM enrollments WHERE enrollment_status IS NOT NULL GROUP BY enrollment_status;
SELECT 'Recommendations by audience:' as info, audience, COUNT(*) as count FROM ai_recommendations WHERE audience IS NOT NULL GROUP BY audience;

COMMIT;

-- Success message
SELECT '✅ Migration completed successfully!' as status;

