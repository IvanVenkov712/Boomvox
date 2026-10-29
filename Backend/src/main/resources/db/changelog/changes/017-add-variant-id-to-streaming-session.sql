--liquibase formatted sql
--changeset boomvox:017-add-variant-id-to-streaming-session

ALTER TABLE streaming_session
    ADD COLUMN variant_id BIGINT,
    ADD CONSTRAINT fk_streaming_session_variant
        FOREIGN KEY (variant_id) REFERENCES audio_variant (id);

--rollback ALTER TABLE streaming_session DROP CONSTRAINT fk_streaming_session_variant;
ALTER TABLE streaming_session DROP COLUMN variant_id;