DELETE pl FROM photo_like pl
JOIN photo p ON pl.photo_id = p.id
JOIN `like` l ON pl.like_id = l.id
JOIN `user` u ON u.id = p.user_id OR u.id = l.user_id
WHERE u.username = :username
