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
        return respondSuccess(service.updateDir(director));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDir(@PathVariable Integer id) {
        return respondSuccess(service.getDirById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDir(@PathVariable Integer id) {
        return respondSuccess(service.deleteDirById(id));
    }

    @PostMapping
    public ResponseEntity<?> createDir(@Valid @RequestBody Director director) {
        return respondSuccess(service.addDir(director));
    }

    @GetMapping
    public ResponseEntity<?> get() {
        return respondSuccessList(service.get());
    }
}
