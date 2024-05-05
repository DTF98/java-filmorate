package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage extends Storage<Film> {
    Film addLike(Integer filmID, Integer userID);

    Integer deleteLike(Integer filmID, Integer userID);

    List<Integer> getLikes(Integer filmID);

    List<Film> getMostPopularFilms(Integer count);

    List<Film> getSortedLikesListOfDirectorsFilms(Integer directorId);

    List<Film> getSortedYearListOfDirectorsFilms(Integer directorId);
}
