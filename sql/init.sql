-- Create the role
CREATE ROLE lekhai_user WITH
  CREATEDB
  CREATEROLE
  LOGIN
  PASSWORD 'password';

-- Change database ownership
ALTER DATABASE lekhai OWNER TO lekhai_user;

-- Grant database-level privileges
GRANT ALL PRIVILEGES ON DATABASE lekhai TO lekhai_user;

-- ⚠️ IMPORTANT: Connect to the lekhai database
\c lekhai

-- Grant all on public schema
GRANT ALL ON SCHEMA public TO lekhai_user;
GRANT USAGE ON SCHEMA public TO lekhai_user;
GRANT CREATE ON SCHEMA public TO lekhai_user;

-- Grant all on all existing tables
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO lekhai_user;

-- Grant all on all sequences
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO lekhai_user;

-- Grant all on all functions
GRANT ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA public TO lekhai_user;

-- ✅ IMPORTANT: Set default privileges for future objects
ALTER DEFAULT PRIVILEGES IN SCHEMA public
  GRANT ALL ON TABLES TO lekhai_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA public
  GRANT ALL ON SEQUENCES TO lekhai_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA public
  GRANT ALL ON FUNCTIONS TO lekhai_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA public
  GRANT ALL ON TYPES TO lekhai_user;