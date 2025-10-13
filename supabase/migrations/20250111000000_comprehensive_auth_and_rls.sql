-- ============================================================================
-- Comprehensive Authentication and Row Level Security Migration
-- ============================================================================
-- This migration ensures all tables have proper auth integration and RLS policies
-- for a secure multi-tenant student performance management system.
--
-- Author: AIClass API Team
-- Date: 2025-01-11
-- ============================================================================

-- Set timeout to prevent lock issues
SET lock_timeout = '15s';

-- ============================================================================
-- SECTION 1: Custom Functions for RLS
-- ============================================================================

-- Function to get current user's role
CREATE OR REPLACE FUNCTION public.get_current_user_role()
RETURNS text
LANGUAGE sql
SECURITY DEFINER
STABLE
AS $$
  SELECT role::text 
  FROM public.users 
  WHERE auth_user_id = auth.uid()
  LIMIT 1;
$$;

-- Function to check if current user is a teacher
CREATE OR REPLACE FUNCTION public.is_teacher()
RETURNS boolean
LANGUAGE sql
SECURITY DEFINER
STABLE
AS $$
  SELECT EXISTS (
    SELECT 1 
    FROM public.users 
    WHERE auth_user_id = auth.uid() 
    AND role = 'teacher'
  );
$$;

-- Function to check if current user is a student
CREATE OR REPLACE FUNCTION public.is_student()
RETURNS boolean
LANGUAGE sql
SECURITY DEFINER
STABLE
AS $$
  SELECT EXISTS (
    SELECT 1 
    FROM public.users 
    WHERE auth_user_id = auth.uid() 
    AND role = 'student'
  );
$$;

-- Function to get current user's internal ID
CREATE OR REPLACE FUNCTION public.get_current_user_id()
RETURNS uuid
LANGUAGE sql
SECURITY DEFINER
STABLE
AS $$
  SELECT id 
  FROM public.users 
  WHERE auth_user_id = auth.uid()
  LIMIT 1;
$$;

-- ============================================================================
-- SECTION 2: Ensure auth_user_id exists and is properly indexed
-- ============================================================================

-- Ensure users table has auth_user_id (idempotent)
DO $$ 
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns 
    WHERE table_schema = 'public' 
    AND table_name = 'users' 
    AND column_name = 'auth_user_id'
  ) THEN
    ALTER TABLE public.users ADD COLUMN auth_user_id uuid UNIQUE NOT NULL;
  END IF;
END $$;

-- Create index for performance (idempotent)
CREATE INDEX IF NOT EXISTS idx_users_auth_user_id ON public.users(auth_user_id);

-- Ensure timestamps exist on users table
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns 
    WHERE table_schema = 'public' 
    AND table_name = 'users' 
    AND column_name = 'created_at'
  ) THEN
    ALTER TABLE public.users ADD COLUMN created_at timestamp with time zone NOT NULL DEFAULT now();
  END IF;
  
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns 
    WHERE table_schema = 'public' 
    AND table_name = 'users' 
    AND column_name = 'updated_at'
  ) THEN
    ALTER TABLE public.users ADD COLUMN updated_at timestamp with time zone NOT NULL DEFAULT now();
  END IF;
END $$;

-- ============================================================================
-- SECTION 3: Enable RLS on all tables
-- ============================================================================

ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.subjects ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.classes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.enrollments ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.grades ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.ai_recommendations ENABLE ROW LEVEL SECURITY;

-- ============================================================================
-- SECTION 4: RLS Policies for USERS table
-- ============================================================================

-- Drop existing policies if they exist
DROP POLICY IF EXISTS "users_select_own" ON public.users;
DROP POLICY IF EXISTS "users_insert_own" ON public.users;
DROP POLICY IF EXISTS "users_update_own" ON public.users;
DROP POLICY IF EXISTS "users_delete_own" ON public.users;
DROP POLICY IF EXISTS "users_teachers_view_all" ON public.users;

