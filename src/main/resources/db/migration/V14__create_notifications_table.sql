CREATE TABLE notifications (
    id         UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id    VARCHAR(128) NOT NULL,
    title      VARCHAR(255) NOT NULL,
    body       TEXT         NOT NULL,
    is_read    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_notifications         PRIMARY KEY (id),
    CONSTRAINT fk_notification_user     FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_notifications_user_date ON notifications (user_id, created_at DESC);
