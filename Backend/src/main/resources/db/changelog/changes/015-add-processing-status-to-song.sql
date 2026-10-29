--liquibase formatted sql
--changeset boomvox:015-add-processing-status-to-song

ALTER TABLE song ADD COLUMN processing_status VARCHAR(255) NOT NULL DEFAULT 'ACTIVE';

--rollback ALTER TABLE song DROP COLUMN processing_status;