CREATE TABLE core.users (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    citizen_id TEXT NOT NULL UNIQUE,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    email TEXT NOT NULL UNIQUE,
    password_hash CHAR(60) NOT NULL,

    CONSTRAINT first_name_max_length  CHECK ( char_length(first_name) < 20 ),
    CONSTRAINT last_name_max_length  CHECK ( char_length(last_name) < 20 ),
    CONSTRAINT citizen_id_format CHECK ( citizen_id ~ '^[0-9]{11}$' ),
    CONSTRAINT email_valid CHECK (email ~ '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$')
);

-- exclude DELETE until retention mechanism implemented
GRANT SELECT, INSERT, UPDATE
ON TABLE core.users
TO wtow_service;


CREATE TABLE core.currencies (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    code TEXT NOT NULL UNIQUE CHECK ( CHAR_LENGTH(code) = 3 ),
    description VARCHAR(20)
);

INSERT INTO core.currencies (code, description) VALUES ('USD', 'US dollar'), ('EUR', 'EU primary');

GRANT SELECT ON TABLE core.currencies TO wtow_service;


CREATE TABLE core.accounts (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    user_id UUID NOT NULL REFERENCES core.users(id),
    balance NUMERIC(10, 2) NOT NULL,
    currency_id BIGINT NOT NULL REFERENCES core.currencies(id)
);

-- exclude DELETE until retention mechanism implemented
GRANT SELECT, INSERT, UPDATE
ON TABLE core.accounts
TO wtow_service;
