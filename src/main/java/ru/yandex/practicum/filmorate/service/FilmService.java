package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.FilmStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

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
        boolean delFilm = filmStorage.delete(filmID);
        if (delFilm) {
            log.info("Удален фильм id = {}", filmID);
            return true;
        } else {
            throw new NotFoundException("Не найдено по ИД");
        }
    }

    public List<Film> search10MostPopularFilms(Integer count) {
        log.info("Получение списка популярных фильмов");
        return filmStorage.getMostPopularFilms(count);
    }

    public List<Film> getFilms() {
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

    public Film getFilmById(Integer id) {
        log.info("Получение фильма по id : {}", id);
        Optional<Film> film = filmStorage.getById(id);
        if (film.isPresent()) {
            log.info(String.format("Получен фильм по id = %s : %s", id, film));
            return film.get();
        }
        return null;
    }

    public List<Film> getSortedListOfDirectorsFilms(Integer id, String sortBy) {
        log.info("Получение списка фильмов фильмов режиссёра, отсортированных по количеству лайков/году");
        return filmStorage.getSortedListOfDirectorsFilms(id, sortBy);
    }
}

