-- Migration: Add grade_id to ai_recommendations table
-- Description: Adds grade_id foreign key to link recommendations to specific assessments/grades

-- Add grade_id column (nullable to support existing recommendations)
ALTER TABLE ai_recommendations 
ADD COLUMN IF NOT EXISTS grade_id UUID;

-- Add foreign key constraint
ALTER TABLE ai_recommendations
ADD CONSTRAINT ai_recommendations_grade_id_fkey 
FOREIGN KEY (grade_id) REFERENCES grades(id);

-- Create index for query performance
CREATE INDEX IF NOT EXISTS idx_ai_recommendations_grade_id 
ON ai_recommendations(grade_id) 
WHERE grade_id IS NOT NULL;

-- Add comment to document the relationship
COMMENT ON COLUMN ai_recommendations.grade_id IS 'Foreign key to grades table - links recommendation to specific assessment. NULL for general recommendations not tied to a specific grade.';

