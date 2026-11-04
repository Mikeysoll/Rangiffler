SELECT COUNT(*) FROM friendship f
JOIN `user` u ON u.id = f.addressee_id
WHERE u.username = :username AND f.status = 'PENDING'
