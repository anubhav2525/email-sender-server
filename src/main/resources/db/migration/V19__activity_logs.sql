CREATE TABLE activity_logs
(
    id          UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    user_id     UUID         NOT NULL,
    action      VARCHAR(100) NOT NULL,
    -- Examples: 'CAMPAIGN_STARTED', 'CAMPAIGN_PAUSED', 'MAIL_ACCOUNT_ADDED',
    --           'RECIPIENT_IMPORTED', 'TEMPLATE_CREATED', 'UNSUBSCRIBE_RECEIVED'
    entity_type VARCHAR(50), -- 'CAMPAIGN', 'RECIPIENT', 'MAIL_ACCOUNT', 'TEMPLATE'
    entity_id   UUID,        -- BUG FIX: was BIGINT — all PKs are UUID in this schema
    metadata    JSONB                 DEFAULT '{}'::jsonb,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT fk_al_user
        FOREIGN KEY (user_id) REFERENCES app_users (id) ON DELETE CASCADE
);

-- User ki activity history
CREATE INDEX idx_al_user_id
    ON activity_logs (user_id);

-- Entity audit trail: "Campaign X pe kya kya actions hue"
CREATE INDEX idx_al_entity
    ON activity_logs (entity_type, entity_id) WHERE entity_id IS NOT NULL;

-- Recent activity: descending time
CREATE INDEX idx_al_created_at
    ON activity_logs (created_at DESC);

-- Action filter: "saare CAMPAIGN_STARTED events"
CREATE INDEX idx_al_action
    ON activity_logs (action, created_at DESC);