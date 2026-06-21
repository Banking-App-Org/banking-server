ALTER TABLE main_server.bank_accounts ALTER COLUMN currency TYPE VARCHAR(3);
ALTER TABLE main_server.transactions  ALTER COLUMN currency TYPE VARCHAR(3);
ALTER TABLE main_server.payments      ALTER COLUMN currency TYPE VARCHAR(3);
