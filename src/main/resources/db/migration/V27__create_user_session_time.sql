-- Track how many seconds a user spends in the app per calendar day.
-- total_seconds is always incremented (upserted) by the client on each session flush.
CREATE TABLE user_session_time (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      VARCHAR(128) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    session_date DATE         NOT NULL,
    total_seconds INT         NOT NULL DEFAULT 0,
    CONSTRAINT uq_user_session_date UNIQUE (user_id, session_date)
);

CREATE INDEX idx_ust_user_date ON user_session_time(user_id, session_date);
