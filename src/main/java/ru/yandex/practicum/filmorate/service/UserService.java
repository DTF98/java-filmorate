package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.UserStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class UserService {
    private final UserStorage storage;

    public User addToFriends(Integer userId, Integer friendId) {
        log.info("Добавление в друзья у пользователей {} и {}", userId, friendId);
        return storage.addFriend(userId, friendId);
    }

    public Integer removeFriend(Integer userId, Integer friendId) {
        log.info("Удаление из друзей userId={}, friendId={}", userId, friendId);
        return storage.removeFriend(userId, friendId);
    }

    public List<User> getFriends(Integer id) {
        log.info("Получение списка друзей id={}", id);
        return storage.getFriends(id);
    }

    public List<User> searchForCommonFriends(Integer id1, Integer id2) {
        List<User> friends1 = storage.getFriends(id1);
        List<User> friends2 = storage.getFriends(id2);
        friends1.retainAll(friends2);
        if (!friends1.isEmpty()) {
            log.info("Получен список общих друзей: {}", friends1);
            return friends1;
        } else {
            log.info("Общие друзья не найдены!");
            return new ArrayList<>();
        }
    }

    public List<User> getUsers() {
        return new ArrayList<>(storage.get());
    }

    public User setUser(User user) {
        User newUser = storage.add(user);
        log.info(String.format("Добавлен пользователь: {%s}", newUser));
        return user;
    }

    public User updateUser(User user) {
        User newUser = storage.update(user);
        log.info(String.format("Обновлён пользователь: {%s}", newUser));
        return newUser;
    }

    public User getById(Integer id) {
        return storage.getById(id).orElseThrow(() -> {
            String errorText = "Пользователь с таким Id не найден: " + id;
            log.error(errorText);
            return new NotFoundException(errorText);
        });
    }
}
