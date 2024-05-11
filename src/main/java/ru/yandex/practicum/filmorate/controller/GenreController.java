package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.Collection;

@RestController
@RequestMapping(path = "genres")
@Slf4j
@RequiredArgsConstructor
public class GenreController {
    private final GenreService genreService;

    @GetMapping
    public ResponseEntity<Collection<Genre>> get() {
        log.info("Получение всех жанров");
        return ResponseEntity.ok(genreService.get());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Genre> getByID(@PathVariable Integer id) {
        log.info("Получение жанра id={}", id);
        return ResponseEntity.ok(genreService.getByID(id));
    }
}
