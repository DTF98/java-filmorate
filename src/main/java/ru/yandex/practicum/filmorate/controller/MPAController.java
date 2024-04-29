package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.service.MPAService;

import static ru.yandex.practicum.filmorate.util.ResponseUtil.respondSuccess;
import static ru.yandex.practicum.filmorate.util.ResponseUtil.respondSuccessList;

@RestController
@RequestMapping(path = "mpa")
@Slf4j
@RequiredArgsConstructor
public class MPAController {
    private final MPAService mpaService;

    @GetMapping
    public ResponseEntity<?> getAllMpa() {
        log.info("Получение списка всех MPA");
        return respondSuccessList(mpaService.get());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMpa(@PathVariable Integer id) {
        log.info("Получение MPA по id={}", id);
        return respondSuccess(mpaService.getByID(id));
    }
}
