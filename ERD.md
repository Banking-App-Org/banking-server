# ERD — banking-server

> PostgreSQL `banking_server_db` · schema `banking_server` · Flyway V1–V7 (latest: `V7__move_tables_to_banking_server_schema.sql`)
>
> The app runs with `ddl-auto=validate`, so this reflects the live database.

```mermaid
erDiagram

    USERS {
        bigint      id              PK  "BIGSERIAL, auto-increment"
        varchar100  username        UK  "NOT NULL, UNIQUE"
        varchar150  email           UK  "NOT NULL, UNIQUE"
        varchar255  password_hash       "NOT NULL"
        varchar20   phone_number        "nullable"
        varchar100  first_name          "nullable"
        varchar100  last_name           "nullable"
        varchar20   status              "nullable in DB  ACTIVE|INACTIVE  default ACTIVE"
        timestamp   created_at          "NOT NULL  auto-set @PrePersist"
        timestamp   updated_at          "NOT NULL  auto-set @PrePersist @PreUpdate"
    }

    BANK_ACCOUNTS {
        bigint      id              PK  "BIGSERIAL, auto-increment"
        bigint      user_id         FK  "NOT NULL  ref USERS.id  ON DELETE CASCADE"
        varchar30   account_number  UK  "NOT NULL, UNIQUE"
        varchar20   account_type        "NOT NULL  CHECKING|SAVINGS|BUSINESS"
        decimal     balance             "NOT NULL  NUMERIC(15,2)  default 0.00"
        varchar3    currency            "NOT NULL  ISO-4217  default USD"
        varchar20   status              "NOT NULL  ACTIVE|INACTIVE  default ACTIVE"
        timestamp   created_at          "nullable in DB  default NOW()  auto-set @PrePersist"
        timestamp   updated_at          "nullable in DB  default NOW()  auto-set @PrePersist @PreUpdate"
    }

    TRANSACTIONS {
        bigint      id              PK  "BIGSERIAL, auto-increment"
        bigint      account_id      FK  "NOT NULL  ref BANK_ACCOUNTS.id  ON DELETE CASCADE"
        varchar20   type                "NOT NULL  DEPOSIT|WITHDRAWAL|TRANSFER"
        decimal     amount              "NOT NULL  NUMERIC(15,2)"
        varchar3    currency            "NOT NULL  ISO-4217  default USD"
        text        description         "nullable"
        varchar50   reference_id        "nullable  UUID format"
        varchar20   status              "NOT NULL  COMPLETED|PENDING|FAILED  default COMPLETED"
        timestamp   created_at          "nullable in DB  default NOW()  auto-set @PrePersist"
    }

    PAYMENTS {
        bigint      id              PK  "BIGSERIAL, auto-increment"
        bigint      user_id         FK  "NOT NULL  ref USERS.id  ON DELETE CASCADE"
        bigint      account_id      FK  "NOT NULL  ref BANK_ACCOUNTS.id  ON DELETE CASCADE"
        varchar100  payee               "NOT NULL"
        decimal     amount              "NOT NULL  NUMERIC(15,2)"
        varchar3    currency            "NOT NULL  ISO-4217  default USD"
        varchar20   status              "NOT NULL  PROCESSED|PENDING|FAILED  default PENDING"
        date        scheduled_date      "nullable"
        timestamp   processed_at        "nullable"
        timestamp   created_at          "nullable in DB  default NOW()  auto-set @PrePersist"
    }

    PREFERENCES {
        bigint      id              PK  "BIGSERIAL, auto-increment"
        bigint      user_id         FK  UK  "NOT NULL  UNIQUE  ref USERS.id  ON DELETE CASCADE"
        varchar20   phone_number            "nullable"
        boolean     email_notifications     "default true"
        boolean     sms_notifications       "default false"
        boolean     push_notifications      "default false"
        timestamp   created_at              "NOT NULL"
        timestamp   updated_at              "NOT NULL"
    }

    USERS           ||--o{     BANK_ACCOUNTS   : "owns  (user_id FK CASCADE)"
    USERS           ||--o{     PAYMENTS        : "initiates  (user_id FK CASCADE)"
    USERS           ||--o|     PREFERENCES     : "has one  (user_id FK UNIQUE CASCADE)"
    BANK_ACCOUNTS   ||--o{     TRANSACTIONS    : "records  (account_id FK CASCADE)"
    BANK_ACCOUNTS   ||--o{     PAYMENTS        : "source account  (account_id FK CASCADE)"
```

## Indexes

| Table | Indexes |
|-------|---------|
| `users` | `idx_members_username`, `idx_members_email`, `idx_members_status` (names predate V3 rename) |
| `bank_accounts` | `idx_bank_accounts_user_id` |
| `transactions` | `idx_transactions_account_id` |
| `payments` | `idx_payments_user_id`, `idx_payments_status` |
| `preferences` | `idx_preferences_member_id` (name predates V3 rename) |

## Migration history

| Version | File | Change |
|---------|------|--------|
| V1 | `V1__initial_schema.sql` | Initial schema (`main_server`): `members`, `policies`, `preferences` |
| V2 | `V2__fix_id_types.sql` | `SERIAL` → `BIGSERIAL` (int8 ids) |
| V3 | `V3__banking_refactor.sql` | `members` → `users`, drop `policies`, add `bank_accounts`, `transactions`, `payments` |
| V4 | `V4__fix_currency_to_varchar.sql` | `currency CHAR(3)` → `VARCHAR(3)` |
| V5 | `V5__widen_account_number.sql` | `account_number VARCHAR(20)` → `VARCHAR(30)` |
| V6 | `V6__rename_schema_to_banking_server.sql` | Schema `main_server` → `banking_server` |
| V7 | `V7__move_tables_to_banking_server_schema.sql` | Move tables into `banking_server` schema |
