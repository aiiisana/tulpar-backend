-- V37: Practice conversation history, one row per exchange (user msg + AI reply).
CREATE TABLE practice_messages (
    id          UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id     VARCHAR(128) NOT NULL,
    user_text   TEXT         NOT NULL,
    ai_reply    TEXT,
    has_errors  BOOLEAN      NOT NULL DEFAULT false,
    corrections TEXT         NOT NULL DEFAULT '[]',   -- JSON [{original,corrected,explanation}]
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_practice_messages      PRIMARY KEY (id),
    CONSTRAINT fk_practice_messages_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_practice_messages_user ON practice_messages (user_id, created_at ASC);
