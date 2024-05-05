package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage extends Storage<Film> {
    Film addLike(Integer filmID, Integer userID);

    Integer deleteLike(Integer filmID, Integer userID);

    List<Integer> getLikes(Integer filmID);

    List<Film> getMostPopularFilms(Integer count);

    List<Film> getMostPopularFilmsByGenreIdAndYear(Integer count, Integer year, Integer genreId);

    List<Film> getMostPopularFilmsByYear(Integer count, Integer year);

    List<Film> getMostPopularFilmsByGenreId(Integer count, Integer genreId);

    List<Film> getSortedLikesListOfDirectorsFilms(Integer directorId);

    List<Film> getSortedYearListOfDirectorsFilms(Integer directorId);

    List<Optional<Film>> getCommonFilms(Integer userId, Integer friendId);
}
