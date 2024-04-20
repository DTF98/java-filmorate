package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.impl.FilmStorage;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;

    public void addLike(Integer userID, Integer filmID) {
        filmStorage.setLike(filmID, userID);
        log.info(String.format("Добавлен лайк фильму: id = %s пользователем id = %s",
                filmID, userID));
    }

    public void removeLike(Integer userID, Integer filmID) {
        filmStorage.removeLike(filmID, userID);
        log.info(String.format("Удалён лайк пользователя id = %s у фильма id = %s", userID, filmID));
    }

    public List<Film> search10MostPopularFilms(Integer count) {
        log.info("Получен список популярных фильмов!");
        return filmStorage.getMostPopularFilms(count);
    }

    public List<Film> getFilms() {
        return new ArrayList<>(filmStorage.get());
    }

    public Film setFilm(Film film) {
        return filmStorage.set(film);
    }

    public Film updateFilm(Film film) {
        log.info("Обновление фильма: {}", filmStorage.getById(film.getId()));
        return filmStorage.update(film);
    }

    public Film getFilmById(Integer id) {
        Film film = filmStorage.getById(id);
        log.info(String.format("Получен фильм по id = %s : %s", id, film));
        return film;
    }
}

