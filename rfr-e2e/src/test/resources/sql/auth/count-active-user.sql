SELECT COUNT(*) FROM `user`
WHERE id = UUID_TO_BIN(:id)
  AND enabled = true
  AND account_non_expired = true
  AND account_non_locked = true
  AND credentials_non_expired = true