-- Users can view their own profile
CREATE POLICY "users_select_own" ON public.users
  FOR SELECT
  TO authenticated
  USING (auth_user_id = auth.uid());

-- Teachers can view all users (needed for class management)
CREATE POLICY "users_teachers_view_all" ON public.users
  FOR SELECT
  TO authenticated
  USING (public.is_teacher());

-- Users can insert their own record (during registration)
CREATE POLICY "users_insert_own" ON public.users
  FOR INSERT
  TO authenticated
  WITH CHECK (auth_user_id = auth.uid());

-- Users can update their own profile
CREATE POLICY "users_update_own" ON public.users
  FOR UPDATE
  TO authenticated
  USING (auth_user_id = auth.uid())
  WITH CHECK (auth_user_id = auth.uid());

-- Users can delete their own profile
CREATE POLICY "users_delete_own" ON public.users
  FOR DELETE
  TO authenticated
  USING (auth_user_id = auth.uid());

-- ============================================================================
-- SECTION 5: RLS Policies for SUBJECTS table
-- ============================================================================

DROP POLICY IF EXISTS "subjects_all_authenticated_view" ON public.subjects;
DROP POLICY IF EXISTS "subjects_teachers_manage" ON public.subjects;

-- All authenticated users can view subjects
CREATE POLICY "subjects_all_authenticated_view" ON public.subjects
  FOR SELECT
  TO authenticated
  USING (true);

-- Only teachers can create/update/delete subjects
CREATE POLICY "subjects_teachers_manage" ON public.subjects
  FOR ALL
  TO authenticated
  USING (public.is_teacher())
  WITH CHECK (public.is_teacher());

-- ============================================================================
-- SECTION 6: RLS Policies for CLASSES table
-- ============================================================================

DROP POLICY IF EXISTS "classes_teacher_full_access" ON public.classes;
DROP POLICY IF EXISTS "classes_student_view_enrolled" ON public.classes;

-- Teachers can manage their own classes
CREATE POLICY "classes_teacher_full_access" ON public.classes
  FOR ALL
  TO authenticated
  USING (
    teacher_user_id = public.get_current_user_id()
  )
  WITH CHECK (
    teacher_user_id = public.get_current_user_id()
  );

-- Students can view classes they're enrolled in
CREATE POLICY "classes_student_view_enrolled" ON public.classes
  FOR SELECT
  TO authenticated
  USING (
    public.is_student() AND
    id IN (
      SELECT class_id 
      FROM public.enrollments 
      WHERE student_user_id = public.get_current_user_id()
      AND status = 'active'
    )
  );

-- ============================================================================
-- SECTION 7: RLS Policies for ENROLLMENTS table
-- ============================================================================

DROP POLICY IF EXISTS "enrollments_teacher_manage_own_classes" ON public.enrollments;
DROP POLICY IF EXISTS "enrollments_student_view_own" ON public.enrollments;

-- Teachers can manage enrollments for their classes
CREATE POLICY "enrollments_teacher_manage_own_classes" ON public.enrollments
  FOR ALL
  TO authenticated
  USING (
    public.is_teacher() AND
    class_id IN (
      SELECT id 
      FROM public.classes 
      WHERE teacher_user_id = public.get_current_user_id()
    )
  )
  WITH CHECK (
    public.is_teacher() AND
    class_id IN (
      SELECT id 
      FROM public.classes 
      WHERE teacher_user_id = public.get_current_user_id()
    )
  );

-- Students can view their own enrollments
CREATE POLICY "enrollments_student_view_own" ON public.enrollments
  FOR SELECT
  TO authenticated
  USING (
    student_user_id = public.get_current_user_id()
  );

-- ============================================================================
-- SECTION 8: RLS Policies for GRADES table
-- ============================================================================

DROP POLICY IF EXISTS "grades_teacher_manage_own_classes" ON public.grades;
DROP POLICY IF EXISTS "grades_student_view_own" ON public.grades;

