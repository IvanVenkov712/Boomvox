--liquibase formatted sql

--changeset boomvox:019-restore-streaming-session-variant-id
ALTER TABLE streaming_session
    ADD COLUMN variant_id BIGINT,
    ADD CONSTRAINT fk_streaming_session_variant
        FOREIGN KEY (variant_id) REFERENCES audio_variant (id);

--rollback ALTER TABLE streaming_session DROP COLUMN variant_id;
