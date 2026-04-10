-- V19: Add difficulty_level to course_levels so backend can filter by user's level

ALTER TABLE course_levels
    ADD COLUMN difficulty_level VARCHAR(32);

-- Populate based on order_index that V18 inserted
UPDATE course_levels SET difficulty_level = 'BEGINNER'     WHERE order_index = 1;
UPDATE course_levels SET difficulty_level = 'ELEMENTARY'   WHERE order_index = 2;
UPDATE course_levels SET difficulty_level = 'INTERMEDIATE' WHERE order_index = 3;
UPDATE course_levels SET difficulty_level = 'ADVANCED'     WHERE order_index = 4;

-- Add check constraint to keep values consistent
ALTER TABLE course_levels
    ADD CONSTRAINT chk_course_levels_difficulty
    CHECK (difficulty_level IN ('BEGINNER','ELEMENTARY','INTERMEDIATE','ADVANCED'));
