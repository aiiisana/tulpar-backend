-- User stats (streak, XP) and daily activity calendar

CREATE TABLE user_stats (
    user_id            VARCHAR(128) NOT NULL,
    current_streak     INTEGER      NOT NULL DEFAULT 0,
    longest_streak     INTEGER      NOT NULL DEFAULT 0,
    total_xp           INTEGER      NOT NULL DEFAULT 0,
    last_activity_date DATE,

    CONSTRAINT pk_user_stats        PRIMARY KEY (user_id),
    CONSTRAINT fk_user_stats_user   FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE user_daily_activity (
    id            UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id       VARCHAR(128) NOT NULL,
    activity_date DATE         NOT NULL,
    completed     BOOLEAN      NOT NULL DEFAULT FALSE,

    CONSTRAINT pk_user_daily_activity       PRIMARY KEY (id),
    CONSTRAINT uq_user_daily_activity       UNIQUE (user_id, activity_date),
    CONSTRAINT fk_daily_activity_user       FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_daily_activity_user_date ON user_daily_activity (user_id, activity_date DESC);
