-- ============================================================================
-- Auto-Create User Profile Trigger
-- ============================================================================
-- This trigger automatically creates a row in public.users when a new user
-- signs up via Supabase Auth. This eliminates the need for a separate
-- API call to create the user profile.
--
-- The trigger:
-- 1. Fires after a new row is inserted in auth.users
-- 2. Creates a corresponding row in public.users with auth_user_id
-- 3. Copies email from auth.users to public.users
-- 4. Sets a default role (can be overridden later)
--
-- Author: AIClass API Team
-- Date: 2025-01-11
-- ============================================================================

-- Function to handle new user creation
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
  -- Insert new user profile into public.users
  -- Convert role to lowercase to match database constraint
  -- Default role is 'student' - can be changed via API or admin panel
  INSERT INTO public.users (auth_user_id, email, full_name, role, metadata)
  VALUES (
    NEW.id,                                          -- auth_user_id from auth.users.id
    NEW.email,                                       -- email from auth.users
    COALESCE(NEW.raw_user_meta_data->>'full_name', 
             NEW.raw_user_meta_data->>'name',
             NEW.email),                             -- full_name from metadata or email
    LOWER(COALESCE(
      NEW.raw_user_meta_data->>'role',
      'student'
    ))::text,                                        -- role from metadata (converted to lowercase) or default to student
    COALESCE(NEW.raw_user_meta_data, '{}'::jsonb)   -- copy all metadata
  );
  
  RETURN NEW;
END;
$$;

-- Create trigger that fires after user signup
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
  AFTER INSERT ON auth.users
  FOR EACH ROW
  EXECUTE FUNCTION public.handle_new_user();

-- Grant execute permission on the function
GRANT EXECUTE ON FUNCTION public.handle_new_user() TO postgres, anon, authenticated, service_role;

-- Comment the function for documentation
COMMENT ON FUNCTION public.handle_new_user() IS 
  'Automatically creates a user profile in public.users when a new user signs up via Supabase Auth. 
   Role is converted to lowercase to match database constraint (accepts any case on input).
   Default role is student but can be set via raw_user_meta_data during signup.';

-- ============================================================================
-- Usage Examples
-- ============================================================================

-- Frontend signup with metadata (React/JS):
/*
const { data, error } = await supabase.auth.signUp({
  email: 'teacher@university.edu',
  password: 'secure_password_123',
  options: {
    data: {
      full_name: 'Dr. Jane Smith',
      role: 'teacher'  // Accepts any case, stored as lowercase
                        // 'teacher', 'TEACHER', or 'Teacher' all work
    }
  }
})
*/

-- Backend admin creation with specific role:
/*
-- Using service role key to create a teacher
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
  updated_at
) VALUES (
  '00000000-0000-0000-0000-000000000000',
  gen_random_uuid(),
  'authenticated',
  'authenticated',
  'admin@university.edu',
  crypt('password123', gen_salt('bf')),
  now(),
  '{"full_name": "Admin User", "role": "teacher"}'::jsonb,
  now(),
  now()
);
-- Trigger will automatically create the public.users record
*/

-- ============================================================================
-- Testing
-- ============================================================================

-- To test this trigger:
-- 1. Sign up a new user via Supabase Auth (frontend or dashboard)
-- 2. Check that a record was created in public.users:
--    SELECT * FROM public.users WHERE email = 'test@example.com';
-- 3. Verify auth_user_id matches auth.users.id:
--    SELECT 
--      auth.users.id as auth_id,
--      public.users.auth_user_id,
--      public.users.email,
--      public.users.role
--    FROM auth.users
--    JOIN public.users ON auth.users.id = public.users.auth_user_id
--    WHERE auth.users.email = 'test@example.com';

-- ============================================================================
-- Rollback (if needed)
-- ============================================================================

-- To remove this trigger:
-- DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
-- DROP FUNCTION IF EXISTS public.handle_new_user();

