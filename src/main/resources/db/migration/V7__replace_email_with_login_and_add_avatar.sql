ALTER TABLE app_user
    RENAME COLUMN email TO login;

ALTER TABLE app_user
ALTER COLUMN login TYPE VARCHAR(100);

ALTER TABLE app_user
    ADD COLUMN avatar_url VARCHAR(2048);

ALTER INDEX uq_app_user_email_lower
    RENAME TO uq_app_user_login_lower;