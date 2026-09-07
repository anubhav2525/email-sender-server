# Email Manager

> A generic, self-hostable, multi-tenant email outreach platform built
> with Spring Boot.

EmailManager allows users to connect their own Gmail or Outlook SMTP
accounts and run personalized bulk or individual email campaigns from a
single platform. It is designed for use cases such as job outreach, HR
communication, event invitations, product announcements, and sales
prospecting.

## Overview

EmailManager separates email delivery into two independent flows:

- **System Mail** --- transactional application emails such as email
  verification, OTPs, password resets, welcome emails, and account
  deletion notices. These are sent through a fixed application-level
  `noreply` account.
- **Campaign Mail** --- user-generated campaign emails sent through
  the user's own Gmail or Outlook SMTP credentials.

A core design principle is that campaign emails are never sent from the
application's own email account. Campaigns are sent through the user's
configured SMTP account.

## Features

- Multi-tenant user workspaces with JWT authentication
- User profiles with dynamic, ordered, visibility-controlled links
- Multiple Gmail/Outlook SMTP accounts per user
- AES-256-GCM encryption for stored SMTP passwords
- Default sending account support
- Reusable HTML/plain-text email templates
- Mustache-style template placeholders
- Flexible JSONB recipient custom fields
- Recipient groups and CSV import
- BULK and INDIVIDUAL campaign types
- Template and campaign attachments
- Inline image embedding
- Per-account daily sending limits and cooldowns
- Randomized inter-email intervals
- Quartz-based email job scheduling
- Retry support with up to 3 attempts
- Email job statuses: `PENDING`, `SENT`, `FAILED`, `SKIPPED`
- Open, click, bounce, and spam-report tracking
- Per-sender unsubscribe management
- Immutable activity/audit logs
- Soft-delete support
- REST API with DTO-based request/response separation

## Tech Stack

---

Layer Technology Purpose

---

- **Language:** Java 21 (Modern Java features,
  records, virtual
  threads)

- **Framework:** Spring Boot 3.x (REST API, dependency
  injection,
  auto-configuration)

- **ORM:** Spring Data JPA + Entity mapping and
  Hibernate 6 persistence

- **Database:** PostgreSQL 16 Primary datastore,
  JSONB, native enums,
  indexes

- **Migrations:** Flyway Versioned database
  migrations

- **Mail:** Jakarta Mail / JavaMail System and dynamic SMTP
  mail delivery

- **Templates:** Thymeleaf System HTML email
  templates

- **Security:** Spring Security + JWT Authentication, refresh
  tokens, authorization Encryption AES-256-GCM / JCE SMTP password
  encryption at rest

- **Scheduling:** Spring Quartz Campaign job dispatch
  and scheduled tasks

- **Cache:** ConcurrentHashMap Per-account
  `JavaMailSender` cache

- **Validation:** Jakarta Bean Validation Request validation

- \*_Build_:\* Maven Build and dependency
  management

---

## Architecture

EmailManager follows a layered monolith architecture based on the standard:

`Controller → Service → Repository`

with a dedicated mail module for system and campaign delivery.

```text
┌──────────────────────────────────────────────────────────────┐
│                         CLIENT LAYER                         │
│              Web App / Mobile App / REST Client              │
└──────────────────────┬───────────────────────────────────────┘
                       │ HTTPS + JWT Bearer Token
┌──────────────────────▼───────────────────────────────────────┐
│                    CONTROLLER LAYER                          │
│       /api/v1/auth  /api/v1/profiles  /api/v1/campaigns ...  │
└──────────────────────┬───────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────┐
│                      SERVICE LAYER                          │
│ UserService  ProfileService  CampaignService                │
│ RecipientService  TemplateService  MailAccountService       │
└────────────┬──────────────────────────────┬─────────────────┘
             │                              │
┌────────────▼─────────────┐   ┌────────────▼────────────────┐
│       MAIL MODULE        │   │       REPOSITORY LAYER      │
│                          │   │                             │
│ SystemMailService        │   │ AppUserRepository           │
│ CampaignMailService      │   │ CampaignRepository          │
│ MailSenderFactory        │   │ EmailJobRepository + ...    │
│ TemplateRenderer         │   │                             │
│ AesEncryptionUtil        │   └────────────┬────────────────┘
└──────────────────────────┘                │
                                            │ Flyway V1–V20
                           ┌────────────────▼────────────────┐
                           │          PostgreSQL 16          │
                           │  19 tables · 7 native enums     │
                           │  40+ indexes · JSONB            │
                           └─────────────────────────────────┘
```

