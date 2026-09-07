CREATE TABLE profile_links
(
    id         UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    profile_id UUID         NOT NULL,

    label      VARCHAR(100) NOT NULL,              -- "LinkedIn", "Portfolio", "GitHub"
    url        TEXT         NOT NULL,
    is_visible BOOLEAN      NOT NULL DEFAULT TRUE, -- mail me show karo ya nahi
    sort_order SMALLINT     NOT NULL DEFAULT 0,    -- drag-drop ordering ke liye

    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT fk_pl_profile
        FOREIGN KEY (profile_id) REFERENCES profiles (id) ON DELETE CASCADE,

    CONSTRAINT uq_pl_profile_label
        UNIQUE (profile_id, label),

    CONSTRAINT chk_url_not_empty
        CHECK (trim(url) <> '')
);

CREATE TRIGGER trg_profile_links_updated_at
    BEFORE UPDATE
    ON profile_links
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

-- Profile ke saare links fetch karna (most common)
CREATE INDEX idx_profile_links_profile_id
    ON profile_links (profile_id);

-- Ordered fetch: drag-drop sort order ke saath
CREATE INDEX idx_profile_links_ordered
    ON profile_links (profile_id, sort_order ASC);

-- Partial: sirf visible links — email render karte waqt
CREATE INDEX idx_profile_links_visible
    ON profile_links (profile_id, sort_order ASC) WHERE is_visible = TRUE;

