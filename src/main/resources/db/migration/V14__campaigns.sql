CREATE TABLE campaigns
(
    id                   UUID PRIMARY KEY         DEFAULT gen_random_uuid(),
    user_id              UUID            NOT NULL,
    profile_id           UUID            NOT NULL,
    mail_account_id      UUID            NOT NULL,
    template_id          UUID,

    description          TEXT,
    scheduled_start_at   TIMESTAMPTZ,
    timezone             VARCHAR(50)     NOT NULL DEFAULT 'Asia/Kolkata',
    completed_at         TIMESTAMPTZ,

    name                 VARCHAR(200)    NOT NULL,
    type                 campaign_type   NOT NULL,
    status               campaign_status NOT NULL DEFAULT 'DRAFT',

    custom_subject       VARCHAR(500), -- INDIVIDUAL override
    custom_body          TEXT,         -- INDIVIDUAL override

    interval_min_minutes INT             NOT NULL DEFAULT 4,
    interval_max_minutes INT             NOT NULL DEFAULT 7,
    created_at           TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ     NOT NULL DEFAULT now(),

    CONSTRAINT fk_campaigns_user
        FOREIGN KEY (user_id) REFERENCES app_users (id) ON DELETE CASCADE,
    CONSTRAINT fk_campaigns_profile
        FOREIGN KEY (profile_id) REFERENCES profiles (id),
    CONSTRAINT fk_campaigns_mail_account
        FOREIGN KEY (mail_account_id) REFERENCES mail_accounts (id),
    CONSTRAINT fk_campaigns_template
        FOREIGN KEY (template_id) REFERENCES email_templates (id) ON DELETE SET NULL,

    -- BULK campaign me template mandatory hai
    CONSTRAINT chk_bulk_needs_template
        CHECK (type = 'INDIVIDUAL' OR template_id IS NOT NULL),

    -- INDIVIDUAL me custom content mandatory
    CONSTRAINT chk_individual_needs_content
        CHECK (type = 'BULK' OR (custom_subject IS NOT NULL AND custom_body IS NOT NULL)),

    -- interval valid hona chahiye
    CONSTRAINT chk_interval_range
        CHECK (interval_min_minutes >= 1
            AND interval_max_minutes >= interval_min_minutes
            AND interval_max_minutes <= 60),

    -- user sirf apna hi profile aur account use kar sake
    -- (enforced in application layer via service validation)
    CONSTRAINT uq_campaigns_name_user
        UNIQUE (user_id, name)
);

CREATE TRIGGER trg_campaigns_updated_at
    BEFORE UPDATE
    ON campaigns
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

-- User ke saare campaigns
CREATE INDEX idx_campaigns_user_id
    ON campaigns (user_id);

-- Status filter: "mere saare RUNNING campaigns"
CREATE INDEX idx_campaigns_user_status
    ON campaigns (user_id, status);

-- Partial: sirf RUNNING campaigns — scheduler ka primary query
CREATE INDEX idx_campaigns_running
    ON campaigns (id, mail_account_id) WHERE status = 'RUNNING';

-- Future scheduling: "kaunse campaigns kal start honge"
CREATE INDEX idx_campaigns_scheduled
    ON campaigns (scheduled_start_at ASC) WHERE scheduled_start_at IS NOT NULL AND status = 'DRAFT';

-- Mail account load check: "is account se kitne campaigns chal rahe hain"
CREATE INDEX idx_campaigns_mail_account
    ON campaigns (mail_account_id, status);

-- Profile-wise: "is profile se kitne campaigns bheji hain"
CREATE INDEX idx_campaigns_profile_id
    ON campaigns (profile_id);
