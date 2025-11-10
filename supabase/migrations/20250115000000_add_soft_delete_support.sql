-- Migration: Add soft delete support to all tables
-- Description: Adds deleted_at column to all entity tables for soft delete functionality

-- Add deleted_at column to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;

-- Add deleted_at column to subjects table
ALTER TABLE subjects ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;

-- Add deleted_at column to classes table
ALTER TABLE classes ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;

-- Add deleted_at column to enrollments table
ALTER TABLE enrollments ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;

-- Add deleted_at column to grades table
ALTER TABLE grades ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;

-- Add deleted_at column to ai_recommendations table
ALTER TABLE ai_recommendations ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;

-- Create indexes on deleted_at columns for better query performance
CREATE INDEX IF NOT EXISTS idx_users_deleted_at ON users(deleted_at) WHERE deleted_at IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_subjects_deleted_at ON subjects(deleted_at) WHERE deleted_at IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_classes_deleted_at ON classes(deleted_at) WHERE deleted_at IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_enrollments_deleted_at ON enrollments(deleted_at) WHERE deleted_at IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_grades_deleted_at ON grades(deleted_at) WHERE deleted_at IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_ai_recommendations_deleted_at ON ai_recommendations(deleted_at) WHERE deleted_at IS NOT NULL;

-- Add comments to document the soft delete functionality
COMMENT ON COLUMN users.deleted_at IS 'Soft delete timestamp - NULL means active, non-NULL means soft-deleted';
COMMENT ON COLUMN subjects.deleted_at IS 'Soft delete timestamp - NULL means active, non-NULL means soft-deleted';
COMMENT ON COLUMN classes.deleted_at IS 'Soft delete timestamp - NULL means active, non-NULL means soft-deleted';
COMMENT ON COLUMN enrollments.deleted_at IS 'Soft delete timestamp - NULL means active, non-NULL means soft-deleted';
COMMENT ON COLUMN grades.deleted_at IS 'Soft delete timestamp - NULL means active, non-NULL means soft-deleted';
COMMENT ON COLUMN ai_recommendations.deleted_at IS 'Soft delete timestamp - NULL means active, non-NULL means soft-deleted';


