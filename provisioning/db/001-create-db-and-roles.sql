CREATE ROLE wtow_admin WITH LOGIN PASSWORD :'wtow_admin_pw';

CREATE ROLE wtow_service WITH LOGIN PASSWORD :'wtow_service_pw';
COMMENT ON ROLE wtow_service IS
'DEPRECATED: Replaced by wtow_read. Kept for migration compatibility';

CREATE ROLE wtow_read WITH LOGIN PASSWORD :'wtow_read_pw';

CREATE DATABASE wallet_to_wallet_transfer OWNER wtow_admin;
