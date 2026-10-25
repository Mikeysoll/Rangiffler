INSERT INTO `user` (id, username, country_id)
SELECT UUID_TO_BIN(:id), :username, id FROM country WHERE code = :countryCode
