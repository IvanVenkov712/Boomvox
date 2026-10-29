--liquibase formatted sql

--changeset boomvox:008-create-playlist-song-table
CREATE TABLE playlist_song (
    playlist_id BIGINT NOT NULL,
    song_id BIGINT NOT NULL,
    position INTEGER NOT NULL,
    added_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_playlist_song PRIMARY KEY (playlist_id, song_id),
    CONSTRAINT fk_playlist_song_playlist FOREIGN KEY (playlist_id) REFERENCES playlist (id),
    CONSTRAINT fk_playlist_song_song FOREIGN KEY (song_id) REFERENCES song (id)
);

--rollback DROP TABLE IF EXISTS playlist_song;
