-- ACCOUNTS TABLE --
ALTER TABLE core.accounts ENABLE ROW LEVEL SECURITY;

CREATE POLICY user_can_read_own_accounts ON core.accounts
    FOR SELECT
    TO wtow_account_mngr
    USING (user_id = NULLIF(current_setting('app.current_user_id', true), '')::uuid);

CREATE POLICY user_can_insert_own_accounts ON core.accounts
    FOR INSERT
    TO wtow_account_mngr
    WITH CHECK (user_id = NULLIF(current_setting('app.current_user_id', true), '')::uuid);


-- USERS TABLE --
ALTER TABLE core.users ENABLE ROW LEVEL SECURITY;

CREATE POLICY user_can_read_own_user_record ON core.users
    FOR SELECT
    TO wtow_user_mngr
    USING (id = NULLIF(current_setting('app.current_user_id', true), '')::uuid);

CREATE POLICY user_mngr_can_insert_new_user ON core.users
    FOR INSERT
    TO wtow_user_mngr
    WITH CHECK (true);

CREATE POLICY user_can_update_own_user_record ON core.users
    FOR UPDATE
    TO wtow_user_mngr
    USING (id = NULLIF(current_setting('app.current_user_id', true), '')::uuid)
    WITH CHECK (id = NULLIF(current_setting('app.current_user_id', true), '')::uuid);



-- PROBLEM with authentication and RLS policies:
--  1. You attempt to authenticate with email and password. You retrieve user with its userId and
--  then use the userId in order to pass RLS Policies.
--  2. You need user_id in order to pass RLS Policy on users table and retrieve user as part of
--  your authentication request.
-- DEADLOCK:
--  You can't authenticate before you know userId (RLS Policy on core.users table).
--  You can't know your userId before you've passed authentication.
-- SOLUTION:
--  SECURITY DEFINER function
CREATE OR REPLACE FUNCTION core.get_user_for_auth(p_email TEXT)
RETURNS TABLE (id UUID, password_hash TEXT)
SECURITY DEFINER -- runs as function owner – wtow_admin. Bypasses RLS if owner bypasses RLS
SET search_path = core
LANGUAGE sql
STABLE
AS $$
    SELECT id, password_hash
    FROM core.users
    WHERE email = p_email;
$$;

REVOKE ALL ON FUNCTION core.get_user_for_auth(TEXT) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION core.get_user_for_auth(TEXT) TO wtow_user_mngr;
