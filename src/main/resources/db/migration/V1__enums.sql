-- ════════════════════════════════════════════════════
-- STEP 1: PostgreSQL Native Enum Types
-- ════════════════════════════════════════════════════
CREATE TYPE provider_type AS ENUM ('GMAIL', 'OUTLOOK');
CREATE TYPE campaign_type AS ENUM ('BULK', 'INDIVIDUAL');
CREATE TYPE campaign_status AS ENUM ('DRAFT', 'RUNNING', 'PAUSED', 'COMPLETED');
CREATE TYPE job_status AS ENUM ('PENDING', 'SENT', 'FAILED', 'SKIPPED');
CREATE TYPE attachment_type AS ENUM ('IMAGE', 'DOCUMENT', 'OTHER');
CREATE TYPE email_event_type AS ENUM ('OPEN', 'CLICK', 'BOUNCE', 'SPAM_REPORT');
CREATE TYPE account_status_type AS ENUM ('PENDING','APPROVE','REJECT');