INSERT INTO core.users (citizen_id, first_name, last_name, email, password_hash) VALUES
('00000000001', 'Bill', 'Turner', 'bill_turner@gmail.com',
'$2a$10$Qoxe5k1vseOCKUQqeB/8mO6oDv4s4s0ZsS.0ZgcQKciYwqdih6ojG' ),
('00000000002', 'Jack', 'Spiral', 'jack_spiral@gmail.com',
'$2a$10$eWV4ByEURtWUb0AQoygAIOeZBTpGVFVRxTRgIYnw9xE7Cn4b//ruS');

INSERT INTO core.accounts (user_id, balance, currency_id) VALUES
(
    (SELECT id FROM core.users WHERE email = 'bill_turner@gmail.com'), 100000.00,
    (SELECT id FROM core.currencies WHERE code = 'USD')
),
(
    (SELECT id FROM core.users WHERE email = 'jack_spiral@gmail.com'), 100000.00,
    (SELECT id FROM core.currencies WHERE code = 'USD')
);
