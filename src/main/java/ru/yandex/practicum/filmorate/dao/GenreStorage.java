package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Genre;

public interface GenreStorage extends Storage<Genre> {

    boolean isExistGenreById(Integer id);
}
