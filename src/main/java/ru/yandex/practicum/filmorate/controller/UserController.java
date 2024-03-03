package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

@RestController
@Slf4j
public class UserController {
    private final HashMap<Integer, User> users = new HashMap<>();
    private int id = 1;

    @GetMapping("/users")
    public List<User> findAll() {
        List<User> listOfUsers = new ArrayList<>();
        users.forEach((k,v) -> listOfUsers.add(v));
        return listOfUsers;
    }

    @PostMapping(value = "/users")
    public User create(@RequestBody User user) {
        User userCheck = ckeckingUser(user);
        if (userCheck.getId() != null) {
            users.put(userCheck.getId(), userCheck);
            log.info("Добавлен пользователь: {}", users.get(userCheck.getId()));
            return userCheck;
        } else {
            return user;
        }
    }

    @PutMapping(value = "/users")
    public User updateOrCreateNew(@RequestBody User user) {
        if (user.getId() != null) {
            if (users.containsKey(user.getId())) {
                users.put(user.getId(), user);
                log.info("Обновлён пользователь: {}", users.get(user.getId()));
                return user;
            } else {
                log.info("User по такому ID не существует");
                throw new ValidationException("User по такому ID не существует");
            }
        } else {
            return create(user);
        }
    }

    private User ckeckingUser(User user) {
        if (Pattern.matches("^(.+)@(.+)$", user.getEmail())) {
            if (user.getLogin() != null && !user.getLogin().isEmpty() && !user.getLogin().isBlank()) {
                if (user.getName() == null || user.getName().isEmpty()) {
                    user.setName(user.getLogin());
                }
                if (!(user.getBirthday().isAfter(LocalDate.now()))) {
                    user.setId(id);
                    id++;
                    return user;
                } else {
                    log.info("дата рождения не может быть в будущем: {}", user.getBirthday());
                    throw new ValidationException("дата рождения не может быть в будущем");
                }
            } else {
                log.info("логин не может быть пустым и содержать пробелы: {}", user.getLogin());
                throw new ValidationException("логин не может быть пустым и содержать пробелы");
            }
        } else {
            log.info("электронная почта не может быть пустой и должна содержать символ @: {}", user.getEmail());
            throw new ValidationException("электронная почта не может быть пустой и должна содержать символ @");
        }
    }
}
