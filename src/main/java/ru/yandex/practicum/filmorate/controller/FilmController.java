package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping(path = "films")
@Slf4j
@RequiredArgsConstructor
public class FilmController {
    private final FilmService service;

    @GetMapping
    public ResponseEntity<Collection<Film>> get() {
        log.info("Получить список всех фильмов");
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/popular")
    public ResponseEntity<Collection<Film>> getTopFilms(@RequestParam(defaultValue = "10") Integer count, Integer genreId, Integer year) {
        log.info("Получить список популярных фильмов в зависимости от указаной сортировки");
        return ResponseEntity.ok(service.search10MostPopularFilms(count, genreId, year));
    }

    @GetMapping("/search")
    public ResponseEntity<Collection<Film>> search(@RequestParam(name = "query") String query, @RequestParam(name = "by") List<String> by) {
        log.info("Выполнитиь поиск по фильмам");
        return ResponseEntity.ok(service.searchFilmsAndDirectors(query, by));
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Integer> deleteLike(@PathVariable("id") Integer id, @PathVariable("userId") Integer userId) {
        log.info("Удалить лайк фильму по id = {}, пользователем по id = {}", id, userId);
        return ResponseEntity.ok(service.deleteLike(userId, id));
    }

    @DeleteMapping("/{filmId}")
    public ResponseEntity<Boolean> deleteById(@PathVariable("filmId") Integer id) {
        log.info("Удалить фильм по id = {}", id);
        return ResponseEntity.ok(service.delete(id));
    }

    @PostMapping
    public ResponseEntity<Film> create(@Valid @RequestBody Film film) {
        log.info("Добавить фильм {}", film);
        return ResponseEntity.ok(service.addFilm(film));
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Film> addLike(@PathVariable("id") Integer id, @PathVariable("userId") Integer userId) {
        log.info("Добавить лайк фильму по id = {}, пользователем по id = {}", id, userId);
        return ResponseEntity.ok(service.addLike(userId, id));
    }

    @PutMapping
    public ResponseEntity<Film> update(@Valid @RequestBody Film film) {
        log.info("Добавить фильм по id = {}", film.getId());
        return ResponseEntity.ok(service.updateFilm(film));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Film> getById(@PathVariable Integer id) {
        log.info("Получить фильм по id = {}", id);
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/director/{directorId}")
    public ResponseEntity<Collection<Film>> getFilmsOfDirector(@PathVariable Integer directorId, @RequestParam String sortBy) {
        log.info("Получить список фильмов режисера по id = {}", directorId);
        return ResponseEntity.ok(service.getSortedListOfDirectorsFilms(directorId, sortBy));
    }

    @GetMapping("/common")
    public ResponseEntity<Collection<Film>> getCommonFilms(@RequestParam("userId") Integer userId,
                                                               @RequestParam("friendId") Integer friendId) {
        log.info("Получить список общих фильмов пользователей по id = {} и id = {}", userId, friendId);
        return ResponseEntity.ok(service.getCommonFilms(userId, friendId));
    }
}

