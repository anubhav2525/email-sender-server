-- ════════════════════════════════════════════════════
-- campaign_recipients  (M:N junction)
-- ════════════════════════════════════════════════════

CREATE TABLE campaign_recipients
(
    campaign_id  UUID        NOT NULL,
    recipient_id UUID        NOT NULL,
    added_at     TIMESTAMPTZ NOT NULL DEFAULT now(),

    PRIMARY KEY (campaign_id, recipient_id),

    CONSTRAINT fk_cr_campaign
        FOREIGN KEY (campaign_id) REFERENCES campaigns (id) ON DELETE CASCADE,
    CONSTRAINT fk_cr_recipient
        FOREIGN KEY (recipient_id) REFERENCES recipients (id) ON DELETE CASCADE
);

-- Reverse lookup: "yeh recipient kitne campaigns me hai"
CREATE INDEX idx_cr_recipient_id
    ON campaign_recipients (recipient_id);
