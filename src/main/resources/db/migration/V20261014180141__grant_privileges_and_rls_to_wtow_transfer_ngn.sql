GRANT SELECT ON TABLE core.currencies TO wtow_transfer_ngn;

GRANT SELECT, UPDATE
ON TABLE core.accounts
TO wtow_transfer_ngn;

CREATE POLICY transfer_ngn_can_read_all ON core.accounts
    FOR SELECT
    TO wtow_transfer_ngn
    USING (true);


CREATE POLICY transfer_ngn_can_update_all ON core.accounts
    FOR UPDATE
    TO wtow_transfer_ngn
    USING (true)
    WITH CHECK (true);
