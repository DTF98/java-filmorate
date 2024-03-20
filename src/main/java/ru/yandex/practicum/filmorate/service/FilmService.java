package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    private  final FilmStorage storage;
    private int id = 1;
    private static final Comparator<Film> SORT_BY_LIKES = Comparator.comparing(film -> film.getLikes().size(),
            Comparator.reverseOrder());

    @Autowired
    public FilmService(FilmStorage storage) {
        this.storage = storage;
    }

    public void addLike(Integer userID, Integer filmID) {
        if (storage.containsFilm(filmID)) {
            storage.getFilmById(filmID).setLike(userID);
            log.info(String.format("Добавлен лайк фильму: %s пользователем id = %s",
                    storage.getFilmById(filmID), userID));
        } else {
            throw new NotFoundException(String.format("Фильм по id = %s не найден!", filmID));
        }
    }

    public void removeLike(Integer userID, Integer filmID) {
        if (storage.containsFilm(filmID)) {
            if (storage.getFilmById(filmID).getLikes().contains(userID)) {
                log.info(String.format("Удалён лайк пользователя id = %s у фильма id = %s", userID, filmID));
                storage.getFilmById(filmID).removeLike(userID);
            }
        } else {
            throw new ValidationException("Не найден фильм!");
        }
    }

    public List<Film> search10MostPopularFilms(Integer count)
            throws ValidationException {
        if (count > 0) {
            log.info("Получен список популярных фильмов!");
            return storage.getFilms().stream()
                    .sorted(SORT_BY_LIKES)
                    .limit(count)
                    .collect(Collectors.toList());
        } else {
            throw new ValidationException("Передан параметр в count <= 0 ");
        }
    }

    public List<Film> getFilms() {
        return new ArrayList<>(storage.getFilms());
    }

    public void setFilm(Film film) {
        film.setId(id);
        id++;
        storage.setFilm(film);
        log.info(String.format("Добавлен фильм: {%s}", storage.getFilmById(film.getId())));
    }

    public void updateFilm(Film film) {
        if (storage.containsFilm(film.getId())) {
            storage.setFilm(film);
            log.info("Обновлен фильм: {}", storage.getFilmById(film.getId()));
        } else {
            throw new NotFoundException(String.format("Фильм не найден!"));
        }
    }

    public Film getFilmById(Integer id) {
        if (storage.containsFilm(id)) {
            log.info(String.format("Получен фильм по id = %s : %s", id, storage.getFilmById(id)));
            return storage.getFilmById(id);
        } else {
            throw new NotFoundException(String.format("Фильм по id = %s не найден!", id));
        }
    }
}

