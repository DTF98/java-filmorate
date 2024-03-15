package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class UserController {
    private int id = 1;

    private final UserService service;

    @Autowired
    public UserController(InMemoryUserStorage storage) {
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
    public ResponseEntity<?> create(@RequestBody User user) {
        User userCheck = ckeckingUser(user);
        service.setUser(userCheck);
        return new ResponseEntity<>(userCheck, HttpStatus.OK);
    }

    @PutMapping(value = "/users")
    public ResponseEntity<?> updateOrCreateNew(@RequestBody User user) {
        if (user.getId() != null) {
            User userCheck = ckeckingUser(user);
            service.updateUser(userCheck);
            return new ResponseEntity<>(userCheck, HttpStatus.OK);
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

    private User ckeckingUser(User user) {
        if (Pattern.matches("^(.+)@(.+)$", user.getEmail())) {
            if (user.getLogin() != null && !user.getLogin().isEmpty() && !user.getLogin().contains(" ")) {
                if (user.getName() == null || user.getName().isEmpty()) {
                    user.setName(user.getLogin());
                }
                if (!(user.getBirthday().isAfter(LocalDate.now()))) {
                    if (user.getId() == null) {
                        user.setId(id);
                        id++;
                    }
                    return user;
                } else {
                    throw new ValidationException("дата рождения не может быть в будущем: " + user.getBirthday());
                }
            } else {
                throw new ValidationException("логин не может быть пустым и содержать пробелы: " + user.getLogin());
            }
        } else {
            throw new ValidationException("электронная почта не может быть пустой и должна содержать символ @: "
                    + user.getEmail());
        }
    }
}
