
CREATE INDEX IF NOT EXISTS idx_grades_assessment_kind ON grades(assessment_kind);
CREATE INDEX IF NOT EXISTS idx_grades_graded_at ON grades(graded_at);

CREATE INDEX IF NOT EXISTS idx_subjects_name ON subjects(name);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

CREATE INDEX IF NOT EXISTS idx_enrollments_student_status ON enrollments(student_id, status);
CREATE INDEX IF NOT EXISTS idx_enrollments_class_status ON enrollments(class_id, status);

CREATE INDEX IF NOT EXISTS idx_grades_class_graded_at ON grades(class_id, graded_at DESC);
CREATE INDEX IF NOT EXISTS idx_grades_student_graded_at ON grades(student_id, graded_at DESC);
