--liquibase formatted sql

--changeset boomvox:011-create-recommendation-table
CREATE TABLE recommendation (
    user_id BIGINT NOT NULL,
    song_id BIGINT NOT NULL,
    percent INTEGER NOT NULL,
    version BIGINT NOT NULL,
    CONSTRAINT pk_recommendation PRIMARY KEY (user_id, song_id),
    CONSTRAINT ck_recommendation_percent CHECK (percent >= 0 AND percent <= 100),
    CONSTRAINT fk_recommendation_user FOREIGN KEY (user_id) REFERENCES "user" (id),
    CONSTRAINT fk_recommendation_song FOREIGN KEY (song_id) REFERENCES song (id)
);

--rollback DROP TABLE IF EXISTS recommendation;
