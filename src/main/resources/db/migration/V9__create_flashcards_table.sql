CREATE TABLE flashcards (
    id               UUID         NOT NULL DEFAULT gen_random_uuid(),
    word_ru          VARCHAR(255) NOT NULL,
    word_kz          VARCHAR(255) NOT NULL,
    transcription    VARCHAR(255),
    example_sentence TEXT,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_flashcards PRIMARY KEY (id)
);
