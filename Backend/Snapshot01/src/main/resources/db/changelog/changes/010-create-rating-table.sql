--liquibase formatted sql

--changeset boomvox:010-create-rating-table
CREATE TABLE rating (
    user_id BIGINT NOT NULL,
    song_id BIGINT NOT NULL,
    rating_grade INTEGER NOT NULL,
    comment TEXT NOT NULL,
    last_updated_at TIMESTAMP(6) NOT NULL,
    version BIGINT NOT NULL,
    CONSTRAINT pk_rating PRIMARY KEY (user_id, song_id),
    CONSTRAINT ck_rating_grade CHECK (rating_grade >= 0 AND rating_grade <= 10),
    CONSTRAINT fk_rating_user FOREIGN KEY (user_id) REFERENCES "user" (id),
    CONSTRAINT fk_rating_song FOREIGN KEY (song_id) REFERENCES song (id)
);

--rollback DROP TABLE IF EXISTS rating;
