-- ─────────────────────────────────────────────
-- 1. PERMISSIONS
-- ─────────────────────────────────────────────
-- Pattern: RESOURCE:ACTION
-- e.g.     APPLICATION:CREATE, TAX_RATE:UPDATE
-- ─────────────────────────────────────────────

CREATE TABLE permissions
(
    id          UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL UNIQUE, -- 'APPLICATION:CREATE'
    resource    VARCHAR(50)  NOT NULL,        -- 'APPLICATION'
    action      VARCHAR(50)  NOT NULL,        -- 'CREATE'
    description TEXT,

    enabled     BOOLEAN      NOT NULL DEFAULT TRUE,
    deleted     BOOLEAN      NOT NULL DEFAULT FALSE,

    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_permission_resource_action UNIQUE (resource, action)
);

CREATE INDEX idx_permissions_resource ON permissions (resource);

CREATE TRIGGER trg_permissions_updated_at
    BEFORE UPDATE
    ON permissions
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

-- ─────────────────────────────────────────────
-- 2. ROLES
-- ─────────────────────────────────────────────

CREATE TABLE roles
(
    id           UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    name         VARCHAR(50)  NOT NULL UNIQUE, -- 'SUPER_ADMIN', 'ADMIN', 'AGENT', 'USER'
    display_name VARCHAR(100) NOT NULL,        -- 'Super Administrator', 'Agent'
    description  TEXT,

    enabled      BOOLEAN      NOT NULL DEFAULT TRUE,
    deleted      BOOLEAN      NOT NULL DEFAULT FALSE,

    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TRIGGER trg_roles_updated_at
    BEFORE UPDATE
    ON roles
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

CREATE INDEX idx_role_name ON roles (name);

-- ════════════════════════════════════════════════════
-- 3: App Users
-- ════════════════════════════════════════════════════

CREATE TABLE app_users
(
    id                       UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    full_name                VARCHAR(150) NOT NULL,
    std_code                 VARCHAR(4)   NOT NULL DEFAULT '+91',
    phone                    VARCHAR(15)  NOT NULL,

    email                    VARCHAR(255) NOT NULL,
    password                 VARCHAR(255) NOT NULL,

    enabled                  BOOLEAN      NOT NULL DEFAULT TRUE,
    deleted                  BOOLEAN      NOT NULL DEFAULT FALSE,

    profile_url              VARCHAR(500),

    is_email_verified        BOOLEAN      NOT NULL DEFAULT FALSE,
    account_status           account_status_type   DEFAULT 'PENDING',

    reject_reason            VARCHAR,

    is_active                BOOLEAN      NOT NULL DEFAULT TRUE,

    created_at               TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at               TIMESTAMPTZ  NOT NULL DEFAULT now(),
    last_active_at           TIMESTAMPTZ,

    refresh_token_hash       VARCHAR(255), -- hashed, not plain
    refresh_token_expires_at TIMESTAMPTZ,

    CONSTRAINT uq_users_email UNIQUE (email)
);

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE
    ON app_users
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

-- Common auth filter: active + not deleted users
CREATE INDEX idx_users_status
    ON app_users (is_active, deleted);

CREATE INDEX idx_users_phone ON app_users (phone);
CREATE INDEX idx_users_email ON app_users (email);
CREATE INDEX idx_users_account_status ON app_users (account_status);

-- Partial: sirf active users — most common query pattern
CREATE INDEX idx_users_active_only
    ON app_users (id) WHERE deleted = FALSE AND is_active = TRUE AND enabled = TRUE;

-- Last seen / session timeout queries
CREATE INDEX idx_users_last_active
    ON app_users (last_active_at DESC) WHERE last_active_at IS NOT NULL;

-- ─────────────────────────────────────────────
-- 4. ROLE_PERMISSIONS  (Many-to-Many)
-- ─────────────────────────────────────────────
CREATE TABLE role_permissions
(
    role_id       UUID NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions (id) ON DELETE CASCADE,

    PRIMARY KEY (role_id, permission_id)
);

CREATE INDEX idx_role_permissions_role ON role_permissions (role_id);
CREATE INDEX idx_role_permissions_permission ON role_permissions (permission_id);

-- ─────────────────────────────────────────────
-- 5. USER_ROLES  (Many-to-Many)
-- ─────────────────────────────────────────────
-- One user can have multiple roles
-- e.g. someone can be both AGENT and USER
-- ─────────────────────────────────────────────
CREATE TABLE user_roles
(
    user_id UUID NOT NULL REFERENCES app_users (id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles (id) ON DELETE CASCADE,

    PRIMARY KEY (user_id, role_id)
);

CREATE INDEX idx_user_roles_user ON user_roles (user_id);
CREATE INDEX idx_user_roles_role ON user_roles (role_id);

-- ==============================
-- 6. ENDPOINT_PERMISSIONS (DYNAMIC RBAC POLICY)
-- ==============================

CREATE TABLE endpoint_permissions
(
    id            UUID PRIMARY KEY      DEFAULT gen_random_uuid(),

    http_method   VARCHAR(10)  NOT NULL,
    path_pattern  VARCHAR(255) NOT NULL,
    permission_id UUID,

    is_public     BOOLEAN      NOT NULL DEFAULT FALSE,
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    deleted       BOOLEAN      NOT NULL DEFAULT FALSE,

    description   TEXT,

    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ,

    CONSTRAINT fk_endpoint_permissions_permission
        FOREIGN KEY (permission_id)
            REFERENCES permissions (id)
            ON DELETE RESTRICT,

    CONSTRAINT chk_endpoint_permissions_method
        CHECK (http_method IN ('GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'OPTIONS', 'HEAD')),

    CONSTRAINT chk_endpoint_permissions_public_rule
        CHECK (
            (is_public = TRUE AND permission_id IS NULL) OR
            (is_public = FALSE AND permission_id IS NOT NULL)
            )
);
CREATE UNIQUE INDEX uk_endpoint_permissions_active_route
    ON endpoint_permissions (http_method, path_pattern) WHERE deleted = FALSE;

-- Fast lookup for runtime auth check.
CREATE INDEX idx_endpoint_permissions_lookup
    ON endpoint_permissions (deleted, enabled, http_method, path_pattern);

-- Fast reverse lookup: where a permission is used.
CREATE INDEX idx_endpoint_permissions_permission
    ON endpoint_permissions (permission_id);

CREATE TABLE authz_cache_version
(
    key        VARCHAR(50) PRIMARY KEY,
    version    BIGINT      NOT NULL DEFAULT 1,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO authz_cache_version (key, version)
VALUES ('GLOBAL', 1);

-- ==============================
-- VERSION BUMP FUNCTION + TRIGGERS
-- ==============================

CREATE
OR REPLACE FUNCTION bump_global_authz_cache_version()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
BEGIN
UPDATE authz_cache_version
SET version    = version + 1,
    updated_at = NOW()
WHERE key = 'GLOBAL';

RETURN NULL;
END;
$$;

-- Bump when endpoint policy changes.
CREATE TRIGGER trg_bump_authz_on_endpoint_permissions
    AFTER INSERT OR
UPDATE OR
DELETE
ON endpoint_permissions
    FOR EACH STATEMENT
    EXECUTE FUNCTION bump_global_authz_cache_version();

-- Bump when role-to-permission mapping changes.
CREATE TRIGGER trg_bump_authz_on_role_permissions
    AFTER INSERT OR
UPDATE OR
DELETE
ON role_permissions
    FOR EACH STATEMENT
    EXECUTE FUNCTION bump_global_authz_cache_version();

-- Bump when user-to-role mapping changes.
CREATE TRIGGER trg_bump_authz_on_user_roles
    AFTER INSERT OR
UPDATE OR
DELETE
ON user_roles
    FOR EACH STATEMENT
    EXECUTE FUNCTION bump_global_authz_cache_version();