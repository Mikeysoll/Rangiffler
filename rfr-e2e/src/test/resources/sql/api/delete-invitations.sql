DELETE FROM friendship
WHERE status = 'PENDING'
  AND (requester_id = UUID_TO_BIN(:userId) OR addressee_id = UUID_TO_BIN(:userId))