## Database

The database contains **19 tables**, organized into logical layers.
Primary keys use UUIDs and timestamps use `TIMESTAMPTZ`.

### User Layer

---

Table Purpose

---

`app_users` Authentication root, email, BCrypt
password, role, verification and
refresh-token data

`profiles` Sender identity and profile
information

`profile_links` Flexible profile links with label,
URL, visibility and ordering

---

### Content Layer

---

Table Purpose

---

`mail_accounts` User Gmail/Outlook SMTP accounts
and rate-limit settings

`email_templates` Reusable subject/body templates

`attachments` Uploaded documents and images

`template_attachments` Template-to-attachment many-to-many
mapping

---

### Contact & Campaign Layer

Table Purpose

---

`recipients` User contacts with flexible JSONB `custom_fields`
`recipient_groups` Named recipient lists
`recipient_group_members` Recipient-to-group many-to-many mapping
`unsubscribes` Per-sender unsubscribe records
`campaigns` BULK/INDIVIDUAL campaign definitions
`campaign_recipients` Campaign-to-recipient mapping
`campaign_attachments` Campaign-level attachments

### Execution & Analytics Layer

Table Purpose

---

`email_jobs` One job per campaign/recipient pair
`email_events` OPEN, CLICK, BOUNCE and SPAM_REPORT tracking
`activity_logs` Append-only audit trail

Flyway migrations are maintained from **V1 through V20**. The V20
migration adds comprehensive B-Tree, partial and GIN indexes, including
indexes for pending job scheduling, JSONB recipient searches and running
campaigns.

## Dual Mail System

EmailManager intentionally keeps system and campaign mail separate.

### System Mail

System mail uses the Spring Boot auto-configured `JavaMailSender` and
fixed application credentials.

Used for:

- Email verification
- OTP codes
- Password reset links
- Welcome emails
- Account deletion notices

System mail uses Thymeleaf templates and asynchronous dispatch.

### Campaign Mail

Campaign mail uses the user's own SMTP account.

The `MailSenderFactory` creates and caches a `JavaMailSenderImpl` per
`mail_account.id`.

Supported providers:

- Gmail
- Outlook

Campaign sending supports:

- BULK template-based campaigns
- INDIVIDUAL custom emails
- Test connection emails
- Inline images
- Regular attachments
- Unsubscribe checks before dispatch

## SMTP Password Security

User SMTP passwords are never stored as plaintext.

The storage flow is:

```text
User App Password
       ↓
AesEncryptionUtil.encrypt()
       ↓
Generate random 12-byte IV
       ↓
AES-256-GCM encryption
       ↓
Base64(IV + ciphertext + authentication tag)
       ↓
Stored in PostgreSQL
```

During sending:

```text
Encrypted password
       ↓
AesEncryptionUtil.decrypt()
       ↓
AES-256-GCM authentication + decryption
       ↓
Plain SMTP password
       ↓
JavaMailSenderImpl
```

The AES key must be provided through `AES_SECRET_KEY` and must never be
committed to source control.

> **Important:** The AES secret key is exactly 32 characters. Rotating
> this key requires existing encrypted SMTP passwords to be
> re-encrypted.

## Template Engine

EmailManager uses Mustache-style placeholders in both email subjects and
bodies.

Placeholder Resolved From

---

`{{name}}` `recipient.name`
`{{email}}` `recipient.email`
`{{organization}}` `recipient.organization`
`{{role}}` `recipient.role`
`{{custom.KEY}}` `recipient.customFields.get("KEY")`
`{{sender.name}}` Sender profile full name
`{{sender.email}}` Sender profile email
`{{sender.org}}` Sender profile organization
`{{sender.links}}` Visible sender profile links rendered as HTML

