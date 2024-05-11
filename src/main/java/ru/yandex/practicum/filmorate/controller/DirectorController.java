package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@RequestMapping(path = "directors")
@Slf4j
@RequiredArgsConstructor
public class DirectorController {
    private final DirectorService service;

    @PutMapping
    public ResponseEntity<Director> update(@Valid @RequestBody Director director) {
        log.info("Обновить режисера по id = {}", director.getId());
        return ResponseEntity.ok(service.update(director));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Director> getById(@PathVariable Integer id) {
        log.info("Получить режисера по id = {}", id);
        return ResponseEntity.ok(service.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteById(@PathVariable Integer id) {
        log.info("Получить режисера по id = {}", id);
        return ResponseEntity.ok(service.deleteById(id));
    }

    @PostMapping
    public ResponseEntity<Director> create(@Valid @RequestBody Director director) {
        log.info("Добавить режисера {}", director);
        return ResponseEntity.ok(service.add(director));
    }

    @GetMapping
    public ResponseEntity<Collection<Director>> get() {
        log.info("Получить список режисеров");
        return ResponseEntity.ok(service.get());
    }
}
