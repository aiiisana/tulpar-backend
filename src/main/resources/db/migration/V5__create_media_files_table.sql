-- V5: Media file metadata
-- The actual file is stored in MinIO; this table tracks the metadata.

CREATE TABLE media_files (
    id                UUID            NOT NULL DEFAULT gen_random_uuid(),
    media_type        VARCHAR(16)     NOT NULL,
    original_filename VARCHAR(512)    NOT NULL,
    object_key        VARCHAR(1024)   NOT NULL,
    url               VARCHAR(2048)   NOT NULL,
    content_type      VARCHAR(128),
    size_bytes        BIGINT,
    uploaded_by       VARCHAR(128)    NOT NULL,
    uploaded_at       TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_media_files          PRIMARY KEY (id),
    CONSTRAINT uq_media_files_key      UNIQUE (object_key),
    CONSTRAINT chk_media_files_type    CHECK (media_type IN ('AUDIO', 'VIDEO', 'IMAGE')),
    CONSTRAINT fk_media_uploaded_by    FOREIGN KEY (uploaded_by)
        REFERENCES users (id) ON DELETE SET NULL
);

CREATE INDEX idx_media_files_type        ON media_files (media_type);
CREATE INDEX idx_media_files_uploaded_by ON media_files (uploaded_by);
