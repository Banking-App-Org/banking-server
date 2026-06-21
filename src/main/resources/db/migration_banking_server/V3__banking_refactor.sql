-- Rename members → users
ALTER TABLE main_server.members RENAME TO users;

-- Drop insurance-specific tables
DROP TABLE IF EXISTS main_server.policies;

-- Update preferences FK
ALTER TABLE main_server.preferences RENAME COLUMN member_id TO user_id;

-- Create bank_accounts table
CREATE TABLE main_server.bank_accounts (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES main_server.users(id) ON DELETE CASCADE,
  account_number VARCHAR(20) UNIQUE NOT NULL,
  account_type VARCHAR(20) NOT NULL,
  balance NUMERIC(15,2) NOT NULL DEFAULT 0.00,
  currency CHAR(3) NOT NULL DEFAULT 'USD',
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);

-- Create transactions table
CREATE TABLE main_server.transactions (
  id BIGSERIAL PRIMARY KEY,
  account_id BIGINT NOT NULL REFERENCES main_server.bank_accounts(id) ON DELETE CASCADE,
  type VARCHAR(20) NOT NULL,
  amount NUMERIC(15,2) NOT NULL,
  currency CHAR(3) NOT NULL DEFAULT 'USD',
  description TEXT,
  reference_id VARCHAR(50),
  status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
  created_at TIMESTAMP DEFAULT NOW()
);

-- Create payments table
CREATE TABLE main_server.payments (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES main_server.users(id) ON DELETE CASCADE,
  account_id BIGINT NOT NULL REFERENCES main_server.bank_accounts(id) ON DELETE CASCADE,
  payee VARCHAR(100) NOT NULL,
  amount NUMERIC(15,2) NOT NULL,
  currency CHAR(3) NOT NULL DEFAULT 'USD',
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  scheduled_date DATE,
  processed_at TIMESTAMP,
  created_at TIMESTAMP DEFAULT NOW()
);

-- Create indexes for performance
CREATE INDEX idx_bank_accounts_user_id ON main_server.bank_accounts(user_id);
CREATE INDEX idx_transactions_account_id ON main_server.transactions(account_id);
CREATE INDEX idx_payments_user_id ON main_server.payments(user_id);
CREATE INDEX idx_payments_status ON main_server.payments(status);
