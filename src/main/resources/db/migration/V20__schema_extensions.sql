-- V20: Schema extensions — all DDL changes that were previously spread across V22–V29

-- ── Articles: bilingual support + cover image ─────────────────────────────────
ALTER TABLE articles
    ADD COLUMN IF NOT EXISTS image_url  VARCHAR(1000),
    ADD COLUMN IF NOT EXISTS title_kz   VARCHAR(500),
    ADD COLUMN IF NOT EXISTS content_kz TEXT;

-- ── Users: add ELEMENTARY to level constraint ─────────────────────────────────
ALTER TABLE users DROP CONSTRAINT IF EXISTS chk_users_level;

ALTER TABLE users ADD CONSTRAINT chk_users_level
    CHECK (level IN ('BEGINNER', 'ELEMENTARY', 'INTERMEDIATE', 'ADVANCED') OR level IS NULL);

-- ── Users: store R2 object key for avatar ─────────────────────────────────────
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS avatar_object_key VARCHAR(1024);

-- ── Flashcards: audio URL ─────────────────────────────────────────────────────
ALTER TABLE flashcards
    ADD COLUMN IF NOT EXISTS audio_url TEXT;

-- ── User saved flashcards ─────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS user_saved_flashcards (
    user_id      VARCHAR(128) NOT NULL,
    flashcard_id UUID         NOT NULL,
    saved_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_user_saved_flashcards PRIMARY KEY (user_id, flashcard_id),
    CONSTRAINT fk_usf_user             FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_usf_flashcard        FOREIGN KEY (flashcard_id)
        REFERENCES flashcards (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_usf_user_id ON user_saved_flashcards (user_id);

-- ── User session time ─────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS user_session_time (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      VARCHAR(128) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    session_date DATE         NOT NULL,
    total_seconds INT         NOT NULL DEFAULT 0,
    CONSTRAINT uq_user_session_date UNIQUE (user_id, session_date)
);

CREATE INDEX IF NOT EXISTS idx_ust_user_date ON user_session_time(user_id, session_date);
