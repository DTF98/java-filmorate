package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

import static ru.yandex.practicum.filmorate.util.ResponseUtil.respondSuccess;
import static ru.yandex.practicum.filmorate.util.ResponseUtil.respondSuccessList;

@RestController
@RequestMapping(path = "films")
@Slf4j
public class FilmController {
    private final FilmService service;

    @Autowired
    public FilmController(FilmService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Collection<Film>> get() {
        return respondSuccessList(service.getFilms());
    }

    @GetMapping("/popular")
    public ResponseEntity<?> getTopFilms(@RequestParam(defaultValue = "10", required = false) Integer count) {
        return respondSuccessList(service.search10MostPopularFilms(count));
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam(name = "query") String query, @RequestParam(name = "by") List<String> by) {
        return respondSuccessList(service.searchFilmsAndDirectors(query, by));
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<?> deleteLike(@PathVariable("id") Integer id, @PathVariable("userId") Integer userId) {
        return respondSuccess(service.removeLike(userId, id));
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody Film film) {
        return respondSuccess(service.addFilm(film));
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<?> addLike(@PathVariable("id") Integer id, @PathVariable("userId") Integer userId) {
        return respondSuccess(service.addLike(userId, id));
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody Film film) {
        return respondSuccess(service.updateFilm(film));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getFilm(@PathVariable Integer id) {
        return respondSuccess(service.getFilmById(id));
    }
}

