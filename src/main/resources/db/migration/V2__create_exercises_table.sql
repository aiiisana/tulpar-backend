-- V2: Base exercises table (parent of JOINED inheritance hierarchy)

CREATE TABLE exercises (
    id               UUID            NOT NULL DEFAULT gen_random_uuid(),
    exercise_type    VARCHAR(32)     NOT NULL,
    difficulty_level VARCHAR(32)     NOT NULL,
    question         TEXT            NOT NULL,
    explanation      TEXT,
    created_at       TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_exercises              PRIMARY KEY (id),
    CONSTRAINT chk_exercises_type        CHECK (exercise_type IN (
        'VOCABULARY', 'SENTENCE_BUILDER', 'LISTENING',
        'VIDEO_CONTEXT', 'IMAGE_CONTEXT', 'AI_GENERATED'
    )),
    CONSTRAINT chk_exercises_difficulty  CHECK (difficulty_level IN (
        'BEGINNER', 'ELEMENTARY', 'INTERMEDIATE', 'ADVANCED'
    ))
);

CREATE INDEX idx_exercises_type       ON exercises (exercise_type);
CREATE INDEX idx_exercises_difficulty ON exercises (difficulty_level);
CREATE INDEX idx_exercises_created_at ON exercises (created_at DESC);
