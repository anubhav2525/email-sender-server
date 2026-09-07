CREATE TABLE campaign_attachments
(
    campaign_id   UUID NOT NULL,
    attachment_id UUID NOT NULL,

    PRIMARY KEY (campaign_id, attachment_id),

    CONSTRAINT fk_ca_campaign
        FOREIGN KEY (campaign_id) REFERENCES campaigns (id) ON DELETE CASCADE,
    CONSTRAINT fk_ca_attachment
        FOREIGN KEY (attachment_id) REFERENCES attachments (id) ON DELETE CASCADE
);

-- Reverse lookup: "yeh attachment kitne campaigns me use ho rahi hai"
CREATE INDEX idx_ca_attachment_id
    ON campaign_attachments (attachment_id);