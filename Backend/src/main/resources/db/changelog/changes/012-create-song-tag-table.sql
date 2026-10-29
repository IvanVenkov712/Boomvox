--liquibase formatted sql

--changeset boomvox:012-create-song-tag-table
CREATE TABLE song_tag (
    song_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    added_at TIMESTAMP(6),
    version BIGINT NOT NULL,
    CONSTRAINT pk_song_tag PRIMARY KEY (song_id, tag_id),
    CONSTRAINT fk_song_tag_song FOREIGN KEY (song_id) REFERENCES song (id),
    CONSTRAINT fk_song_tag_tag FOREIGN KEY (tag_id) REFERENCES tag (id)
);

--rollback DROP TABLE IF EXISTS song_tag;
