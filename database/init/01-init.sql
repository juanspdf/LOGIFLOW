-- Create databases for LogiFlow microservices
-- PostgreSQL does not support IF NOT EXISTS for CREATE DATABASE

CREATE DATABASE logiflow_auth;
CREATE DATABASE logiflow_pedidos;
CREATE DATABASE logiflow_fleet;
CREATE DATABASE logiflow_billing;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE logiflow_auth TO postgres;
GRANT ALL PRIVILEGES ON DATABASE logiflow_pedidos TO postgres;
GRANT ALL PRIVILEGES ON DATABASE logiflow_fleet TO postgres;
GRANT ALL PRIVILEGES ON DATABASE logiflow_billing TO postgres;

-- Note: Tables will be created by Hibernate ddl-auto=update in each service
