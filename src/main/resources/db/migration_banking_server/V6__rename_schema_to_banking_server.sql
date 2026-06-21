-- Rename schema from main_server to banking_server (idempotent)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.schemata WHERE schema_name = 'main_server')
       AND NOT EXISTS (SELECT 1 FROM information_schema.schemata WHERE schema_name = 'banking_server') THEN
        ALTER SCHEMA main_server RENAME TO banking_server;
    END IF;
END $$;
