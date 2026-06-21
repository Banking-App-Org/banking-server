-- Migration to fix ID column types from SERIAL (int4) to BIGSERIAL (int8)
-- This migration addresses the schema validation mismatch between Hibernat entity definitions and database schema

-- Drop foreign keys before modifying columns
ALTER TABLE main_server.policies DROP CONSTRAINT policies_member_id_fkey;
ALTER TABLE main_server.preferences DROP CONSTRAINT preferences_member_id_fkey;

-- Modify preference id column from SERIAL to BIGSERIAL equivalent
-- First, we need to create a sequence for the new bigint column
CREATE SEQUENCE main_server.preferences_id_seq_new AS BIGINT;
ALTER TABLE main_server.preferences
    ALTER COLUMN id SET DEFAULT nextval('main_server.preferences_id_seq_new'::regclass),
    ALTER COLUMN id SET DATA TYPE BIGINT;

-- Modify policies table
-- Create a sequence for the new bigint column
CREATE SEQUENCE main_server.policies_id_seq_new AS BIGINT;
ALTER TABLE main_server.policies
    ALTER COLUMN id SET DEFAULT nextval('main_server.policies_id_seq_new'::regclass),
    ALTER COLUMN id SET DATA TYPE BIGINT;

-- Modify members table
-- Create a sequence for the new bigint column
CREATE SEQUENCE main_server.members_id_seq_new AS BIGINT;
ALTER TABLE main_server.members
    ALTER COLUMN id SET DEFAULT nextval('main_server.members_id_seq_new'::regclass),
    ALTER COLUMN id SET DATA TYPE BIGINT;

-- Modify foreign key columns
ALTER TABLE main_server.policies ALTER COLUMN member_id SET DATA TYPE BIGINT;
ALTER TABLE main_server.preferences ALTER COLUMN member_id SET DATA TYPE BIGINT;

-- Re-create foreign keys
ALTER TABLE main_server.policies
    ADD CONSTRAINT policies_member_id_fkey FOREIGN KEY (member_id)
    REFERENCES main_server.members(id) ON DELETE CASCADE;

ALTER TABLE main_server.preferences
    ADD CONSTRAINT preferences_member_id_fkey FOREIGN KEY (member_id)
    REFERENCES main_server.members(id) ON DELETE CASCADE;

-- Clean up old sequences if they exist
DROP SEQUENCE IF EXISTS main_server.members_id_seq;
DROP SEQUENCE IF EXISTS main_server.policies_id_seq;
DROP SEQUENCE IF EXISTS main_server.preferences_id_seq;

-- Rename new sequences to original names
ALTER SEQUENCE main_server.members_id_seq_new RENAME TO members_id_seq;
ALTER SEQUENCE main_server.policies_id_seq_new RENAME TO policies_id_seq;
ALTER SEQUENCE main_server.preferences_id_seq_new RENAME TO preferences_id_seq;

