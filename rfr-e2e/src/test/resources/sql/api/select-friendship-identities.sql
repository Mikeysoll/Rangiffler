SELECT DISTINCT HEX(f.id) FROM friendship f
JOIN `user` u ON u.id IN (f.requester_id, f.addressee_id)
WHERE u.username IN (:usernames) ORDER BY HEX(f.id)
