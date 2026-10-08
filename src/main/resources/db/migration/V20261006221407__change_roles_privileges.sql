-- wtow_service out of business
REVOKE ALL ON ALL TABLES IN SCHEMA core FROM wtow_service;


-- Reference data: public, readable by all roles
GRANT SELECT ON TABLE core.currencies TO wtow_read;
GRANT SELECT ON TABLE core.currencies TO wtow_user_mngr;
GRANT SELECT ON TABLE core.currencies TO wtow_account_mngr;


-- User management service
GRANT SELECT, INSERT, UPDATE ON TABLE core.users TO wtow_user_mngr;


-- Account management service
GRANT SELECT, INSERT ON TABLE core.accounts TO wtow_account_mngr;

