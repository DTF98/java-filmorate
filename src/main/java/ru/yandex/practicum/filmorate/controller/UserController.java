package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;

import static ru.yandex.practicum.filmorate.util.ResponseUtil.respondSuccess;
import static ru.yandex.practicum.filmorate.util.ResponseUtil.respondSuccessList;

@RestController
@Slf4j
public class UserController {
    private final UserService service;

    @Autowired
    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/users")
    public ResponseEntity<?> findAll() {
        return respondSuccessList(service.getUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getById(@PathVariable int id) {
        log.info("Получить пользователя по ID - {}", id);

        return respondSuccess(service.getUserById(id));
    }

    @GetMapping("/users/{id}/friends")
    public ResponseEntity<?> getFriends(@PathVariable("id") Integer id) {
        return respondSuccessList(service.getFriends(id));
    }

    @GetMapping("/users/{id}/friends/common/{otherId}")
    public ResponseEntity<?> getListOfMutualFriends(@PathVariable("id") Integer id,
                                                    @PathVariable("otherId") Integer otherId) {
        return respondSuccessList(service.searchForCommonFriends(id, otherId));
    }

    @DeleteMapping("/users/{id}/friends/{friendId}")
    public ResponseEntity<?> deleteFriend(@PathVariable("id") Integer id,
                                          @PathVariable("friendId") Integer friendId) {
        return respondSuccess(service.deleteFriend(id, friendId));
    }

    @PostMapping(value = "/users")
    public ResponseEntity<?> create(@Valid @RequestBody User user) {
        return respondSuccess(service.setUser(user));
    }

    @PutMapping(value = "/users")
    public ResponseEntity<?> update(@Valid @RequestBody User user) {
        return respondSuccess(service.updateUser(user));
    }

    @PutMapping("/users/{id}/friends/{friendId}")
    public ResponseEntity<?> addFriend(@PathVariable("id") Integer id,
                                       @PathVariable("friendId") Integer friendId) {
        return respondSuccess(service.addToFriends(id, friendId));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
        return respondSuccess(HttpStatus.OK);
    }
    @GetMapping("/users/{id}/recommendations")
    public ResponseEntity<?> getRecommendations(@PathVariable("id") Integer id) {
        return respondSuccessList(service.getRecommendations(id));
    }

    @GetMapping("/users/{id}/recommendations")
    public ResponseEntity<?> getRecommendations(@PathVariable("id") Integer id) {
        return respondSuccessList(service.getRecommendations(id));
    }

}