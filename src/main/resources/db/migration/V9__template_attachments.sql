-- ════════════════════════════════════════════════════
-- template_attachments
-- ════════════════════════════════════════════════════

CREATE TABLE template_attachments
(
    template_id   UUID NOT NULL,
    attachment_id UUID NOT NULL,

    PRIMARY KEY (template_id, attachment_id),

    CONSTRAINT fk_ta_template
        FOREIGN KEY (template_id) REFERENCES email_templates (id) ON DELETE CASCADE,
    CONSTRAINT fk_ta_attachment
        FOREIGN KEY (attachment_id) REFERENCES attachments (id) ON DELETE CASCADE
);

-- Reverse lookup: "yeh attachment kitne templates me use ho raha hai"
CREATE INDEX idx_ta_attachment_id
    ON template_attachments (attachment_id);
