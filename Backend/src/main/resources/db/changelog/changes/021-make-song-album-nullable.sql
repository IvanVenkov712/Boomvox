--liquibase formatted sql

--changeset boomvox:021-make-song-album-nullable
ALTER TABLE boomvox.song ALTER COLUMN album_id DROP NOT NULL;

--rollback ALTER TABLE boomvox.song ALTER COLUMN album_id SET NOT NULL;