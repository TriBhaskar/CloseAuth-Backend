package com.anterka.closeauth.constants;

/**
 * This is a table constants class that holds all the tables in the close-auth
 * [{@link CloseAuthTables}] cannot be instantiated
 * */
public class CloseAuthTables {

    private CloseAuthTables(){}
    /**
     * Holds the [close_auth_enterprise_details] table attributes
     * */
    public static class EnterpriseDetails {
        public static final String TABLE_NAME = "close_auth_enterprise_details";
        public static final String SEQUENCE_NAME = "seq_enterprise_details_id";
        /**
         * [close_auth_enterprise_details] table's column names below
         * */
        public static final String ID = "ent_id";
        public static final String NAME = "ent_name";
        public static final String EMAIL = "ent_email";
        public static final String CONTACT = "ent_contact_number";
        public static final String COUNTRY = "ent_country";
        public static final String STATE = "ent_state";
        public static final String CITY = "ent_city";
        public static final String ADDRESS = "ent_address";
        public static final String PIN_CODE = "ent_pin_code";
        public static final String CREATED_BY = "ent_created_by";
        public static final String CREATED_AT = "ent_created_at";
        public static final String UPDATED_BY = "ent_updated_by";
        public static final String UPDATED_AT = "ent_updated_at";
        private EnterpriseDetails(){}
    }
    /**
     * Holds the [close_auth_enterprise_roles] table attributes
     * */
    public static class EnterpriseUserRoles{
        public static final String TABLE_NAME= "close_auth_enterprise_roles";
        public static final String ID = "ent_role_id";
        public static final String ROLE = "ent_role_name";
        public static final String DESCRIPTION = "ent_role_description";
        private EnterpriseUserRoles(){}
    }

    /**
     * Holds the [close_auth_enterprise_users] table attributes
     * */
    public static class EnterpriseUsers{
        public static final String TABLE_NAME = "close_auth_enterprise_users";
        public static final String SEQUENCE_NAME = "seq_enterprise_users_id";
        /**
         * [close_auth_enterprise_users] table's column names below
         * */
        public static final String ID = "ent_user_id";
        public static final String ENT_ID = "ent_id";
        public static final String FIRST_NAME = "ent_user_first_name";
        public static final String LAST_NAME = "ent_user_last_name";
        public static final String USER_NAME = "ent_user_user_name";
        public static final String EMAIL = "ent_user_email";
        public static final String PASSWORD = "ent_user_password";
        public static final String ROLE = "ent_user_role_id";
        public static final String STATUS = "ent_user_status";
        public static final String LAST_LOGIN = "ent_user_last_login_at";
        public static final String FAILED_LOGIN_ATTEMPTS = "ent_user_failed_login_attempts";
        public static final String LAST_PASSWORD_CHANGED_AT = "ent_user_last_password_changed_at";
        public static final String CREATED_BY = "ent_user_created_by";
        public static final String CREATED_AT = "ent_user_created_at";
        public static final String UPDATED_BY = "ent_user_updated_by";
        public static final String UPDATED_AT = "ent_user_updated_at";
        private EnterpriseUsers(){}
    }
}
