-- Insert roles into close_auth_enterprise_roles dynamically using ENUM values
DO $$
DECLARE
    role_value user_roles_enum;
BEGIN
    FOR role_value IN
        SELECT unnest(enum_range(NULL::user_roles_enum)) -- Fetch all ENUM values
    LOOP
        INSERT INTO close_auth_enterprise_roles (ent_role_id, ent_role_name, ent_role_description)
        VALUES (nextval('seq_enterprise_roles_id'), role_value, 'Automatically inserted role');
    END LOOP;
END $$;
