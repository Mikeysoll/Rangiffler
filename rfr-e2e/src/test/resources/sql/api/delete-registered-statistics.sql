DELETE s FROM statistic s
JOIN `user` u ON s.user_id = u.id
WHERE u.username = :username
