CREATE ROLE wtow_admin WITH LOGIN PASSWORD :'admin_pw';
CREATE ROLE wtow_service WITH LOGIN PASSWORD :'service_pw';

CREATE DATABASE wallet_to_wallet_transfer OWNER wtow_admin;
