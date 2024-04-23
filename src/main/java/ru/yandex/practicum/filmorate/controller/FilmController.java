package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.List;

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
    public List<Film> findAll() {
        return service.getFilms();
    }

    @GetMapping("/popular")
    public ResponseEntity<?> getTopFilms(@RequestParam(defaultValue = "10", required = false) Integer count) {
        return new ResponseEntity<>(service.search10MostPopularFilms(count), HttpStatus.OK);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public HttpStatus deleteLike(@PathVariable("id") Integer id, @PathVariable("userId") Integer userId) {
        service.removeLike(userId, id);
        return HttpStatus.OK;
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody Film film) {
        return new ResponseEntity<>(service.setFilm(film), HttpStatus.OK);
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<?> addLike(@PathVariable("id") Integer id, @PathVariable("userId") Integer userId) {
        service.addLike(userId, id);
        return new ResponseEntity<>(service.getFilmById(id), HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody Film film) {
        return new ResponseEntity<>(service.updateFilm(film), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getFilm(@PathVariable Integer id) {
        return new ResponseEntity<>(service.getFilmById(id), HttpStatus.OK);
    }
}

