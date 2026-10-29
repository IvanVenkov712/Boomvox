--liquibase formatted sql

--changeset boomvox:009-create-favourites-list-song-table
CREATE TABLE favourites_list_song (
    favourites_list_id BIGINT NOT NULL,
    song_id BIGINT NOT NULL,
    position INTEGER NOT NULL,
    added_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_favourites_list_song PRIMARY KEY (favourites_list_id, song_id),
    CONSTRAINT fk_favourites_list_song_favourites_list
        FOREIGN KEY (favourites_list_id) REFERENCES favourites_list (id),
    CONSTRAINT fk_favourites_list_song_song FOREIGN KEY (song_id) REFERENCES song (id)
);

--rollback DROP TABLE IF EXISTS favourites_list_song;
