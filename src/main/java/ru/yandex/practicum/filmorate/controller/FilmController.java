package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.List;

@RestController
@Slf4j
public class FilmController {
    private int id = 1;
    private final LocalDate movieBirthday = LocalDate.of(1895,12,28);
    private final int maximumDescriptionLength = 200;
    private final FilmService service;

    @Autowired
    public FilmController(FilmStorage storage) {
        this.service = new FilmService(storage);
    }

    @GetMapping("/films")
    public List<Film> findAll() {
        return service.getFilms();
    }

    @GetMapping("/films/popular")
    public ResponseEntity<?> getTopFilms(@RequestParam(defaultValue = "10", required = false) Integer count) {
        return new ResponseEntity<>(service.search10MostPopularFilms(count), HttpStatus.OK);
    }

    @DeleteMapping("/films/{id}/like/{userId}")
    public HttpStatus deleteLike(@PathVariable("id") Integer id, @PathVariable("userId") Integer userId) {
        service.removeLike(userId, id);
        return HttpStatus.OK;
    }

    @PostMapping(value = "/films")
    public ResponseEntity<?> create(@RequestBody Film film) {
        Film filmCheck = chekingFilm(film);
        service.setFilm(filmCheck);
        return new ResponseEntity<>(filmCheck, HttpStatus.OK);
    }

    @PutMapping("/films/{id}/like/{userId}")
    public ResponseEntity<?> addLike(@PathVariable("id") Integer id, @PathVariable("userId") Integer userId) {
        service.addLike(userId, id);
        return new ResponseEntity<>(service.getFilmById(id), HttpStatus.OK);
    }

    @PutMapping(value = "/films")
    public ResponseEntity<?> updateOrCreateNew(@RequestBody Film film) {
        if (film.getId() != null) {
            Film filmCheck = chekingFilm(film);
            service.updateFilm(filmCheck);
            return new ResponseEntity<>(filmCheck, HttpStatus.OK);
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

