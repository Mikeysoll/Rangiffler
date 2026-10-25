DELETE p FROM photo p
JOIN `user` u ON p.user_id = u.id
WHERE u.username = :username
