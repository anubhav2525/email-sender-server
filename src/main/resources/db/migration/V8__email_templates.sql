-- ════════════════════════════════════════════════════
-- email_templates
-- ════════════════════════════════════════════════════

CREATE TABLE email_templates
(
    id               UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    user_id          UUID         NOT NULL,

    name             VARCHAR(150) NOT NULL,
    subject_template VARCHAR(500) NOT NULL,
    body_template    TEXT         NOT NULL,

    preview_text     VARCHAR(200), -- Inbox me subject ke neeche dikhta hai: "Hi {{name}}, we'd like to..."
    category         VARCHAR(100), -- "Job Application", "Event Invite", "HR Onboarding" — user apna category de

    is_html          BOOLEAN      NOT NULL DEFAULT TRUE,

    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT fk_templates_user
        FOREIGN KEY (user_id) REFERENCES app_users (id) ON DELETE CASCADE,

    CONSTRAINT uq_templates_user_name
        UNIQUE (user_id, name)
);

CREATE TRIGGER trg_templates_updated_at
    BEFORE UPDATE
    ON email_templates
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

-- User ke saare templates
CREATE INDEX idx_templates_user_id
    ON email_templates (user_id);

-- Category filter: "mere saare Job Application templates"
CREATE INDEX idx_templates_user_category
    ON email_templates (user_id, category) WHERE category IS NOT NULL;