DROP TABLE IF EXISTS film_likes;
DROP TABLE IF EXISTS film_genres;
DROP TABLE IF EXISTS genres;
DROP TABLE IF EXISTS film_mpa;
DROP TABLE IF EXISTS mpa;
DROP TABLE IF EXISTS user_friends;
DROP TABLE IF EXISTS review_like;
DROP TABLE IF EXISTS review;
DROP TABLE IF EXISTS film_directors;
DROP TABLE IF EXISTS films;
DROP TABLE IF EXISTS directors;
DROP TABLE IF EXISTS users;

ALTER SEQUENCE IF EXISTS REVIEW_ID_SEQ RESTART WITH 1;

CREATE TABLE IF NOT EXISTS USERS
(
    id       INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    email    varchar(64),
    login    varchar(64),
    name     varchar(64),
    birthday date
);

CREATE TABLE IF NOT EXISTS USER_FRIENDS
(
    user_id   INTEGER REFERENCES USERS (ID),
    friend_id INTEGER REFERENCES USERS (ID),
    status    varchar(64) DEFAULT false
);

CREATE TABLE IF NOT EXISTS DIRECTORS
(
    director_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    director_name VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS FILMS
(
    id           INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name         varchar(64),
    description  varchar(1024),
    release_date date,
    duration     INTEGER
);

CREATE TABLE IF NOT EXISTS GENRES
(
    genre_id INTEGER PRIMARY KEY,
    genre    varchar(64)
);

CREATE TABLE IF NOT EXISTS FILM_GENRES
(
    film_id  INTEGER REFERENCES FILMS (ID) ON DELETE CASCADE,
    genre_id INTEGER REFERENCES GENRES (GENRE_ID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS MPA
(
    mpa_id INTEGER PRIMARY KEY,
    mpa    varchar(64)
);

CREATE TABLE IF NOT EXISTS FILM_MPA
(
    film_id INTEGER REFERENCES FILMS (ID) ON DELETE CASCADE,
    mpa_id  INTEGER REFERENCES MPA (MPA_ID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS FILM_LIKES
(
    user_id INTEGER REFERENCES USERS (ID) ON DELETE CASCADE,
    film_id INTEGER REFERENCES FILMS (ID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS FILM_DIRECTORS (
    film_id INTEGER REFERENCES FILMS(ID) ON DELETE CASCADE,
    director_id INTEGER REFERENCES DIRECTORS(DIRECTOR_ID) ON DELETE CASCADE
);

CREATE SEQUENCE IF NOT EXISTS REVIEW_ID_SEQ START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS REVIEW
(
    id          INTEGER DEFAULT NEXTVAL('REVIEW_ID_SEQ') PRIMARY KEY,
    film_id     INTEGER REFERENCES FILMS (id) ON DELETE CASCADE,
    user_id     INTEGER REFERENCES USERS (id) ON DELETE CASCADE,
    content     TEXT NOT NULL,
    useful      INTEGER,
    is_positive BOOLEAN
);

CREATE TABLE IF NOT EXISTS REVIEW_LIKE
(
    id        INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    review_id INTEGER REFERENCES REVIEW (id) ON DELETE CASCADE,
    user_id   INTEGER REFERENCES USERS (id) ON DELETE CASCADE,
    like_type BOOLEAN, -- true is like, false is dislike
    UNIQUE (review_id, user_id)
);
