package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage extends Storage<Film> {
    public Film addLike(Integer filmID, Integer userID);

    public Integer removeLike(Integer filmID, Integer userID);

    boolean removeFilm(long id);

    public List<Integer> getLikes(Integer filmID);

    public List<Film> getMostPopularFilms(Integer count);
}
