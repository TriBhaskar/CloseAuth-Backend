-- Create sequences
CREATE SEQUENCE seq_enterprise_details_id
    INCREMENT 1
    START 1
    MINVALUE 1
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE seq_enterprise_users_id
    INCREMENT 1
    START 1
    MINVALUE 1
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE seq_enterprise_roles_id
    INCREMENT 1
    START 1
    MINVALUE 1
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE seq_close_auth_licenses_id
    INCREMENT 1
    START 1
    MINVALUE 1
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE seq_close_auth_license_allocation_id
    INCREMENT 1
    START 1
    MINVALUE 1
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE seq_close_auth_transaction_details_id
    INCREMENT 1
    START 1
    MINVALUE 1
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE seq_close_auth_enterprise_applications_id
    INCREMENT 1
    START 1
    MINVALUE 1
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE seq_close_auth_enterprise_application_users_id
    INCREMENT 1
    START 1
    MINVALUE 1
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE seq_close_auth_enterprise_application_user_access_id
    INCREMENT 1
    START 1
    MINVALUE 1
    NO MAXVALUE
    CACHE 1;

-- ENUMS
CREATE TYPE enterprise_user_status_enum AS ENUM ('BLOCKED', 'UNBLOCKED');
CREATE TYPE license_type_enum AS ENUM ('FREE', 'BASIC', 'PREMIUM', 'ENTERPRISE');
CREATE TYPE transaction_status_enum AS ENUM ('SUCCESS', 'FAILED');
CREATE TYPE allocated_license_status_enum AS ENUM ('ACTIVE', 'EXPIRED');
CREATE TYPE user_roles_enum AS ENUM('ADMIN', 'USER', 'ORGANIZATION');
CREATE TYPE app_users_user_status_enum AS ENUM ('ACTIVE', 'INACTIVE');

-- Enterprise Details table
CREATE TABLE close_auth_enterprise_details (
    ent_id BIGINT DEFAULT nextval('seq_enterprise_details_id') PRIMARY KEY,
    ent_name VARCHAR(100) NOT NULL,
    ent_email VARCHAR(100) NOT NULL,
    ent_contact_number VARCHAR(10) NOT NULL,
    ent_country VARCHAR(50) NOT NULL,
    ent_state VARCHAR(50) NOT NULL,
    ent_address VARCHAR(500),
    ent_pin_code INTEGER NOT NULL,
    ent_created_by VARCHAR(50) NOT NULL,
    ent_created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    ent_updated_by VARCHAR(50),
    ent_updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_enterprise_email UNIQUE (ent_email),
    CONSTRAINT uk_ent_contact_number UNIQUE (ent_contact_number)
);

-- Enterprise Roles table
CREATE TABLE close_auth_enterprise_roles (
    ent_role_id BIGINT DEFAULT nextval('seq_enterprise_roles_id') PRIMARY KEY,
    ent_role_name user_roles_enum NOT NULL DEFAULT 'USER',
    ent_role_description TEXT
);

-- Enterprise Users table
CREATE TABLE close_auth_enterprise_users (
    ent_user_id BIGINT DEFAULT nextval('seq_enterprise_users_id') PRIMARY KEY,
    ent_id BIGINT NOT NULL,
    ent_user_first_name VARCHAR(50),
    ent_user_last_name VARCHAR(50),
    ent_user_user_name VARCHAR(50) NOT NULL,
    ent_user_email VARCHAR(100) NOT NULL,
    ent_user_password VARCHAR(255) NOT NULL,
    ent_user_role_id BIGINT NOT NULL,
    ent_user_status enterprise_user_status_enum NOT NULL DEFAULT 'UNBLOCKED',
    ent_user_last_login_at TIMESTAMP WITH TIME ZONE,
    ent_user_failed_login_attempts INTEGER DEFAULT 0,
    ent_user_last_password_changed_At TIMESTAMP WITH TIME ZONE,
    ent_user_created_by VARCHAR(50) NOT NULL,
    ent_user_created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    ent_user_updated_by VARCHAR(50),
    ent_user_updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_enterprise FOREIGN KEY (ent_id)
        REFERENCES close_auth_enterprise_details(ent_id) ON DELETE CASCADE,
    CONSTRAINT fk_role_id FOREIGN KEY (ent_user_role_id)
        REFERENCES close_auth_enterprise_roles(ent_role_id) ON DELETE RESTRICT,
    CONSTRAINT uk_enterprise_user_email UNIQUE (ent_id, ent_user_email)
);

