-- ════════════════════════════════════════════════════
-- attachments
-- ════════════════════════════════════════════════════

CREATE TABLE attachments
(
    id         UUID PRIMARY KEY         DEFAULT gen_random_uuid(),
    user_id    UUID            NOT NULL,

    file_name  VARCHAR(255)    NOT NULL,
    file_path  TEXT            NOT NULL,
    file_size  BIGINT          NOT NULL DEFAULT 0,

    type       attachment_type NOT NULL,

    mime_type  VARCHAR(100), -- 'application/pdf', 'image/png' etc

    -- TRUE = email body ke andar image embed hogi (logo, banner)
    -- FALSE = normal attachment
    is_inline  BOOLEAN         NOT NULL DEFAULT FALSE,

    created_at TIMESTAMPTZ     NOT NULL DEFAULT now(),

    CONSTRAINT fk_attachments_user
        FOREIGN KEY (user_id) REFERENCES app_users (id) ON DELETE CASCADE,

    CONSTRAINT chk_file_size
        CHECK (file_size >= 0)
);

-- User ke saare attachments
CREATE INDEX idx_attachments_user_id
    ON attachments (user_id);

-- Type-wise filter: "mere saare documents dikhao" / "saare images"
CREATE INDEX idx_attachments_user_type
    ON attachments (user_id, type);

-- Partial: inline attachments (logos/banners) — email rendering ke liye
CREATE INDEX idx_attachments_inline
    ON attachments (user_id) WHERE is_inline = TRUE;

