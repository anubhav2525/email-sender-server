-- ════════════════════════════════════════════════════
-- profiles  (1:1 with app_users)
-- ════════════════════════════════════════════════════

CREATE TABLE profiles
(
    id           UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    user_id      UUID         NOT NULL,

    first_name   VARCHAR(150) NOT NULL,
    middle_name  VARCHAR(150),
    last_name    VARCHAR(150) NOT NULL,

    phone        VARCHAR(20)  NOT NULL,
    email        VARCHAR(255),

    organization VARCHAR(200), -- Company/College/NGO
    website      VARCHAR(300), -- Primary website (optional)

    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT fk_profiles_user
        FOREIGN KEY (user_id) REFERENCES app_users (id) ON DELETE CASCADE,
    CONSTRAINT uq_profiles_user
        UNIQUE (user_id)       -- strict 1:1
);

CREATE TRIGGER trg_profiles_updated_at
    BEFORE UPDATE
    ON profiles
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

-- Org-based search (admin panel me filter karna ho)
CREATE INDEX idx_profiles_organization
    ON profiles (organization) WHERE organization IS NOT NULL;
