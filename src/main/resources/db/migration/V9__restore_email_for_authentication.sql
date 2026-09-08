ALTER TABLE app_user
    RENAME COLUMN login TO email;

ALTER TABLE app_user
    ALTER COLUMN email TYPE VARCHAR(320);

ALTER INDEX uq_app_user_login_lower
    RENAME TO uq_app_user_email_lower;