Example:

```text
Subject: Regarding the {{role}} position at {{organization}}

Hi {{name}},

I came across {{organization}}'s opening for a {{role}} role and wanted to reach out.

I have {{custom.experience}} of experience in {{custom.skills}}.

Would love to connect!

Best regards,
{{sender.name}}
{{sender.links}}
```

## Package Structure

```text
src/main/java/
└── .../
    ├── enums/
    ├── entity/
    │   └── key/
    ├── repository/
    ├── dto/
    └── mail/
        ├── system/
        ├── campaign/
        ├── factory/
        └── util/
```

### Important Modules

- `enums/` --- PostgreSQL-backed domain enums such as `UserRole`,
  `ProviderType`, `CampaignType`, `CampaignStatus`, `JobStatus`,
  `AttachmentType`, and `EmailEventType`.
- `entity/` --- Hibernate/JPA entity mappings.
- `entity/key/` --- Composite key classes for many-to-many junction
  tables.
- `repository/` --- Spring Data repositories and custom JPQL queries.
- `dto/` --- Separate request and response DTOs with Jakarta
  validation.
- `mail/system/` --- Transactional application email delivery.
- `mail/campaign/` --- User campaign email delivery and template
  rendering.
- `mail/factory/` --- Dynamic SMTP sender creation and caching.
- `mail/util/` --- AES-256-GCM encryption/decryption utilities.

Entities are not exposed directly through API responses. DTO separation
protects internal fields such as encrypted SMTP passwords and
refresh-token data.

## REST API

All API endpoints use the `/api/v1` prefix.

Protected endpoints require:

```http
Authorization: Bearer <access-token>
```

### Authentication

Method Endpoint Description

---

POST `/api/v1/auth/register` Register a new user
POST `/api/v1/auth/login` Login and receive access/refresh JWTs
POST `/api/v1/auth/refresh` Generate a new access token
POST `/api/v1/auth/logout` Invalidate refresh token
POST `/api/v1/auth/verify-email` Verify email
POST `/api/v1/auth/forgot-password` Request password reset
POST `/api/v1/auth/reset-password` Reset password

### Profile & Links

Method Endpoint Description

---

POST `/api/v1/profiles` Create sender profile
GET `/api/v1/profiles/me` Get current profile
PUT `/api/v1/profiles/me` Update profile
POST `/api/v1/profiles/me/links` Add profile link
PATCH `/api/v1/profiles/me/links/{id}` Update profile link
DELETE `/api/v1/profiles/me/links/{id}` Remove profile link
PATCH `/api/v1/profiles/me/links/reorder` Reorder profile links

### Mail Accounts

Method Endpoint Description

---

POST `/api/v1/mail-accounts` Add Gmail/Outlook SMTP account
GET `/api/v1/mail-accounts` List user's mail accounts
PUT `/api/v1/mail-accounts/{id}` Update account settings
POST `/api/v1/mail-accounts/{id}/test` Test SMTP connection
PATCH `/api/v1/mail-accounts/{id}/default` Set default account
DELETE `/api/v1/mail-accounts/{id}` Remove mail account

### Recipients

---

Method Endpoint Description

---

POST `/api/v1/recipients` Add recipient

POST `/api/v1/recipients/import` Import recipients from
CSV

GET `/api/v1/recipients` Search/filter/paginate
recipients

GET `/api/v1/recipients/groups` List recipient groups

POST `/api/v1/recipients/groups` Create recipient group

POST `/api/v1/recipients/groups/{id}/members` Add recipients to group

---

### Campaigns

---

Method Endpoint Description

---

POST `/api/v1/campaigns` Create BULK or
INDIVIDUAL campaign

GET `/api/v1/campaigns` List campaigns

GET `/api/v1/campaigns/{id}/stats` Get campaign analytics

POST `/api/v1/campaigns/{id}/start` Start campaign and
create email jobs

POST `/api/v1/campaigns/{id}/pause` Pause campaign

POST `/api/v1/campaigns/{id}/resume` Resume campaign

GET `/api/v1/campaigns/{id}/jobs` List campaign email
jobs

---

## Scheduling & Retry

