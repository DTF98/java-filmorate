package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Map;

public interface FilmStorage extends Storage<Film> {
    void addLike(Integer filmID, Integer userID);

    void deleteLike(Integer filmID, Integer userID);

    List<Film> getMostPopularFilms(Integer count);

    List<Film> getSearchedFilms(String query, List<String> by);

    List<Film> getMostPopularFilmsByGenreIdAndYear(Integer count, Integer year, Integer genreId);

    List<Film> getMostPopularFilmsByYear(Integer count, Integer year);

    List<Film> getMostPopularFilmsByGenreId(Integer count, Integer genreId);

    List<Film> getSortedLikesListOfDirectorsFilms(Integer directorId);

    List<Film> getSortedYearListOfDirectorsFilms(Integer directorId);

    List<Integer> getListOfUsersFilms(Integer id);

    Map<Integer, Integer> getFilmIdByPopularity(List<Integer> filmIds);
}
