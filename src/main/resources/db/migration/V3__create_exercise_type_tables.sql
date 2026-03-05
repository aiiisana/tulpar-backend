-- V3: Exercise-type-specific tables (JOINED inheritance children)
--     Each table shares its PK with exercises.id via a FK.

-- ── Vocabulary exercises ────────────────────────────────────────────────────
CREATE TABLE vocabulary_exercises (
    exercise_id     UUID        NOT NULL,
    word            VARCHAR(255) NOT NULL,
    translation     VARCHAR(255) NOT NULL,
    options         TEXT        NOT NULL,   -- JSON array of strings
    correct_answer  VARCHAR(255) NOT NULL,

    CONSTRAINT pk_vocabulary_exercises   PRIMARY KEY (exercise_id),
    CONSTRAINT fk_vocabulary_exercise_id FOREIGN KEY (exercise_id)
        REFERENCES exercises (id) ON DELETE CASCADE
);

-- ── Listening exercises ─────────────────────────────────────────────────────
CREATE TABLE listening_exercises (
    exercise_id     UUID        NOT NULL,
    audio_url       VARCHAR(1024) NOT NULL,
    transcript      TEXT,
    options         TEXT        NOT NULL,
    correct_answer  VARCHAR(512) NOT NULL,

    CONSTRAINT pk_listening_exercises   PRIMARY KEY (exercise_id),
    CONSTRAINT fk_listening_exercise_id FOREIGN KEY (exercise_id)
        REFERENCES exercises (id) ON DELETE CASCADE
);

-- ── Video exercises ─────────────────────────────────────────────────────────
CREATE TABLE video_exercises (
    exercise_id     UUID            NOT NULL,
    video_url       VARCHAR(1024)   NOT NULL,
    start_time      DOUBLE PRECISION,
    end_time        DOUBLE PRECISION,
    options         TEXT            NOT NULL,
    correct_answer  VARCHAR(512)    NOT NULL,

    CONSTRAINT pk_video_exercises   PRIMARY KEY (exercise_id),
    CONSTRAINT fk_video_exercise_id FOREIGN KEY (exercise_id)
        REFERENCES exercises (id) ON DELETE CASCADE
);

-- ── Image exercises ─────────────────────────────────────────────────────────
CREATE TABLE image_exercises (
    exercise_id     UUID            NOT NULL,
    image_url       VARCHAR(1024)   NOT NULL,
    options         TEXT            NOT NULL,
    correct_answer  VARCHAR(512)    NOT NULL,

    CONSTRAINT pk_image_exercises   PRIMARY KEY (exercise_id),
    CONSTRAINT fk_image_exercise_id FOREIGN KEY (exercise_id)
        REFERENCES exercises (id) ON DELETE CASCADE
);

-- ── Sentence builder exercises ──────────────────────────────────────────────
CREATE TABLE sentence_builder_exercises (
    exercise_id       UUID    NOT NULL,
    correct_sentence  TEXT    NOT NULL,
    shuffled_words    TEXT    NOT NULL,  -- JSON array of strings

    CONSTRAINT pk_sentence_builder_exercises   PRIMARY KEY (exercise_id),
    CONSTRAINT fk_sentence_builder_exercise_id FOREIGN KEY (exercise_id)
        REFERENCES exercises (id) ON DELETE CASCADE
);
