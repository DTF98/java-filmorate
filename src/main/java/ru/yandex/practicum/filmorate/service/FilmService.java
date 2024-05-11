package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.FilmStorage;
import ru.yandex.practicum.filmorate.dao.GenreStorage;
import ru.yandex.practicum.filmorate.dao.MPAStorage;
import ru.yandex.practicum.filmorate.dao.UserFeedStorage;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserFeedStorage userFeedStorage;
    private final GenreStorage genreStorage;
    private final MPAStorage mpaStorage;

    public Film addLike(Integer userID, Integer filmID) {
        try {
            Optional<Film> film = filmStorage.getById(filmID);
            if (film.isPresent() && filmStorage.addLike(filmID, userID)) {
                log.info(String.format("Добавлен лайк фильму: id = %s пользователем id = %s",
                        filmID, userID));
                if (userFeedStorage.addInHistory(userID, "LIKE", "ADD", filmID)) {
                    log.info("Добавлено в историю добавление лайка пользователем id = {} фильму id = {}", userID, filmID);
                    return film.get();
                } else {
                    throw new AddFeedException(String.format("Ошибка добваления в историю лайка пользователя по id = %s," +
                            " фильму по id = %s", userID, filmID));
                }
            } else {
                throw new NotFoundException(String.format("Фильм по id = %s или пользователь по id = %s не найдены!",
                        filmID, userID));
            }
        } catch (DataAccessException e) {
            log.error("Ошибка при добавления лайка", e);
        }
        throw new ApplicationException(String.format("Ошибка при добавлении лайка фильму id = %s, " +
                "пользователем id = %s", filmID, userID));
    }

    public Integer deleteLike(Integer userID, Integer filmID) {
        try {
            if (filmStorage.deleteLike(filmID, userID)) {
                log.info("Удален лайк пользователя id = {} для фильма id = {}", userID, filmID);
                if (userFeedStorage.addInHistory(userID,"LIKE", "REMOVE", filmID)) {
                    log.info("Добавлено в историю удаление лайка пользователя id = {} у фильма id = {}", userID, filmID);
                    return filmID;
                } else {
                    throw new AddFeedException(String.format("Ошибка добваления в историю удаления лайка пользователя" +
                            " по id = %s, фильму по id = %s", userID, filmID));
                }
            } else {
                throw new NotFoundException(String.format("Фильм по id = %s или пользователь по id = %s не найдены!",
                        filmID, userID));
            }
        } catch (DataAccessException e) {
            log.error("Ошибка при удалени лайка", e);
        }
        throw new ApplicationException(String.format("Ошибка при удалении лайка фильму id = %s, " +
                "пользователем id = %s", filmID, userID));
    }

    public boolean delete(Integer filmID) {
        try {
            if (!filmStorage.delete(filmID)) {
                throw new NotFoundException(String.format("Фильм по id = %s не найден!", filmID));
            }
            log.info("Удален фильм id = {}", filmID);
            return true;
        } catch (DataAccessException e) {
            log.error("Ошибка при удалени фильма", e);
        }
        throw new ApplicationException(String.format("Ошибка при удалении фильма по id = %s", filmID));
    }

    public List<Film> search10MostPopularFilms(Integer count, Integer genreId, Integer year) {
        try {
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
        } catch (DataAccessException e) {
            log.error("Ошибка при получении списка популярных фильмов", e);
        }
        throw new ApplicationException("Ошибка при получении списка популярных фильмов");
    }

    public List<Film> searchFilmsAndDirectors(String query, List<String> by) {
        try {
            log.info("Получение списка фильмов по названию фильма и/или режиссеру");
            return filmStorage.getSearchedFilms(query, by);
        } catch (DataAccessException e) {
            log.error("Ошибка при получении списка фильмов по названию фильма и/или режиссеру", e);
        }
        throw new ApplicationException("Ошибка при получении списка фильмов по названию фильма и/или режиссеру");
    }

    public List<Film> getAll() {
        try {
            log.info("Получение списка всех фильмов");
            return new ArrayList<>(filmStorage.get());
        } catch (DataAccessException e) {
            log.error("Ошибка при получении списка всех фильмов", e);
        }
        throw new ApplicationException("Ошибка при получении списка всех фильмов");
    }

    public Film addFilm(Film film) {
        try {
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
            if (added.getId() == 0) {
                throw new CreateEntityException(String.format("Фильм не добавлен! %s", film));
            }
            return added;
        } catch (DataAccessException e) {
            log.error("Ошибка при добавлении фильма {}", film, e);
        }
        throw new ApplicationException(String.format("Ошибка при добавлении фильма %s", film));
    }

    public Film updateFilm(Film film) {
        try {
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
            Film updated = filmStorage.update(film);
            if (updated.getId() == 0) {
                throw new NotFoundException(String.format("Фильм по id = %s не найден!", film.getId()));
            }
            return updated;
        } catch (DataAccessException e) {
            log.error("Ошибка при обновлении фильма {}", film, e);
        }
        throw new ApplicationException(String.format("Ошибка при обновлении фильма %s", film));
    }

    public Film getById(Integer id) {
        try {
            log.info("Получение фильма по id : {}", id);
            Optional<Film> film = filmStorage.getById(id);
            if (film.isPresent()) {
                log.info(String.format("Получен фильм по id = %s : %s", id, film));
                return film.get();
            }
        } catch (DataAccessException e) {
            log.error("Ошибка при получении фильма по id = {}", id, e);
            throw new NotFoundException(String.format("Фильм по id = %s не найден!", id));
        }
        throw new ApplicationException(String.format("Ошибка при получении фильма по id = %s", id));
    }

    public List<Film> getSortedListOfDirectorsFilms(Integer id, String sortBy) {
        try {
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
        } catch (DataAccessException e) {
            log.error("Ошибка при получении списка фильмов режисера");
        }
        throw new ApplicationException("Ошибка при получении списка фильмов режисера");
    }

    public List<Optional<Film>> getCommonFilms(Integer userId, Integer friendId) {
        try {
            return filmStorage.getCommonFilms(userId, friendId);
        } catch (DataAccessException e) {
            log.error("Ошибка при получении списка общих фильмов");
        }
        throw new ApplicationException("Ошибка при получении списка общих фильмов");
    }
}

