-- Mirrors sql/init.sql: Flyway owns objects as postgres, the app connects as non-superuser lekhai_user so RLS applies.
CREATE ROLE lekhai_user LOGIN PASSWORD 'password';

GRANT ALL ON SCHEMA public TO lekhai_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO lekhai_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO lekhai_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON FUNCTIONS TO lekhai_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TYPES TO lekhai_user;
