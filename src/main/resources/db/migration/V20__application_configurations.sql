CREATE TABLE application_config
(
    id                  BIGINT PRIMARY KEY,

    -- branding
    app_name            VARCHAR(100)             NOT NULL,
    app_short_name      VARCHAR(20)              NOT NULL,
    owner_name          VARCHAR(20)              NOT NULL,
    web_site_url        VARCHAR(255)             NOT NULL,
    logo_url            VARCHAR(500),
    favicon_url         VARCHAR(500),
    tagline             VARCHAR(200)             NOT NULL,
    description         TEXT,
    sub_description     TEXT,

    -- contact
    contact_phone       VARCHAR(20),
    contact_email       VARCHAR(50),
    contact_whatsapp    VARCHAR(20),
    contact_address     VARCHAR(300),

    -- seo / meta
    meta_description    VARCHAR(300),
    meta_keywords       VARCHAR(500),
    og_image_url        VARCHAR(500),

    -- app / social links
    app_store_url       VARCHAR(500),
    play_store_url      VARCHAR(500),
    twitter_url         VARCHAR(500),
    facebook_url        VARCHAR(500),
    instagram_url       VARCHAR(500),
    whatsapp_link       VARCHAR(500),

    -- audit
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);