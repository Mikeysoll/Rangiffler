DELETE l FROM `like` l
JOIN `user` u ON l.user_id = u.id
WHERE u.username = :username
