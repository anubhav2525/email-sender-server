CREATE TABLE recipient_group_members
(
    group_id     UUID        NOT NULL,
    recipient_id UUID        NOT NULL,
    added_at     TIMESTAMPTZ NOT NULL DEFAULT now(),

    PRIMARY KEY (group_id, recipient_id),

    CONSTRAINT fk_rgm_group
        FOREIGN KEY (group_id) REFERENCES recipient_groups (id) ON DELETE CASCADE,
    CONSTRAINT fk_rgm_recipient
        FOREIGN KEY (recipient_id) REFERENCES recipients (id) ON DELETE CASCADE
);

-- Reverse lookup: "yeh recipient kis kis group me hai"
CREATE INDEX idx_rgm_recipient_id
    ON recipient_group_members (recipient_id);