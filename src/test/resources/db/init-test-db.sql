CREATE ROLE wtow_admin WITH LOGIN PASSWORD 'admin_pw';
CREATE ROLE wtow_service WITH LOGIN PASSWORD 'service_pw';
COMMENT ON ROLE wtow_service IS
'DEPRECATED: Replaced by wtow_read. Kept for migration compatibility';
CREATE ROLE wtow_read WITH LOGIN PASSWORD 'read_pw';
CREATE ROLE wtow_user_mngr WITH LOGIN PASSWORD 'user_mngr_pw';
CREATE ROLE wtow_account_mngr WITH LOGIN PASSWORD 'account_mngr_pw';
CREATE ROLE wtow_transfer_ngn WITH LOGIN PASSWORD 'transfer_ngn_pw';

ALTER DATABASE wallet_to_wallet_transfer OWNER TO wtow_admin;

CREATE SCHEMA core AUTHORIZATION wtow_admin;

-- DEPRECATED: Replaced by wtow_read. Kept for migration compatibility
GRANT USAGE ON SCHEMA core TO wtow_service;
GRANT USAGE ON SCHEMA core TO wtow_read;
GRANT USAGE ON SCHEMA core TO wtow_user_mngr;
GRANT USAGE ON SCHEMA core TO wtow_account_mngr;
GRANT USAGE ON SCHEMA core TO wtow_transfer_ngn;
