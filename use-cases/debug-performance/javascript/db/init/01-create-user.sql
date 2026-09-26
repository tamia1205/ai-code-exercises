-- Note: The app_user role is already created by PostgreSQL 
-- using the POSTGRES_USER environment variable.
-- This script just ensures the user has the necessary permissions.

-- Ensure the app_user has all the required privileges for the application
ALTER ROLE app_user WITH 
    SUPERUSER
    CREATEDB
    CREATEROLE
    INHERIT
    REPLICATION
    CONNECTION LIMIT -1;

-- Grant privileges on database
GRANT ALL PRIVILEGES ON DATABASE ecommerce TO app_user;

-- Make sure app_user owns the database
ALTER DATABASE ecommerce OWNER TO app_user;

-- Connect to the ecommerce database to set schema permissions
\c ecommerce;

-- Grant permissions on schema level (will be applied to tables created later)
ALTER DEFAULT PRIVILEGES FOR ROLE app_user IN SCHEMA public 
GRANT ALL PRIVILEGES ON TABLES TO app_user;

ALTER DEFAULT PRIVILEGES FOR ROLE app_user IN SCHEMA public 
GRANT ALL PRIVILEGES ON SEQUENCES TO app_user;

ALTER DEFAULT PRIVILEGES FOR ROLE app_user IN SCHEMA public 
GRANT ALL PRIVILEGES ON FUNCTIONS TO app_user;

