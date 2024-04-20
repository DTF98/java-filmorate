package ru.yandex.practicum.filmorate.dao.impl;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage extends Storage<Film>{

    public void setLike (Integer filmID, Integer userID);

    public void removeLike (Integer filmID, Integer userID);

    public List<Integer> getLikes(Integer filmID);

    public List<Film> getMostPopularFilms(Integer count);
}
