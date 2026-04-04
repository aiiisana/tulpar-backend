CREATE TABLE grammar_rules (
    id          UUID         NOT NULL DEFAULT gen_random_uuid(),
    title       VARCHAR(500) NOT NULL,
    explanation TEXT         NOT NULL,
    examples    TEXT         NOT NULL DEFAULT '[]',  -- JSON array
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_grammar_rules PRIMARY KEY (id)
);
