-- V24: User-flashcard save relationship.
-- Allows each authenticated user to bookmark any flashcard.

CREATE TABLE user_saved_flashcards (
    user_id      VARCHAR(128) NOT NULL,
    flashcard_id UUID         NOT NULL,
    saved_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_user_saved_flashcards PRIMARY KEY (user_id, flashcard_id),
    CONSTRAINT fk_usf_user             FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_usf_flashcard        FOREIGN KEY (flashcard_id)
        REFERENCES flashcards (id) ON DELETE CASCADE
);

CREATE INDEX idx_usf_user_id ON user_saved_flashcards (user_id);
