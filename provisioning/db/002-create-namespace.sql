CREATE SCHEMA core AUTHORIZATION wtow_admin;

-- DEPRECATED: Replaced by wtow_read. Kept for migration compatibility
GRANT USAGE ON SCHEMA core TO wtow_service;

GRANT USAGE ON SCHEMA core TO wtow_read;
