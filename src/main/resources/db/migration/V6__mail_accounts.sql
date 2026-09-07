-- ════════════════════════════════════════════════════
-- mail_accounts
-- ════════════════════════════════════════════════════

CREATE TABLE mail_accounts
(
    id                  UUID PRIMARY KEY       DEFAULT gen_random_uuid(),
    user_id             UUID          NOT NULL,

    provider            provider_type NOT NULL,
    email               VARCHAR(255)  NOT NULL,
    app_password        TEXT          NOT NULL,

    display_name        VARCHAR(150),

    is_active           BOOLEAN       NOT NULL DEFAULT TRUE,
    is_default          BOOLEAN       NOT NULL DEFAULT FALSE,

    daily_sent_count    INT           NOT NULL DEFAULT 0,
    daily_limit         INT           NOT NULL DEFAULT 150,

    emails_per_batch    INT           NOT NULL DEFAULT 20,
    batch_cooldown_mins INT           NOT NULL DEFAULT 60,

    last_sent_at        TIMESTAMPTZ,
    cooldown_until      TIMESTAMPTZ,

    created_at          TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT fk_mail_accounts_user
        FOREIGN KEY (user_id) REFERENCES app_users (id) ON DELETE CASCADE,

    CONSTRAINT uq_mail_accounts_user_email
        UNIQUE (user_id, email),

    -- safe limits
    CONSTRAINT chk_daily_limit
        CHECK (daily_limit BETWEEN 1 AND 500),
    CONSTRAINT chk_emails_per_batch
        CHECK (emails_per_batch BETWEEN 1 AND 50),
    CONSTRAINT chk_cooldown_mins
        CHECK (batch_cooldown_mins BETWEEN 10 AND 1440),
    CONSTRAINT chk_daily_sent_count
        CHECK (daily_sent_count >= 0)
);

CREATE TRIGGER trg_mail_accounts_updated_at
    BEFORE UPDATE
    ON mail_accounts
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

-- User ke saare accounts list karna
CREATE INDEX idx_mail_accounts_user_id
    ON mail_accounts (user_id);

-- Partial: user ka default active account — O(1) lookup
CREATE INDEX idx_mail_accounts_default
    ON mail_accounts (user_id) WHERE is_default = TRUE AND is_active = TRUE;

-- Scheduler: cooldown check — kaunse accounts abhi available hain
CREATE INDEX idx_mail_accounts_cooldown
    ON mail_accounts (cooldown_until ASC) WHERE cooldown_until IS NOT NULL;

-- Daily reset job: daily_sent_count reset karna hota hai raat ko
CREATE INDEX idx_mail_accounts_daily_count
    ON mail_accounts (user_id, daily_sent_count) WHERE daily_sent_count > 0;
