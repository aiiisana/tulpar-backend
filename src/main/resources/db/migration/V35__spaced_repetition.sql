-- V35: Spaced repetition state table (SM-2 algorithm)
-- Each row tracks one user's SM-2 state for one exercise.

CREATE TABLE IF NOT EXISTS spaced_repetition_state (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         VARCHAR(128) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    exercise_id     UUID        NOT NULL REFERENCES exercises(id) ON DELETE CASCADE,
    ease_factor     DOUBLE PRECISION NOT NULL DEFAULT 2.5,   -- SM-2 E-factor (min 1.3)
    interval_days   INT         NOT NULL DEFAULT 1,          -- days until next review
    repetitions     INT         NOT NULL DEFAULT 0,          -- consecutive correct reviews
    next_review_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),      -- when to show next
    last_reviewed_at TIMESTAMPTZ,
    CONSTRAINT uq_srs_user_exercise UNIQUE (user_id, exercise_id)
);

CREATE INDEX IF NOT EXISTS idx_srs_user_due
    ON spaced_repetition_state (user_id, next_review_at);
