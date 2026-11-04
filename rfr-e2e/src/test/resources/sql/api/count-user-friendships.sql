SELECT COUNT(*) FROM friendship f
JOIN `user` u ON u.id = f.requester_id OR u.id = f.addressee_id
WHERE u.username = :username
