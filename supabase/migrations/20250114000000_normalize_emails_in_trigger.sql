-- ============================================================================
-- Email Normalization in Auto-Create User Profile Trigger
-- ============================================================================
-- Updates the handle_new_user() trigger to normalize emails (lowercase, trim)
-- before inserting into public.users table.
--
-- This ensures consistency with Java application layer which also normalizes
-- emails using EmailUtils.normalizeEmail()
--
-- Author: AIClass API Team
-- Date: 2025-01-14
-- ============================================================================

-- Update function to handle new user creation with email normalization
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
  normalized_email text;
BEGIN
  -- Normalize email: lowercase and trim whitespace
  normalized_email := LOWER(TRIM(NEW.email));
  
  -- Insert new user profile into public.users
  -- Convert role to lowercase to match database constraint
  -- Default role is 'student' - can be changed via API or admin panel
  INSERT INTO public.users (auth_user_id, email, full_name, role, metadata)
  VALUES (
    NEW.id,                                          -- auth_user_id from auth.users.id
    normalized_email,                                -- normalized email (lowercase, trimmed)
    COALESCE(NEW.raw_user_meta_data->>'full_name', 
             NEW.raw_user_meta_data->>'name',
             normalized_email),                      -- full_name from metadata or normalized email
    LOWER(COALESCE(
      NEW.raw_user_meta_data->>'role',
      'student'
    ))::text,                                        -- role from metadata (converted to lowercase) or default to student
    COALESCE(NEW.raw_user_meta_data, '{}'::jsonb)   -- copy all metadata
  );
  
  RETURN NEW;
END;
$$;

-- Update comment to reflect email normalization
COMMENT ON FUNCTION public.handle_new_user() IS 
  'Automatically creates a user profile in public.users when a new user signs up via Supabase Auth. 
   Email is normalized (lowercase, trimmed) to ensure consistency.
   Role is converted to lowercase to match database constraint (accepts any case on input).
   Default role is student but can be set via raw_user_meta_data during signup.';

-- ============================================================================
-- Testing Email Normalization
-- ============================================================================

-- Test that emails are properly normalized:
-- 1. Sign up with email: " User@Example.COM  " (with spaces and mixed case)
-- 2. Verify stored as: "user@example.com" (lowercase, no spaces)
--    SELECT email FROM public.users WHERE auth_user_id = '<user-id>';

-- ============================================================================
-- Benefits of Email Normalization
-- ============================================================================
-- 
-- 1. Consistency: "User@Example.com" and "user@example.com" treated as same
-- 2. Database uniqueness: Prevents duplicate users with different casing
-- 3. Lookup reliability: Email searches work regardless of input casing
-- 4. Security: Consistent email handling prevents authentication bypasses
-- 5. User experience: Users can login with any casing of their email
--
-- ============================================================================

