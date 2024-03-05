package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

@RestController
@Slf4j
public class UserController {
    private int id = 1;
    private final UserStorage storage = new UserStorage();

    @GetMapping("/users")
    public List<User> findAll() {
        return storage.getUsers();
    }

    @PostMapping(value = "/users")
    public ResponseEntity<?> create(@RequestBody User user) {
        try {
            User userCheck = ckeckingUser(user);
            storage.setUser(userCheck);
            log.info("Добавлен пользователь: {}", storage.getUserById(userCheck.getId()));
            return new ResponseEntity<>(userCheck, HttpStatus.OK);
        } catch (ValidationException e) {
            log.debug(e.getMessage());
            return new ResponseEntity<>(user, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping(value = "/users")
    public ResponseEntity<?> updateOrCreateNew(@RequestBody User user) {
        if (user.getId() != null) {
            try {
                User userCheck = ckeckingUser(user);
                if (storage.containsUser(userCheck.getId())) {
                    storage.setUser(userCheck);
                    log.info("Обновлён пользователь: {}", storage.getUserById(userCheck.getId()));
                    return new ResponseEntity<>(userCheck, HttpStatus.OK);
                } else {
                    log.info("User по такому ID не существует");
                    return new ResponseEntity<>(userCheck, HttpStatus.NOT_FOUND);
                }
            } catch (ValidationException e) {
                log.error(e.getMessage());
                return new ResponseEntity<>(user, HttpStatus.BAD_REQUEST);
            }
        } else {
            return create(user);
        }
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
