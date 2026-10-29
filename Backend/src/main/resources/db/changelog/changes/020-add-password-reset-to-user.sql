-- liquibase formatted sql

-- changeset boomvox:020
ALTER TABLE "user"
    ADD COLUMN reset_token VARCHAR(255) NULL,
    ADD COLUMN reset_token_expiry TIMESTAMP(6) NULL;