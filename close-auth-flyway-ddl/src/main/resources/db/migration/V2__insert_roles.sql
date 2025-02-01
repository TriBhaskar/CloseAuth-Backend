DO $$
DECLARE
    role_value userrolesenum;
BEGIN
    FOR role_value IN
        SELECT * FROM unnest(enum_range(NULL::userrolesenum)) -- Fetch all ENUM values
    LOOP
        INSERT INTO close_auth_enterprise_roles (ent_role_id, ent_role_name, ent_role_description)
        VALUES (nextval('seq_enterprise_roles_id'), role_value, 'Automatically inserted role');
    END LOOP;
END $$;
