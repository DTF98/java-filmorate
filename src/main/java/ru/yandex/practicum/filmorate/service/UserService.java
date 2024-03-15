package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.IntersectionException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
@Slf4j
public class UserService {
    UserStorage inMemoryUserStorage;

    @Autowired
    public UserService(UserStorage inMemoryUserStorage) {
        this.inMemoryUserStorage = inMemoryUserStorage;
    }

    public void addToFriends(Integer id1, Integer id2) throws ValidationException {
        if (inMemoryUserStorage.containsUser(id1) && inMemoryUserStorage.containsUser(id2)) {
            inMemoryUserStorage.getUserById(id1).setFriend(id2);
            inMemoryUserStorage.getUserById(id2).setFriend(id1);
            log.info(String.format("Создана дружба у пользователей %s и %s", id1, id2));
        } else {
            throw new ValidationException("Не найден пользователь!");
        }
    }

    public void removeFriend(Integer id1, Integer id2) throws ValidationException {
        if (inMemoryUserStorage.containsUser(id2) && inMemoryUserStorage.containsUser(id1)) {
            if (inMemoryUserStorage.getUserById(id1).getFriends()
                    .contains(id2) && inMemoryUserStorage.getUserById(id2).getFriends()
                    .contains(id1)) {
                log.info(String.format("Удалена дружба! У пользователей %s и %s", id1, id2));
                inMemoryUserStorage.getUserById(id1).removeFriend(id2);
                inMemoryUserStorage.getUserById(id2).removeFriend(id1);
            }
        } else {
            throw new ValidationException("Не найден пользователь!");
        }
    }

    public HashSet<Integer> searchForCommonFriends(Integer id1, Integer id2) throws IntersectionException, ValidationException {
        if (inMemoryUserStorage.containsUser(id1) && inMemoryUserStorage.containsUser(id2)) {
            HashSet<Integer> buf = new HashSet<>(inMemoryUserStorage.getUserById(id1).getFriends());
            if (buf.retainAll(inMemoryUserStorage.getUserById(id2).getFriends())) {
                log.info(String.format("Получен список общих друзей: %s", buf));
                return buf;
            } else {
                throw new IntersectionException("Общих друзей не найдено!");
            }
        } else {
            throw new ValidationException("Пользовате(ль)/(ли) не существу(ет)/(ют)!");
        }
    }

    public List<User> getUsers() {
        return new ArrayList<>(inMemoryUserStorage.getUsers());
    }

    public void setUser(User user) {
        inMemoryUserStorage.setUser(user);
        log.info(String.format("Добавлен пользователь: {%s}", inMemoryUserStorage.getUserById(user.getId())));
    }

    public void updateUser(User user) {
        if (inMemoryUserStorage.containsUser(user.getId())) {
            inMemoryUserStorage.setUser(user);
            log.info(String.format("Обновлён пользователь: {%s}", inMemoryUserStorage.getUserById(user.getId())));
        } else {
            throw new NotFoundException(String.format("Пользователь не найден!"));
        }
    }

    public User getUserById(Integer id) {
        if (inMemoryUserStorage.containsUser(id)) {
            log.info(String.format("Получен список друзей : %s", inMemoryUserStorage.getUserById(id).getFriends()));
            return inMemoryUserStorage.getUserById(id);
        } else {
            throw new NotFoundException(String.format("Пользователь по id = %s не найден!", id));
        }
    }
}
