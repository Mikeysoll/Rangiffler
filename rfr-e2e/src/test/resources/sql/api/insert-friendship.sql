INSERT INTO friendship (id, requester_id, addressee_id, status, created_at, responded_at, version)
VALUES (UUID_TO_BIN(:id), UUID_TO_BIN(:requesterId), UUID_TO_BIN(:addresseeId), :status,
        CURRENT_TIMESTAMP(6), CASE WHEN :status = 'ACCEPTED' THEN CURRENT_TIMESTAMP(6) ELSE NULL END, 0)
