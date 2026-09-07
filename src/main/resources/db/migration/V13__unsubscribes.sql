CREATE TABLE unsubscribes
(
    id              UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    user_id         UUID         NOT NULL,
    email           VARCHAR(255) NOT NULL,
    reason          TEXT,
    unsubscribed_at TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT fk_unsub_user
        FOREIGN KEY (user_id) REFERENCES app_users (id) ON DELETE CASCADE,
    CONSTRAINT uq_unsub_user_email
        UNIQUE (user_id, email)
);

-- User ke saare unsubscribes list karna (dashboard)
CREATE INDEX idx_unsub_user_id
    ON unsubscribes (user_id);
