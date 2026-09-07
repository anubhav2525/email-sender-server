CREATE TABLE recipients
(
    id            UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    user_id       UUID         NOT NULL,

    name          VARCHAR(150) NOT NULL,
    email         VARCHAR(255) NOT NULL,
    organization  VARCHAR(200),                       -- "company" → generic
    role          VARCHAR(200),                       -- "position" → generic
    notes         TEXT,

    custom_fields JSONB                 DEFAULT '{}'::jsonb,

    is_active     BOOLEAN      NOT NULL DEFAULT TRUE, -- NEW: soft disable
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT fk_recipients_user
        FOREIGN KEY (user_id) REFERENCES app_users (id) ON DELETE CASCADE,
    CONSTRAINT uq_recipients_user_email
        UNIQUE (user_id, email)
);

CREATE TRIGGER trg_recipients_updated_at
    BEFORE UPDATE
    ON recipients
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

-- User ke saare recipients
CREATE INDEX idx_recipients_user_id
    ON recipients (user_id);

-- Active recipients only — campaign me add karte waqt
CREATE INDEX idx_recipients_user_active
    ON recipients (user_id, is_active) WHERE is_active = TRUE;

-- Name search: "Rahul" search karo recipients me
CREATE INDEX idx_recipients_name
    ON recipients (user_id, name);

-- JSONB: custom_fields pe search
-- e.g., WHERE custom_fields @> '{"department": "Engineering"}'
-- e.g., WHERE custom_fields @> '{"dietary": "veg"}'
CREATE INDEX idx_recipients_custom_fields
    ON recipients USING GIN (custom_fields);
