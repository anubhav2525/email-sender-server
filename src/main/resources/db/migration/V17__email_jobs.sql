-- ════════════════════════════════════════════════════
-- email_jobs
-- NEW: This table was MISSING from original migrations
-- ════════════════════════════════════════════════════

CREATE TABLE email_jobs
(
    id             UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    campaign_id    UUID        NOT NULL,
    recipient_id   UUID        NOT NULL,

    status         job_status  NOT NULL DEFAULT 'PENDING',
    scheduled_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    sent_at        TIMESTAMPTZ,

    retry_count    INT         NOT NULL DEFAULT 0,
    failure_reason TEXT,

    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_jobs_campaign
        FOREIGN KEY (campaign_id) REFERENCES campaigns (id) ON DELETE CASCADE,
    CONSTRAINT fk_jobs_recipient
        FOREIGN KEY (recipient_id) REFERENCES recipients (id),

    CONSTRAINT chk_retry_count
        CHECK (retry_count BETWEEN 0 AND 3),

    CONSTRAINT uq_jobs_campaign_recipient
        UNIQUE (campaign_id, recipient_id)
);

CREATE TRIGGER trg_email_jobs_updated_at
    BEFORE UPDATE
    ON email_jobs
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

CREATE INDEX idx_jobs_campaign_id
    ON email_jobs (campaign_id);

CREATE INDEX idx_jobs_recipient_id
    ON email_jobs (recipient_id);

CREATE INDEX idx_jobs_campaign_status
    ON email_jobs (campaign_id, status);

-- Most critical scheduler query:
-- SELECT * FROM email_jobs
-- WHERE status = 'PENDING' AND scheduled_at <= now()
-- ORDER BY scheduled_at ASC
-- LIMIT 10;
CREATE INDEX idx_jobs_scheduler
    ON email_jobs (scheduled_at ASC, status) WHERE status = 'PENDING';

-- Failed jobs retry: "3 se kam retry wale FAILED jobs"
CREATE INDEX idx_jobs_retry
    ON email_jobs (campaign_id, retry_count) WHERE status = 'FAILED' AND retry_count < 3;