When a campaign starts, EmailManager creates trackable `email_jobs` for
campaign recipients.

A job can move through:

```text
PENDING
  ├── SENT
  ├── FAILED
  └── SKIPPED
```

Failed jobs can be retried up to **3 times** with failure reasons
recorded.

Quartz handles scheduled dispatch. Per-account daily limits, batch
cooldowns and randomized inter-email intervals help control sending
behavior and reduce the risk of provider restrictions.

Before dispatching a job, EmailManager also checks whether the recipient has
unsubscribed from the sender. Unsubscribed recipients are automatically
marked `SKIPPED`.

## Tracking & Analytics

Email events are stored per job:

- `OPEN`
- `CLICK`
- `BOUNCE`
- `SPAM_REPORT`

Click events can store the clicked URL in JSONB metadata. Events are
timestamped and can be aggregated for campaign-level analytics.

## Prerequisites

Requirement Version Notes

---

Java JDK 21+ Required
Maven 3.9+ Or use the included Maven wrapper
PostgreSQL 14+ PostgreSQL 16 recommended
Gmail / Outlook --- SMTP App Password required

For Gmail, use an **App Password** rather than the normal Google account
password. Gmail requires 2-Step Verification for App Password usage.

## Installation

### 1. Clone the repository

```bash
git clone https://github.com/your-org/EmailManager.git
cd EmailManager
```

> Replace the repository URL above with the actual project repository
> URL.

### 2. Create the PostgreSQL database

```sql
CREATE DATABASE EmailManager_db;

CREATE USER EmailManager_user
WITH PASSWORD 'your_password';

GRANT ALL PRIVILEGES
ON DATABASE EmailManager_db
TO EmailManager_user;
```

### 3. Configure environment variables

Create a `.env` file or configure the variables in your deployment
environment.

```env
DB_URL=jdbc:postgresql://localhost:5432/EmailManager_db
DB_USERNAME=EmailManager_user
DB_PASSWORD=your_password

JWT_SECRET=your-minimum-256-bit-random-secret
AES_SECRET_KEY=your-32-character-secret-key

SYSTEM_MAIL_USERNAME=noreply@yourapp.com
SYSTEM_MAIL_APP_PASSWORD=your-system-mail-app-password

FRONTEND_URL=http://localhost:3000

JWT_ACCESS_EXPIRY_MS=900000
JWT_REFRESH_EXPIRY_MS=604800000
```

---

### 4. Run the application

Using the Maven wrapper:

```bash
./mvnw spring-boot:run
```

Or build and run the JAR:

```bash
./mvnw clean package -DskipTests
java -jar target/EmailManager-1.0.0.jar
```

Flyway automatically applies the database migrations on startup.

### 5. Verify the application

```bash
curl http://localhost:8080/api/v1/health
```

Expected response:

```json
{
  "status": "UP",
  "database": "UP"
}
```

## Development

For development with Spring DevTools hot reload:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## Docker

Docker Compose can be used to run PostgreSQL and the application
together.

Example database/application services:

```yaml
services:
  db:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: EmailManager_db
      POSTGRES_USER: EmailManager_user
      POSTGRES_PASSWORD: EmailManager_pass
    ports:
      - "5432:5432"

  app:
    build: .
    ports:
      - "8080:8080"
    depends_on:
      - db
    env_file:
      - .env
```

Start the stack:

```bash
docker-compose up -d
```

## Flyway Migrations

All migrations from `V1` through `V20` are applied automatically when
the application starts.

To run migrations manually:

```bash
./mvnw flyway:migrate
```

After startup, verify the `flyway_schema_history` table and confirm the
migrations completed successfully.

## Security Notes

- Never commit `.env`, SMTP passwords, JWT secrets or AES keys.
- SMTP passwords are encrypted using AES-256-GCM before database
  storage.
- The AES key must be kept outside source code and `application.yml`.
- Access and refresh token handling is implemented through Spring
  Security and JWT.
- API responses use DTOs instead of exposing JPA entities directly.
- Unsubscribe checks occur before campaign job dispatch.
- Audit logs are append-only.

## Project Status

**Version:** `1.0`\
**Status:** Active Development
