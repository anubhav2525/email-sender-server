CREATE TABLE recipient_groups
(
    id          UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    user_id     UUID         NOT NULL,

    name        VARCHAR(150) NOT NULL,
    description TEXT,

    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT fk_rg_user
        FOREIGN KEY (user_id) REFERENCES app_users (id) ON DELETE CASCADE,
    CONSTRAINT uq_rg_user_name
        UNIQUE (user_id, name)
);

CREATE TRIGGER trg_recipient_groups_updated_at
    BEFORE UPDATE
    ON recipient_groups
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

-- User ke saare groups
CREATE INDEX idx_rg_user_id
    ON recipient_groups (user_id);

