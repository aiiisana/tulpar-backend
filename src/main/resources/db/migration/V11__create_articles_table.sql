CREATE TABLE articles (
    id               UUID         NOT NULL DEFAULT gen_random_uuid(),
    title            VARCHAR(500) NOT NULL,
    content          TEXT         NOT NULL,
    difficulty_level VARCHAR(32)  NOT NULL,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_articles              PRIMARY KEY (id),
    CONSTRAINT chk_articles_difficulty  CHECK (difficulty_level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED'))
);

CREATE INDEX idx_articles_difficulty ON articles (difficulty_level);
CREATE INDEX idx_articles_created_at ON articles (created_at DESC);
