package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@Slf4j
public class FilmController {
    private final HashMap<Integer,Film> films = new HashMap<>();
    private int id = 1;

    @GetMapping("/films")
    public List<Film> findAll() {
        List<Film> listOfFilms = new ArrayList<>();
        films.forEach((k,v) -> listOfFilms.add(v));
        return listOfFilms;
    }

    @PostMapping(value = "/films")
    public Film create(@RequestBody Film film) {
        Film filmCheck = chekingFilm(film);
        if (filmCheck.getId() != null) {
            films.put(filmCheck.getId(),filmCheck);
            log.info("Добавлен фильм: {}", films.get(filmCheck.getId()));
            return filmCheck;
        } else {
            return film;
        }
    }

    @PutMapping(value = "/films")
    public Film updateOrCreateNew(@RequestBody Film film) {
        if (film.getId() != null) {
            if (films.containsKey(film.getId())) {
                films.put(film.getId(),film);
                log.info("Обновлен фильм: {}", films.get(film.getId()));
                return film;
            } else {
                log.info("Film по такому ID не существует");
                throw new ValidationException("Film по такому ID не существует");
            }
        } else {
            return create(film);
        }
    }

    private Film chekingFilm(Film film) {
        if (!film.getName().isEmpty()) {
            if (!(film.getDescription().length() > 200)) {
                if (!(film.getReleaseDate().isBefore(LocalDate.of(1895,12,28)))) {
                    if (!(film.getDuration() < 0)) {
                        film.setId(id);
                        id++;
                        return film;
                    } else {
                        log.info("продолжительность фильма должна быть положительной: {}", film.getDuration());
                        throw new ValidationException("продолжительность фильма должна быть положительной");
                    }
                } else {
                    log.info("дата релиза — не раньше 28 декабря 1895 года: {}", film.getReleaseDate());
                    throw new ValidationException("дата релиза — не раньше 28 декабря 1895 года");
                }
            } else {
                log.info("максимальная длина описания — 200 символов: {}", film.getDescription().length());
                throw new ValidationException("максимальная длина описания — 200 символов");
            }
        } else {
            log.info("название не может быть пустым: {}", film.getName());
            throw new ValidationException("название не может быть пустым");
        }
    }
}