-- Teachers can manage grades for their classes
CREATE POLICY "grades_teacher_manage_own_classes" ON public.grades
  FOR ALL
  TO authenticated
  USING (
    public.is_teacher() AND
    class_id IN (
      SELECT id 
      FROM public.classes 
      WHERE teacher_user_id = public.get_current_user_id()
    )
  )
  WITH CHECK (
    public.is_teacher() AND
    class_id IN (
      SELECT id 
      FROM public.classes 
      WHERE teacher_user_id = public.get_current_user_id()
    )
  );

-- Students can view their own grades
CREATE POLICY "grades_student_view_own" ON public.grades
  FOR SELECT
  TO authenticated
  USING (
    student_user_id = public.get_current_user_id()
  );

-- ============================================================================
-- SECTION 9: RLS Policies for AI_RECOMMENDATIONS table
-- ============================================================================

DROP POLICY IF EXISTS "recommendations_view_own" ON public.ai_recommendations;
DROP POLICY IF EXISTS "recommendations_teacher_manage_own_classes" ON public.ai_recommendations;

-- Users can view recommendations addressed to them
CREATE POLICY "recommendations_view_own" ON public.ai_recommendations
  FOR SELECT
  TO authenticated
  USING (
    recipient_user_id = public.get_current_user_id()
  );

-- Teachers can create/manage recommendations for their classes
CREATE POLICY "recommendations_teacher_manage_own_classes" ON public.ai_recommendations
  FOR ALL
  TO authenticated
  USING (
    public.is_teacher() AND
    class_id IN (
      SELECT id 
      FROM public.classes 
      WHERE teacher_user_id = public.get_current_user_id()
    )
  )
  WITH CHECK (
    public.is_teacher() AND
    class_id IN (
      SELECT id 
      FROM public.classes 
      WHERE teacher_user_id = public.get_current_user_id()
    )
  );

-- ============================================================================
-- SECTION 10: Grant necessary permissions
-- ============================================================================

-- Grant usage on functions to authenticated users
GRANT EXECUTE ON FUNCTION public.get_current_user_role() TO authenticated;
GRANT EXECUTE ON FUNCTION public.is_teacher() TO authenticated;
GRANT EXECUTE ON FUNCTION public.is_student() TO authenticated;
GRANT EXECUTE ON FUNCTION public.get_current_user_id() TO authenticated;

-- ============================================================================
-- SECTION 11: Create indexes for RLS performance
-- ============================================================================

-- Indexes to support RLS queries efficiently
CREATE INDEX IF NOT EXISTS idx_classes_teacher_user_id ON public.classes(teacher_user_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_student_user_id ON public.enrollments(student_user_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_class_id ON public.enrollments(class_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_status ON public.enrollments(status);
CREATE INDEX IF NOT EXISTS idx_grades_student_user_id ON public.grades(student_user_id);
CREATE INDEX IF NOT EXISTS idx_grades_class_id ON public.grades(class_id);
CREATE INDEX IF NOT EXISTS idx_ai_recommendations_recipient_user_id ON public.ai_recommendations(recipient_user_id);
CREATE INDEX IF NOT EXISTS idx_ai_recommendations_class_id ON public.ai_recommendations(class_id);

-- ============================================================================
-- MIGRATION COMPLETE
-- ============================================================================

-- Verification queries (comment out in production)
-- SELECT 'RLS enabled on users:' as check, pg_class.relrowsecurity 
-- FROM pg_class WHERE relname = 'users';
-- SELECT 'Total policies created:' as check, count(*) 
-- FROM pg_policies WHERE schemaname = 'public';

COMMENT ON FUNCTION public.get_current_user_role() IS 'Returns the role of the currently authenticated user';
COMMENT ON FUNCTION public.is_teacher() IS 'Returns true if the current user is a teacher';
COMMENT ON FUNCTION public.is_student() IS 'Returns true if the current user is a student';
COMMENT ON FUNCTION public.get_current_user_id() IS 'Returns the internal UUID of the currently authenticated user';

