package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.impl.UserStorage;
import ru.yandex.practicum.filmorate.exception.IntersectionException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class UserService {
    private final UserStorage storage;

    public void addToFriends(Integer userId, Integer friendId) {
        storage.addFriend(userId, friendId);
        log.info(String.format("Создана дружба у пользователей %s и %s", userId, friendId));
    }

    public void removeFriend(Integer userId, Integer friendId) {
        storage.removeFriend(userId, friendId);
        log.info("Удалили из друзей userId={}, friendId={}", userId, friendId);
    }

    public List<User> getFriends(Integer id) {
        log.info("Получение списка друзей id={}", id);
        return storage.getFriends(id);
    }

    public List<User> searchForCommonFriends(Integer id1, Integer id2) {
        List<User> friends1 = storage.getFriends(id1);
        List<User> friends2 = storage.getFriends(id2);
        if (friends1.retainAll(friends2)) {
            log.info("Получен список общих друзей: {}", friends1);
            return friends1;
        } else {
            throw new IntersectionException("Общих друзей не найдено!");
        }
    }

    public List<User> getUsers() {
        return new ArrayList<>(storage.get());
    }

    public User setUser(User user) {
        User newUser = storage.set(user);
        log.info(String.format("Добавлен пользователь: {%s}", newUser));
        return user;
    }

    public User updateUser(User user) {
        User newUser = storage.update(user);
        log.info(String.format("Обновлён пользователь: {%s}", newUser));
        return newUser;
    }

    public User getUserById(Integer id) {
        User user = storage.getById(id);
        log.info(String.format("Получен пользователь : %s", id));
        return user;
    }

}
