package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.List;

@RestController
@Slf4j
public class FilmController {
    private int id = 1;
    private final LocalDate movieBirthday = LocalDate.of(1895,12,28);
    private final int maximumDescriptionLength = 200;
    private final FilmStorage storage = new FilmStorage();

    @GetMapping("/films")
    public List<Film> findAll() {
        return storage.getFilms();
    }

    @PostMapping(value = "/films")
    public ResponseEntity<?> create(@RequestBody Film film) {
        try {
            Film filmCheck = chekingFilm(film);
            storage.setFilm(filmCheck);
            log.info("Добавлен фильм: {}", storage.getFilmById(filmCheck.getId()));
            return new ResponseEntity<>(filmCheck, HttpStatus.OK);
        } catch (ValidationException e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(film, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping(value = "/films")
    public ResponseEntity<?> updateOrCreateNew(@RequestBody Film film) {
        if (film.getId() != null) {
            try {
                Film filmCheck = chekingFilm(film);
                if (storage.containsFilm(filmCheck.getId())) {
                    storage.setFilm(filmCheck);
                    log.info("Обновлен фильм: {}", storage.getFilmById(filmCheck.getId()));
                    return new ResponseEntity<>(filmCheck, HttpStatus.OK);
                } else {
                    log.info("Film по такому ID не существует");
                    return new ResponseEntity<>(film, HttpStatus.NOT_FOUND);
                }
            } catch (ValidationException e) {
                log.error(e.getMessage());
                return new ResponseEntity<>(film, HttpStatus.BAD_REQUEST);
            }
        } else {
            return create(film);
        }
    }

    private Film chekingFilm(Film film) {
        if (!film.getName().isEmpty()) {
            if (!(film.getDescription().length() > maximumDescriptionLength)) {
                if (!(film.getReleaseDate().isBefore(movieBirthday))) {
                    if (!(film.getDuration() < 0)) {
                        if (film.getId() == null) {
                            film.setId(id);
                            id++;
                        }
                        return film;
                    } else {
                        throw new ValidationException("продолжительность фильма должна быть положительной: " +
                                film.getDuration());
                    }
                } else {
                    throw new ValidationException("дата релиза — не раньше 28 декабря 1895 года: " +
                            film.getReleaseDate());
                }
            } else {
                throw new ValidationException("максимальная длина описания — 200 символов: " +
                        film.getDescription().length());
            }
        } else {
            throw new ValidationException("название не может быть пустым: " + film.getName());
        }
    }
}

