package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.service.MPAService;

import java.util.Collection;

@RestController
@RequestMapping(path = "mpa")
@Slf4j
@RequiredArgsConstructor
public class MPAController {
    private final MPAService mpaService;

    @GetMapping
    public ResponseEntity<Collection<MPA>> getAllMpa() {
        log.info("Получение списка всех MPA");
        return ResponseEntity.ok(mpaService.get());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MPA> getMpa(@PathVariable Integer id) {
        log.info("Получение MPA по id={}", id);
        return ResponseEntity.ok(mpaService.getByID(id));
    }
}