-- Enterprise applications table
CREATE TABLE close_auth_enterprise_applications (
    ent_app_id BIGINT DEFAULT nextval('seq_close_auth_enterprise_applications_id') PRIMARY KEY,
    ent_id BIGINT NOT NULL,
    ent_app_name VARCHAR(50) NOT NULL,
    ent_app_createdBy BIGINT NOT NULL,
    ent_app_description VARCHAR(150) NOT NULL,
    ent_app_key VARCHAR(20) NOT NULL,
    ent_app_tech_type VARCHAR(100) NOT NULL,
    ent_app_created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ent_app_updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_close_auth_enterprise_details_id FOREIGN KEY (ent_id)
        REFERENCES close_auth_enterprise_details(ent_id),
    CONSTRAINT fk_close_auth_enterprise_users_id FOREIGN KEY (ent_app_createdBy)
        REFERENCES close_auth_enterprise_users(ent_user_id)
);

-- License types table
CREATE TABLE close_auth_license_types (
    license_type_id SERIAL NOT NULL PRIMARY KEY,
    license_type license_type_enum NOT NULL DEFAULT 'FREE',
    license_max_active_users INTEGER NOT NULL DEFAULT 0,
    license_max_applications INTEGER NOT NULL DEFAULT 0,
    license_price INTEGER NOT NULL DEFAULT 0,
    license_expiry_months INTEGER NOT NULL DEFAULT 0
);

-- License table
CREATE TABLE close_auth_licenses (
    license_id BIGINT DEFAULT nextval('seq_close_auth_licenses_id') PRIMARY KEY,
    license_type_id INTEGER NOT NULL,
    license_key VARCHAR(100) NOT NULL,
    license_allocated BOOLEAN NOT NULL DEFAULT FALSE,
    license_addedAt TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    license_valid_upto_date TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_license_type_id FOREIGN KEY (license_type_id)
        REFERENCES close_auth_license_types(license_type_id),
    CONSTRAINT uk_license_key UNIQUE (license_key)
);

-- Transaction details table
CREATE TABLE close_auth_transaction_details (
    transaction_details_id BIGINT DEFAULT nextval('seq_close_auth_transaction_details_id') PRIMARY KEY,
    ent_id BIGINT NOT NULL,
    license_type_id INTEGER NOT NULL,
    transaction_id VARCHAR(100) NOT NULL,
    transaction_mode VARCHAR(50) NOT NULL,
    transaction_amount INTEGER NOT NULL,
    transaction_currency VARCHAR(10) NOT NULL,
    transaction_status transaction_status_enum NOT NULL,
    transaction_failure_reason VARCHAR(100),
    transaction_location VARCHAR(100) NOT NULL,
    transaction_created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_enterprise_id FOREIGN KEY (ent_id)
        REFERENCES close_auth_enterprise_details(ent_id),
    CONSTRAINT fk_license_type_id FOREIGN KEY (license_type_id)
        REFERENCES close_auth_license_types(license_type_id)
);

-- License allocation table
CREATE TABLE close_auth_license_allocation (
    license_allocation_id BIGINT DEFAULT nextval('seq_close_auth_license_allocation_id') PRIMARY KEY,
    license_id BIGINT NOT NULL,
    transaction_details_id BIGINT NOT NULL,
    license_allocatedAt TIMESTAMP WITH TIME ZONE NOT NULL,
    license_status allocated_license_status_enum NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT fk_close_auth_license_id FOREIGN KEY (license_id)
        REFERENCES close_auth_licenses(license_id),
    CONSTRAINT fk_close_auth_transaction_details_id FOREIGN KEY (transaction_details_id)
        REFERENCES close_auth_transaction_details(transaction_details_id)
);

-- Enterprise application users table
CREATE TABLE close_auth_enterprise_application_users (
    ent_app_users_user_id BIGINT DEFAULT nextval('seq_close_auth_enterprise_application_users_id') PRIMARY KEY,
    ent_id BIGINT NOT NULL,
    ent_app_users_username VARCHAR(20) NOT NULL,
    ent_app_users_user_password VARCHAR(20) NOT NULL,
    ent_app_users_user_first_name VARCHAR(20) NOT NULL,
    ent_app_users_user_last_name VARCHAR(20) NOT NULL,
    ent_app_users_user_role_id user_roles_enum NOT NULL,
    ent_app_users_user_email VARCHAR(50) NOT NULL,
    ent_app_users_user_status app_users_user_status_enum NOT NULL DEFAULT 'INACTIVE',
    ent_app_users_user_created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ent_app_users_user_created_by BIGINT NOT NULL,
    ent_app_users_user_updated_at TIMESTAMP WITH TIME ZONE,
    ent_app_users_user_updated_by BIGINT,
    CONSTRAINT fk_enterprise_user_id FOREIGN KEY (ent_app_users_user_created_by)
        REFERENCES close_auth_enterprise_users(ent_user_id),
    CONSTRAINT fk_enterprise_user_update_id FOREIGN KEY (ent_app_users_user_updated_by)
        REFERENCES close_auth_enterprise_users(ent_user_id)
);

