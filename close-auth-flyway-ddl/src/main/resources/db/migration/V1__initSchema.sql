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

-- Enterprise Details table
CREATE TABLE enterprise_details (
    ent_id BIGINT DEFAULT nextval('seq_enterprise_details_id') PRIMARY KEY,
    ent_name VARCHAR(100) NOT NULL,
    ent_email VARCHAR(100) NOT NULL,  -- Increased length for longer domain names
    ent_contact_number VARCHAR(20) NOT NULL,  -- Increased to accommodate international numbers
    ent_address VARCHAR(500),
    ent_license_active VARCHAR(20) NOT NULL DEFAULT 'INACTIVE',
    ent_license_type VARCHAR(20) NOT NULL DEFAULT 'FREE',
    ent_max_users INTEGER DEFAULT 1,
    ent_timezone VARCHAR(50) DEFAULT 'UTC',  -- Added timezone
    ent_created_by VARCHAR(50) NOT NULL,
    ent_created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    ent_updated_by VARCHAR(50),
    ent_updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_enterprise_email UNIQUE (ent_email),
    CONSTRAINT uk_ent_contact_number UNIQUE (ent_contact_number),
    CONSTRAINT chk_ent_license_active CHECK (ent_license_active IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT chk_ent_license_type CHECK (ent_license_type IN ('FREE', 'BASIC', 'PREMIUM', 'ENTERPRISE'))
);

-- Enterprise Roles table
CREATE TABLE enterprise_roles (
    ent_role_id BIGINT DEFAULT nextval('seq_enterprise_roles_id') PRIMARY KEY,
    ent_id BIGINT NOT NULL,
    ent_role_name VARCHAR(50) NOT NULL,
    ent_role_description TEXT,
    ent_role_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    ent_role_permissions JSONB DEFAULT '{}',
    ent_role_created_by VARCHAR(50) NOT NULL,
    ent_role_created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    ent_role_updated_by VARCHAR(50),
    ent_role_updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    ent_role_deleted_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uk_enterprise_role UNIQUE (ent_id, ent_role_name),  -- Changed to per-enterprise unique roles
    CONSTRAINT fk_enterprise_id FOREIGN KEY (ent_id)
        REFERENCES enterprise_details(ent_id) ON DELETE CASCADE,
    CONSTRAINT chk_role_status CHECK (ent_role_status IN ('ACTIVE', 'INACTIVE'))
);

-- Enterprise Users table
CREATE TABLE enterprise_users (
    ent_user_id BIGINT DEFAULT nextval('seq_enterprise_users_id') PRIMARY KEY,
    ent_id BIGINT NOT NULL,
    ent_user_email VARCHAR(100) NOT NULL,
    ent_user_user_name VARCHAR(50) NOT NULL,
    ent_user_password_hash VARCHAR(255) NOT NULL,
    ent_user_first_name VARCHAR(50),
    ent_user_last_name VARCHAR(50),
    ent_user_role BIGINT NOT NULL,
    ent_user_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    ent_user_last_login_at TIMESTAMP WITH TIME ZONE,
    ent_user_failed_login_attempts INTEGER DEFAULT 0,
    ent_user_last_password_change TIMESTAMP WITH TIME ZONE,
    ent_user_created_by VARCHAR(50) NOT NULL,
    ent_user_created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    ent_user_updated_by VARCHAR(50),
    ent_user_updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    ent_user_deleted_at TIMESTAMP WITH TIME ZONE,  -- Soft delete support
    CONSTRAINT fk_enterprise FOREIGN KEY (ent_id)
        REFERENCES enterprise_details(ent_id) ON DELETE CASCADE,
    CONSTRAINT fk_role_id FOREIGN KEY (ent_user_role)
        REFERENCES enterprise_roles(ent_role_id) ON DELETE RESTRICT,
    CONSTRAINT uk_enterprise_user_email UNIQUE (ent_id, ent_user_email),  -- Changed to per-enterprise unique email
    CONSTRAINT chk_ent_user_status CHECK (ent_user_status IN ('ACTIVE', 'INACTIVE', 'LOCKED'))
);

-- Indexes
CREATE INDEX idx_enterprise_name ON enterprise_details(ent_name);
CREATE INDEX idx_enterprise_email ON enterprise_details(ent_email);
CREATE INDEX idx_enterprise_license_active ON enterprise_details(ent_license_active);

CREATE INDEX idx_enterprise_users_email ON enterprise_users(ent_user_email);
CREATE INDEX idx_enterprise_users_username ON enterprise_users(ent_user_user_name);
CREATE INDEX idx_enterprise_users_status ON enterprise_users(ent_user_status);
CREATE INDEX idx_enterprise_users_role ON enterprise_users(ent_user_role);

CREATE INDEX idx_enterprise_roles_name ON enterprise_roles(ent_role_name);
CREATE INDEX idx_enterprise_roles_status ON enterprise_roles(ent_role_status);
CREATE INDEX idx_enterprise_roles_deleted_at ON enterprise_roles(ent_role_deleted_at);

-- Trigger function for updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_TABLE_NAME = 'enterprise_details' THEN
        NEW.ent_updated_at = CURRENT_TIMESTAMP;
    ELSIF TG_TABLE_NAME = 'enterprise_users' THEN
        NEW.ent_user_updated_at = CURRENT_TIMESTAMP;
    ELSIF TG_TABLE_NAME = 'enterprise_roles' THEN
        NEW.ent_role_updated_at = CURRENT_TIMESTAMP;
    END IF;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create triggers
CREATE TRIGGER update_enterprise_details_updated_at
    BEFORE UPDATE ON enterprise_details
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_enterprise_users_updated_at
    BEFORE UPDATE ON enterprise_users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_enterprise_roles_updated_at
    BEFORE UPDATE ON enterprise_roles
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Comments
COMMENT ON TABLE enterprise_details IS 'Stores enterprise-level information for the authentication system';
COMMENT ON TABLE enterprise_users IS 'Stores user information associated with enterprises';
COMMENT ON TABLE enterprise_roles IS 'Stores role definitions for enterprise users';

COMMENT ON COLUMN enterprise_details.ent_id IS 'Primary key for enterprise details';
COMMENT ON COLUMN enterprise_details.ent_name IS 'Name of the enterprise';
COMMENT ON COLUMN enterprise_details.ent_email IS 'Primary email domain for the enterprise';
COMMENT ON COLUMN enterprise_details.ent_timezone IS 'Default timezone for the enterprise';

COMMENT ON COLUMN enterprise_users.ent_user_id IS 'Primary key for enterprise users';
COMMENT ON COLUMN enterprise_users.ent_user_email IS 'Email address of the user';
COMMENT ON COLUMN enterprise_users.ent_user_failed_login_attempts IS 'Number of consecutive failed login attempts';

COMMENT ON COLUMN enterprise_roles.ent_role_id IS 'Primary key for enterprise roles';
COMMENT ON COLUMN enterprise_roles.ent_role_name IS 'Name identifier for the role';
COMMENT ON COLUMN enterprise_roles.ent_role_permissions IS 'JSONB field storing role permissions';