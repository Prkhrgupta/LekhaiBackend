-- Database for `./gradlew test -PcucumberDb=local`; Cucumber wipes shops/users here before every scenario.
CREATE DATABASE lekhai_test OWNER lekhai_user;

\c lekhai_test

GRANT ALL ON SCHEMA public TO lekhai_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO lekhai_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO lekhai_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON FUNCTIONS TO lekhai_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TYPES TO lekhai_user;
