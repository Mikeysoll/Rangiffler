DELETE a FROM authority a
JOIN `user` u ON a.user_id = u.id
WHERE u.username = :username
