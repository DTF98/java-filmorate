package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;

@RestController
@RequestMapping(path ="users")
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final UserService service;

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(service.get());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable int id) {
        log.info("Получить пользователя по id = {}", id);
        return ResponseEntity.ok(service.getUserById(id));
    }

    @GetMapping("/{id}/friends")
    public ResponseEntity<?> getFriends(@PathVariable("id") Integer id) {
        log.info("Получить список друзей пользователя по id = {}", id);
        return ResponseEntity.ok(service.getFriends(id));
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public ResponseEntity<?> getListOfMutualFriends(@PathVariable("id") Integer id,
                                                    @PathVariable("otherId") Integer otherId) {
        log.info("Получить список общих друзей пользователя по id = {}, с пользователем по id = {}", id, otherId);
        return ResponseEntity.ok(service.searchForCommonFriends(id, otherId));
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public ResponseEntity<?> deleteFriend(@PathVariable("id") Integer id,
                                          @PathVariable("friendId") Integer friendId) {
        log.info("Удалить из друзей пользователя по id = {}, пользователя по id = {}", id, friendId);
        return ResponseEntity.ok(service.deleteFriend(id, friendId));
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody User user) {
        log.info("Создать нового пользователя {}", user);
        return ResponseEntity.ok(service.add(user));
    }

    @PutMapping
    public ResponseEntity<?> update(@Valid @RequestBody User user) {
        log.info("Обновить пользователя по id = {}", user.getId());
        return ResponseEntity.ok(service.update(user));
    }

    @PutMapping("/{id}/friends/{friendId}")
    public ResponseEntity<?> addFriend(@PathVariable("id") Integer id,
                                       @PathVariable("friendId") Integer friendId) {
        log.info("Добавить пользователя по id = {} в друзья к пользователю по id = {}",id, friendId);
        return ResponseEntity.ok(service.addToFriends(id, friendId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        log.info("Удалить пользователя по id = {}", id);
        return ResponseEntity.ok(service.delete(id));
    }

    @GetMapping("/{id}/feed")
    public ResponseEntity<?> getFeedByUser(@PathVariable Integer id) {
        log.info("Получить историю событий пользователя по id = {}", id);
        return ResponseEntity.ok(service.getFeed(id));
    }

    @GetMapping("/{id}/recommendations")
    public ResponseEntity<?> getRecommendations(@PathVariable("id") Integer id) {
        log.info("Получить список рекомендаций для пользователя по id = {}", id);
        return ResponseEntity.ok(service.getRecommendations(id));
    }
}