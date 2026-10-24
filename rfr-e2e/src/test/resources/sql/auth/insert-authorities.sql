INSERT INTO authority (user_id, authority)
VALUES (UUID_TO_BIN(:id), 'read'), (UUID_TO_BIN(:id), 'write')
