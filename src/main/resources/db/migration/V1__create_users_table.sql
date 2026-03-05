-- V1: Users table
-- Primary key is the Firebase UID (string). No password column.

CREATE TABLE users (
    id          VARCHAR(128)    NOT NULL,
    email       VARCHAR(255)    NOT NULL,
    role        VARCHAR(32)     NOT NULL DEFAULT 'USER',
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_users        PRIMARY KEY (id),
    CONSTRAINT uq_users_email  UNIQUE (email),
    CONSTRAINT chk_users_role  CHECK (role IN ('USER', 'CONTENT_MANAGER', 'ADMIN'))
);

CREATE INDEX idx_users_role ON users (role);
