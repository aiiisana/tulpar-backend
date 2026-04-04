CREATE TABLE daily_challenges (
    id             UUID    NOT NULL DEFAULT gen_random_uuid(),
    challenge_date DATE    NOT NULL,
    correct_word   VARCHAR(255) NOT NULL,
    letters        TEXT    NOT NULL,   -- JSON array of letter strings
    image_urls     TEXT    NOT NULL,   -- JSON array of image URLs

    CONSTRAINT pk_daily_challenges          PRIMARY KEY (id),
    CONSTRAINT uq_daily_challenges_date     UNIQUE (challenge_date)
);

CREATE INDEX idx_daily_challenges_date ON daily_challenges (challenge_date);
