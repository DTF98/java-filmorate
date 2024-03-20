package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import javax.validation.Valid;
import java.util.List;

@RestController
@Slf4j
public class FilmController {
    //TODO RequestID в логи
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
    public ResponseEntity<?> create(@Valid @RequestBody Film film) {
        service.setFilm(film);
        return new ResponseEntity<>(film, HttpStatus.OK);
    }

    @PutMapping("/films/{id}/like/{userId}")
    public ResponseEntity<?> addLike(@PathVariable("id") Integer id, @PathVariable("userId") Integer userId) {
        service.addLike(userId, id);
        return new ResponseEntity<>(service.getFilmById(id), HttpStatus.OK);
    }

    @PutMapping(value = "/films")
    public ResponseEntity<?> updateOrCreateNew(@Valid @RequestBody Film film) {
        if (film.getId() != null) {
            service.updateFilm(film);
            return new ResponseEntity<>(film, HttpStatus.OK);
        } else {
            return create(film);
        }
    }
}