-- Application user access table
CREATE TABLE close_auth_enterprise_application_user_access (
    ent_app_user_access_id BIGINT DEFAULT nextval('seq_close_auth_enterprise_application_user_access_id') PRIMARY KEY,
    ent_app_id BIGINT NOT NULL,
    ent_app_users_user_id BIGINT NOT NULL,
    ent_app_user_access_providedAt TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ent_app_user_access_providedBy BIGINT NOT NULL,
    CONSTRAINT fk_enterprise_app_id FOREIGN KEY (ent_app_id)
        REFERENCES close_auth_enterprise_applications(ent_app_id),
    CONSTRAINT fk_enterprise_app_user_id FOREIGN KEY (ent_app_users_user_id)
        REFERENCES close_auth_enterprise_application_users(ent_app_users_user_id),
    CONSTRAINT fk_enterprise_app_user_access_providedById FOREIGN KEY (ent_app_user_access_providedBy)
        REFERENCES close_auth_enterprise_application_users(ent_app_users_user_id)
);

-- Indexes
CREATE INDEX idx_enterprise_name ON close_auth_enterprise_details(ent_name);
CREATE INDEX idx_enterprise_email ON close_auth_enterprise_details(ent_email);
CREATE INDEX idx_enterprise_roles_name ON close_auth_enterprise_roles(ent_role_name);
CREATE INDEX idx_enterprise_users_email ON close_auth_enterprise_users(ent_user_email);
CREATE INDEX idx_enterprise_users_username ON close_auth_enterprise_users(ent_user_user_name);
CREATE INDEX idx_enterprise_users_status ON close_auth_enterprise_users(ent_user_status);
CREATE INDEX idx_enterprise_applications_app_name ON close_auth_enterprise_applications(ent_app_name);
CREATE INDEX idx_enterprise_applications_app_createdBy ON close_auth_enterprise_applications(ent_app_createdBy);
CREATE INDEX idx_enterprise_applications_app_key ON close_auth_enterprise_applications(ent_app_key);
CREATE INDEX idx_license_type ON close_auth_license_types(license_type);
CREATE INDEX idx_license_id ON close_auth_licenses(license_id);
CREATE INDEX idx_transaction_enterprise_id ON close_auth_transaction_details(ent_id);
CREATE INDEX idx_transaction_id ON close_auth_transaction_details(transaction_id);
CREATE INDEX idx_transaction_location ON close_auth_transaction_details(transaction_location);
CREATE INDEX idx_license_allocation_id ON close_auth_license_allocation(license_allocation_id);
CREATE INDEX idx_license_id_allocation ON close_auth_license_allocation(license_id);
CREATE INDEX idx_enterprise_application_users_user_id ON close_auth_enterprise_application_users(ent_app_users_user_id);
CREATE INDEX idx_enterprise_application_users_username ON close_auth_enterprise_application_users(ent_app_users_username);
CREATE INDEX idx_enterprise_application_user_access_id ON close_auth_enterprise_application_user_access(ent_app_user_access_id);

-- Trigger function for updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_TABLE_NAME = 'close_auth_enterprise_details' THEN
        NEW.ent_updated_at = CURRENT_TIMESTAMP;
    ELSIF TG_TABLE_NAME = 'close_auth_enterprise_users' THEN
        NEW.ent_user_updated_at = CURRENT_TIMESTAMP;
    ELSIF TG_TABLE_NAME = 'close_auth_enterprise_applications' THEN
        NEW.ent_app_updated_at = CURRENT_TIMESTAMP;
    ELSIF TG_TABLE_NAME = 'close_auth_enterprise_application_users' THEN
        NEW.ent_app_users_user_updated_at = CURRENT_TIMESTAMP;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

-- Triggers
CREATE TRIGGER set_updated_at_enterprise_details
BEFORE UPDATE ON close_auth_enterprise_details
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER set_updated_at_enterprise_users
BEFORE UPDATE ON close_auth_enterprise_users
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER set_updated_at_enterprise_applications
BEFORE UPDATE ON close_auth_enterprise_applications
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER set_updated_at_enterprise_application_users
BEFORE UPDATE ON close_auth_enterprise_application_users
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Comments
COMMENT ON TABLE close_auth_enterprise_details IS 'Stores enterprise-level information for the authentication system';
COMMENT ON TABLE close_auth_enterprise_users IS 'Stores user information associated with enterprises';
COMMENT ON TABLE close_auth_enterprise_roles IS 'Stores role definitions for enterprise users';

COMMENT ON COLUMN close_auth_enterprise_details.ent_id IS 'Primary key for enterprise details';
COMMENT ON COLUMN close_auth_enterprise_details.ent_name IS 'Name of the enterprise';
COMMENT ON COLUMN close_auth_enterprise_users.ent_user_failed_login_attempts IS 'Number of consecutive failed login attempts';

COMMENT ON COLUMN close_auth_enterprise_roles.ent_role_id IS 'Primary key for enterprise roles';
COMMENT ON COLUMN close_auth_enterprise_roles.ent_role_name IS 'Name identifier for the role';