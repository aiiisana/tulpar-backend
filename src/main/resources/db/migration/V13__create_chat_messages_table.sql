CREATE TABLE chat_messages (
    id         UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id    VARCHAR(128) NOT NULL,
    role       VARCHAR(16)  NOT NULL,
    content    TEXT         NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_chat_messages         PRIMARY KEY (id),
    CONSTRAINT chk_chat_message_role    CHECK (role IN ('USER', 'ASSISTANT')),
    CONSTRAINT fk_chat_message_user     FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_chat_messages_user_date ON chat_messages (user_id, created_at DESC);
