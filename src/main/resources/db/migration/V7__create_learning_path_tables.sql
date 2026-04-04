-- Duolingo-style learning path: Course → Level → Lesson → LessonExercise

CREATE TABLE courses (
    id          UUID            NOT NULL DEFAULT gen_random_uuid(),
    title       VARCHAR(255)    NOT NULL,
    description TEXT,
    order_index INTEGER         NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_courses PRIMARY KEY (id)
);

CREATE TABLE course_levels (
    id          UUID            NOT NULL DEFAULT gen_random_uuid(),
    course_id   UUID            NOT NULL,
    title       VARCHAR(255)    NOT NULL,
    order_index INTEGER         NOT NULL DEFAULT 0,

    CONSTRAINT pk_course_levels         PRIMARY KEY (id),
    CONSTRAINT fk_course_level_course   FOREIGN KEY (course_id)
        REFERENCES courses (id) ON DELETE CASCADE
);

CREATE INDEX idx_course_levels_course ON course_levels (course_id, order_index);

CREATE TABLE lessons (
    id          UUID            NOT NULL DEFAULT gen_random_uuid(),
    level_id    UUID            NOT NULL,
    title       VARCHAR(255)    NOT NULL,
    order_index INTEGER         NOT NULL DEFAULT 0,
    xp_reward   INTEGER         NOT NULL DEFAULT 10,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_lessons           PRIMARY KEY (id),
    CONSTRAINT fk_lesson_level      FOREIGN KEY (level_id)
        REFERENCES course_levels (id) ON DELETE CASCADE
);

CREATE INDEX idx_lessons_level ON lessons (level_id, order_index);

CREATE TABLE lesson_exercises (
    id          UUID    NOT NULL DEFAULT gen_random_uuid(),
    lesson_id   UUID    NOT NULL,
    exercise_id UUID    NOT NULL,
    order_index INTEGER NOT NULL DEFAULT 0,

    CONSTRAINT pk_lesson_exercises          PRIMARY KEY (id),
    CONSTRAINT uq_lesson_exercise           UNIQUE (lesson_id, exercise_id),
    CONSTRAINT fk_lesson_exercise_lesson    FOREIGN KEY (lesson_id)
        REFERENCES lessons (id) ON DELETE CASCADE,
    CONSTRAINT fk_lesson_exercise_exercise  FOREIGN KEY (exercise_id)
        REFERENCES exercises (id) ON DELETE CASCADE
);

CREATE INDEX idx_lesson_exercises_lesson ON lesson_exercises (lesson_id, order_index);
