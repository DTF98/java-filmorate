package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.FilmStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.xml.bind.ValidationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;

    public Film addLike(Integer userID, Integer filmID) {
        Film film = filmStorage.addLike(filmID, userID);
        log.info(String.format("Добавлен лайк фильму: id = %s пользователем id = %s",
                filmID, userID));
        return film;
    }

    public Integer deleteLike(Integer userID, Integer filmID) {
        log.info("Удаление лайка у фильма id = {}, пользователем id = {}", filmID, userID);
        return filmStorage.deleteLike(filmID, userID);
    }

    public boolean delete(Integer filmID) {
        log.info("Удаление фильма id = {}", filmID);
        if (!filmStorage.delete(filmID)) {
            throw new NotFoundException("Не найдено по ИД");
        }
        log.info("Удален фильм id = {}", filmID);
        return true;
    }

    public List<Film> search10MostPopularFilms(Integer count, Integer genreId, Integer year) throws ValidationException {
        if (genreId != null && year == null) {
            log.info("Получение списка популярных фильмов по жанру");
            return filmStorage.getMostPopularFilmsByGenreId(count, genreId);
        } else if (genreId == null && year != null) {
            log.info("Получение списка популярных фильмов по году");
            return filmStorage.getMostPopularFilmsByYear(count, year);
        } else if (genreId != null) {
            log.info("Получение списка популярных фильмов по жанру и году");
            return filmStorage.getMostPopularFilmsByGenreIdAndYear(count, year, genreId);
        } else if (count != null) {
            log.info("Получение списка популярных фильмов");
            return filmStorage.getMostPopularFilms(count);
        } else {
            throw new ValidationException("Переданы не валидные параметры запроса");
        }
    }

    public List<Film> getAll() {
        log.info("Получение списка всех фильмов");
        return new ArrayList<>(filmStorage.get());
    }

    public Film addFilm(Film film) {
        log.info("Добавление фильма: {}", film.getId());
        return filmStorage.add(film);
    }

    public Film updateFilm(Film film) {
        log.info("Обновление фильма: {}", film.getId());
        return filmStorage.update(film);
    }

    public Film getById(Integer id) {
        log.info("Получение фильма по id : {}", id);
        Optional<Film> film = filmStorage.getById(id);
        if (film.isPresent()) {
            log.info(String.format("Получен фильм по id = %s : %s", id, film));
            return film.get();
        }
        return null;
    }

    public List<Film> getSortedListOfDirectorsFilms(Integer id, String sortBy) {
        if (sortBy.equals("likes")) {
            log.info("Получение списка фильмов режиссёра, отсортированных по количеству лайков");
            return filmStorage.getSortedLikesListOfDirectorsFilms(id);
        } else if (sortBy.equals("year")) {
            log.info("Получение списка фильмов режиссёра, отсортированных по году");
            return filmStorage.getSortedYearListOfDirectorsFilms(id);
        }
        return null;
    }

    public List<Optional<Film>> getCommonFilms(Integer userId, Integer friendId) {
        return filmStorage.getCommonFilms(userId, friendId);
    }
}

