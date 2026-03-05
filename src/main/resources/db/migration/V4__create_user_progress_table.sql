-- V4: User progress — one row per (user, exercise) pair

CREATE TABLE user_progress (
    id                  UUID            NOT NULL DEFAULT gen_random_uuid(),
    user_id             VARCHAR(128)    NOT NULL,
    exercise_id         UUID            NOT NULL,
    status              VARCHAR(32)     NOT NULL DEFAULT 'IN_PROGRESS',
    attempts            INTEGER         NOT NULL DEFAULT 0,
    completed_at        TIMESTAMPTZ,
    last_attempted_at   TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_user_progress         PRIMARY KEY (id),
    CONSTRAINT uq_user_exercise         UNIQUE (user_id, exercise_id),
    CONSTRAINT fk_progress_user_id      FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_progress_exercise_id  FOREIGN KEY (exercise_id)
        REFERENCES exercises (id) ON DELETE CASCADE,
    CONSTRAINT chk_progress_status      CHECK (status IN ('IN_PROGRESS', 'COMPLETED', 'FAILED'))
);

CREATE INDEX idx_user_progress_user_id    ON user_progress (user_id);
CREATE INDEX idx_user_progress_exercise   ON user_progress (exercise_id);
CREATE INDEX idx_user_progress_status     ON user_progress (user_id, status);
