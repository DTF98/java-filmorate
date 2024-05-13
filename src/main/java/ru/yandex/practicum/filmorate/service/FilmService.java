package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.FilmStorage;
import ru.yandex.practicum.filmorate.dao.GenreStorage;
import ru.yandex.practicum.filmorate.dao.MPAStorage;
import ru.yandex.practicum.filmorate.dao.UserStorage;
import ru.yandex.practicum.filmorate.exception.CreateEntityException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final GenreStorage genreStorage;
    private final MPAStorage mpaStorage;
    private final UserFeedService userFeedService;
    private final UserStorage userStorage;

    public Film addLike(Integer userID, Integer filmID) {
        Film film = getById(filmID);
        userStorage.getById(userID).orElseThrow(() ->
                new NotFoundException(String.format("Пользователь не найден id = %s", userID)));
        filmStorage.addLike(filmID, userID);
        log.info(String.format("Добавлен лайк фильму: id = %s пользователем id = %s",
                filmID, userID));
        userFeedService.addInHistoryFeed(userID, "LIKE", "ADD", filmID);
        log.info("Добавлено в историю добавление лайка пользователем id = {} фильму id = {}", userID, filmID);
        return film;
    }

    public Integer deleteLike(Integer userID, Integer filmID) {
        getById(filmID);
        userStorage.getById(userID).orElseThrow(() ->
                new NotFoundException(String.format("Пользователь не найден id = %s", userID)));
        filmStorage.deleteLike(filmID, userID);
        log.info("Удален лайк пользователя id = {} для фильма id = {}", userID, filmID);
        userFeedService.addInHistoryFeed(userID,"LIKE", "REMOVE", filmID);
        log.info("Добавлено в историю удаление лайка пользователя id = {} у фильма id = {}", userID, filmID);
        return filmID;
    }

    public Film delete(Integer filmID) {
        Film deleted = getById(filmID);
        filmStorage.delete(filmID);
        log.info("Удален фильм id = {}", filmID);
        return deleted;
    }

    public List<Film> search10MostPopularFilms(Integer count, Integer genreId, Integer year) {
        if (genreId != null && year == null && count > 0) {
            log.info("Получение списка популярных фильмов по жанру");
            return filmStorage.getMostPopularFilmsByGenreId(count, genreId);
        }
        if (genreId == null && year != null && count > 0) {
            log.info("Получение списка популярных фильмов по году");
            return filmStorage.getMostPopularFilmsByYear(count, year);
        }
        if (genreId != null && count > 0) {
            log.info("Получение списка популярных фильмов по жанру и году");
            return filmStorage.getMostPopularFilmsByGenreIdAndYear(count, year, genreId);
        }
        if (count > 0) {
            log.info("Получение списка популярных фильмов");
            return filmStorage.getMostPopularFilms(count);
        }
        throw new ValidationException("Переданы не валидные параметры запроса");
    }

    public List<Film> searchFilmsAndDirectors(String query, List<String> by) {
        log.info("Получение списка фильмов по названию фильма и/или режиссеру");
        return filmStorage.getSearchedFilms(query, by);
    }

    public List<Film> getAll() {
            log.info("Получение списка всех фильмов");
            return new ArrayList<>(filmStorage.get());
    }

    public Film addFilm(Film film) {
        if (film.getMpa() != null) {
            if (!mpaStorage.isExistMpaById(film.getMpa().getId())) {
                throw new ValidationException("Введены некорректные данные mpa");
            }
        }
        if (!film.getGenres().isEmpty()) {
            for (Genre g : film.getGenres()) {
                if (!genreStorage.isExistGenreById(g.getId())) {
                    throw new ValidationException("Введены некорректные данные жанров");
                }
            }
        }
        Film added = filmStorage.add(film);
        if (added.getId() == null) {
            throw new CreateEntityException(String.format("Фильм не добавлен! %s", film));
        }
        return added;
    }

    public Film updateFilm(Film film) {
        if (film.getMpa() != null) {
            if (!mpaStorage.isExistMpaById(film.getMpa().getId())) {
                throw new ValidationException("Введены некорректные данные mpa");
            }
        }
        if (!film.getGenres().isEmpty()) {
            for (Genre g: film.getGenres()) {
                if (!genreStorage.isExistGenreById(g.getId())) {
                    throw new ValidationException("Введены некорректные данные жанров");
                }
            }
        }
        getById(film.getId());
        Film updated = filmStorage.update(film);
        log.info("Обновлен фильм по id = {}", film.getId());
        return updated;
    }

    public Film getById(Integer id) {
        log.info("Получение фильма по id : {}", id);
        Film film = filmStorage.getById(id).orElseThrow(() ->
                new NotFoundException(String.format("Не найден фильм по id = %s", id)));
        log.info(String.format("Получен фильм по id = %s : %s", id, film));
        return film;
    }

    public List<Film> getSortedListOfDirectorsFilms(Integer id, String sortBy) {
        if (sortBy.equals("likes")) {
            log.info("Получение списка фильмов режиссёра, отсортированных по количеству лайков");
            List<Film> likes = filmStorage.getSortedLikesListOfDirectorsFilms(id);
            if (likes.isEmpty()) {
                throw new NotFoundException("Режисер или его фильмы не найдены");
            }
            log.info("Получен список фильмов режиссера id = {} отсортированных по лайкам", id);
            return likes;
        }
        if (sortBy.equals("year")) {
            log.info("Получение списка фильмов режиссёра, отсортированных по году");
            List<Film> years = filmStorage.getSortedYearListOfDirectorsFilms(id);
            if (years.isEmpty()) {
                throw new NotFoundException("Режисер или его фильмы не найдены");
            }
            log.info("Получен список фильмов режиссера id = {} отсортированных по году", id);
            return years;
        }
        else {
            throw new ValidationException("Переданы не верные параметры запроса");
        }
    }

    public List<Film> getCommonFilms(Integer userId, Integer friendId) {
        List<Integer> userFilmIds = filmStorage.getListOfUsersFilms(userId);
        List<Integer> friendsFilmIds = filmStorage.getListOfUsersFilms(friendId);

        List<Integer> commonFilmsIds = new ArrayList<>();
        for (Integer userFilmId : userFilmIds) {
            if (friendsFilmIds.contains(userFilmId)) {
                commonFilmsIds.add(userFilmId);
            }
        }
        List<Integer> sortedFilmsIds = getSortedFilmByPopularity(commonFilmsIds);

        List<Film> commonFilms = new ArrayList<>();
        for (Integer sortedFilmsId : sortedFilmsIds) {
            commonFilms.add(getById(sortedFilmsId));
        }
        if (commonFilms.isEmpty()) {
            return new ArrayList<>();
        } else {
            return commonFilms;
        }
    }

    private List<Integer> getSortedFilmByPopularity(List<Integer> filmIds) {
        Map<Integer, Integer> filmPopularityMap = filmStorage.getFilmIdByPopularity(filmIds);
        List<Integer> sortedFilmIds = new ArrayList<>();
        filmPopularityMap.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed());
        sortedFilmIds.addAll(filmPopularityMap.keySet());
        return sortedFilmIds;
    }
}

