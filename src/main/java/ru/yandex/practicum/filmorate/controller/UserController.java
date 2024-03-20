package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class UserController {
    private final UserService service;

    @Autowired
    public UserController(UserStorage storage) {
        this.service = new UserService(storage);
    }

    @GetMapping("/users")
    public List<User> findAll() {
        return service.getUsers();
    }

    @GetMapping("/users/{id}/friends")
    public ResponseEntity<?> getFriends(@PathVariable("id") Integer id) {
        return new ResponseEntity<>(service.getUserById(id).getFriends()
                .stream()
                .map(service::getUserById)
                .collect(Collectors.toList()), HttpStatus.OK);
    }

    @GetMapping("/users/{id}/friends/common/{otherId}")
    public ResponseEntity<?> getListOfMutualFriends(@PathVariable("id") Integer id,
                                                    @PathVariable("otherId") Integer otherId) {
        return new ResponseEntity<>(service.searchForCommonFriends(id, otherId).stream()
                .map(service::getUserById)
                .collect(Collectors.toList()),
                HttpStatus.OK);
    }

    @DeleteMapping("/users/{id}/friends/{friendId}")
    public HttpStatus deleteFriend(@PathVariable("id") Integer id,
                                 @PathVariable("friendId") Integer friendId) {
        service.removeFriend(id, friendId);
        return HttpStatus.OK;
    }

    @PostMapping(value = "/users")
    public ResponseEntity<?> create(@Valid @RequestBody User user) {
        service.setUser(user);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PutMapping(value = "/users")
    public ResponseEntity<?> updateOrCreateNew(@Valid @RequestBody User user) {
        if (user.getId() != null) {
            service.updateUser(user);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } else {
            return create(user);
        }
    }

    @PutMapping("/users/{id}/friends/{friendId}")
    public ResponseEntity<?> addFriend(@PathVariable("id") Integer id,
                                       @PathVariable("friendId") Integer friendId) {
        service.addToFriends(id, friendId);
        return new ResponseEntity<>(service.getUserById(id), HttpStatus.OK);
    }
}
