SELECT DISTINCT related.username FROM `user` owner
JOIN friendship f ON owner.id = f.requester_id OR owner.id = f.addressee_id
JOIN `user` related ON related.id = f.requester_id OR related.id = f.addressee_id
WHERE owner.username = :username AND related.id <> owner.id
ORDER BY related.username
