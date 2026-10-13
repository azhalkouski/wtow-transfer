ALTER TABLE core.accounts
ADD COLUMN acc_number VARCHAR(16) NOT NULL UNIQUE CHECK ( char_length(acc_number ) = 16 );
