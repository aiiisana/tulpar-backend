-- V22: Add ELEMENTARY to the users.level check constraint.
-- V6 created the constraint with only BEGINNER/INTERMEDIATE/ADVANCED,
-- but the DifficultyLevel enum (and course_levels) includes ELEMENTARY.
-- Without this fix, setting a user's level to ELEMENTARY throws a
-- ConstraintViolationException at the DB layer.

ALTER TABLE users DROP CONSTRAINT chk_users_level;

ALTER TABLE users ADD CONSTRAINT chk_users_level
    CHECK (level IN ('BEGINNER', 'ELEMENTARY', 'INTERMEDIATE', 'ADVANCED') OR level IS NULL);
