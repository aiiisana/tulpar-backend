-- V16: Store FCM device tokens for push notifications.
--
-- One user can have multiple devices (phone + tablet, etc.).
-- ON CONFLICT … DO UPDATE keeps the token fresh on every app launch.

CREATE TABLE device_tokens (
    id         UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id    VARCHAR(128) NOT NULL,
    token      TEXT         NOT NULL,
    platform   VARCHAR(10)  NOT NULL DEFAULT 'unknown', -- 'ios' | 'android' | 'web'
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_device_tokens      PRIMARY KEY (id),
    CONSTRAINT uq_device_tokens_tok  UNIQUE (token),
    CONSTRAINT fk_device_token_user  FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_device_tokens_user ON device_tokens (user_id);
