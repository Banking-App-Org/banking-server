-- Create main_server schema
CREATE SCHEMA IF NOT EXISTS main_server;

-- Members table
CREATE TABLE main_server.members (
    id SERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_members_username ON main_server.members(username);
CREATE INDEX idx_members_email ON main_server.members(email);
CREATE INDEX idx_members_status ON main_server.members(status);

-- Policies table
CREATE TABLE main_server.policies (
    id SERIAL PRIMARY KEY,
    member_id INTEGER NOT NULL REFERENCES main_server.members(id) ON DELETE CASCADE,
    policy_number VARCHAR(50) NOT NULL UNIQUE,
    policy_type VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    premium_amount DECIMAL(10, 2),
    currency VARCHAR(3) DEFAULT 'USD',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_policies_member_id ON main_server.policies(member_id);
CREATE INDEX idx_policies_status ON main_server.policies(status);
CREATE INDEX idx_policies_number ON main_server.policies(policy_number);

-- Preferences table (for notification preferences)
CREATE TABLE main_server.preferences (
    id SERIAL PRIMARY KEY,
    member_id INTEGER NOT NULL UNIQUE REFERENCES main_server.members(id) ON DELETE CASCADE,
    phone_number VARCHAR(20),
    email_notifications BOOLEAN DEFAULT true,
    sms_notifications BOOLEAN DEFAULT false,
    push_notifications BOOLEAN DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_preferences_member_id ON main_server.preferences(member_id);

