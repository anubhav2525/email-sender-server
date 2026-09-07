CREATE TABLE email_events
(
    id          UUID PRIMARY KEY          DEFAULT gen_random_uuid(), -- BUG FIX: was BIGSERIAL
    job_id      UUID             NOT NULL,                           -- BUG FIX: was BIGINT
    event_type  email_event_type NOT NULL,
    occurred_at TIMESTAMPTZ      NOT NULL DEFAULT now(),
    metadata    JSONB                     DEFAULT '{}'::jsonb,
    -- OPEN        → {"ip": "122.x.x.x", "user_agent": "Gmail/Android"}
    -- CLICK       → {"url": "https://linkedin.com/in/...", "link_label": "Portfolio"}
    -- BOUNCE      → {"reason": "550 mailbox full", "bounce_type": "hard"}
    -- SPAM_REPORT → {"reported_via": "gmail"}

    CONSTRAINT fk_ee_job
        FOREIGN KEY (job_id) REFERENCES email_jobs (id) ON DELETE CASCADE
);

-- Job ke saare events (open/click tracking)
CREATE INDEX idx_ee_job_id
    ON email_events (job_id);

-- Campaign analytics: event type breakdown
-- JOIN email_jobs → email_events → GROUP BY event_type
CREATE INDEX idx_ee_job_event
    ON email_events (job_id, event_type);

-- Time-range analytics: "is week kitne opens hue"
CREATE INDEX idx_ee_occurred_at
    ON email_events (occurred_at DESC);

-- JSONB: metadata search (click tracking — kaunsa URL click hua)
CREATE INDEX idx_ee_metadata
    ON email_events USING GIN (metadata);