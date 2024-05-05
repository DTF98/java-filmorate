package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import javax.validation.Valid;

import static ru.yandex.practicum.filmorate.util.ResponseUtil.respondSuccess;
import static ru.yandex.practicum.filmorate.util.ResponseUtil.respondSuccessList;

@RestController
@RequestMapping(path = "directors")
@Slf4j
public class DirectorController {
    private final DirectorService service;

    @Autowired
    public DirectorController(DirectorService service) {
        this.service = service;
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody Director director) {
        return respondSuccess(service.update(director));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        return respondSuccess(service.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Integer id) {
        return respondSuccess(service.deleteById(id));
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody Director director) {
        return respondSuccess(service.add(director));
    }

    @GetMapping
    public ResponseEntity<?> get() {
        return respondSuccessList(service.get());
    }
}
