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
    private final UserStorage storage;
    private int id = 1;


    @Autowired
    public UserService(UserStorage storage) {
        this.storage = storage;
    }

    public void addToFriends(Integer id1, Integer id2) throws ValidationException {
        if (storage.containsUser(id1) && storage.containsUser(id2)) {
            storage.getUserById(id1).setFriend(id2);
            storage.getUserById(id2).setFriend(id1);
            log.info(String.format("Создана дружба у пользователей %s и %s", id1, id2));
        } else {
            throw new ValidationException("Не найден пользователь!");
        }
    }

    public void removeFriend(Integer id1, Integer id2) throws ValidationException {
        if (storage.containsUser(id2) && storage.containsUser(id1)) {
            if (storage.getUserById(id1).getFriends()
                    .contains(id2) && storage.getUserById(id2).getFriends()
                    .contains(id1)) {
                log.info(String.format("Удалена дружба! У пользователей %s и %s", id1, id2));
                storage.getUserById(id1).removeFriend(id2);
                storage.getUserById(id2).removeFriend(id1);
            }
        } else {
            throw new ValidationException("Не найден пользователь!");
        }
    }

    public HashSet<Integer> searchForCommonFriends(Integer id1, Integer id2) throws IntersectionException, ValidationException {
        if (storage.containsUser(id1) && storage.containsUser(id2)) {
            HashSet<Integer> buf = new HashSet<>(storage.getUserById(id1).getFriends());
            if (buf.retainAll(storage.getUserById(id2).getFriends())) {
                log.info("Получен список общих друзей: {}", buf);
                return buf;
            } else {
                throw new IntersectionException("Общих друзей не найдено!");
            }
        } else {
            throw new ValidationException("Пользовате(ль)/(ли) не существу(ет)/(ют)!");
        }
    }

    public List<User> getUsers() {
        return new ArrayList<>(storage.getUsers());
    }

    public void setUser(User user) {
        user.setId(id);
        id++;
        storage.setUser(user);
        log.info(String.format("Добавлен пользователь: {%s}", storage.getUserById(user.getId())));
    }

    public void updateUser(User user) {
        if (storage.containsUser(user.getId())) {
            storage.setUser(user);
            log.info(String.format("Обновлён пользователь: {%s}", storage.getUserById(user.getId())));
        } else {
            throw new NotFoundException(String.format("Пользователь не найден!"));
        }
    }

    public User getUserById(Integer id) {
        if (storage.containsUser(id)) {
            log.info(String.format("Получен список друзей : %s", storage.getUserById(id).getFriends()));
            return storage.getUserById(id);
        } else {
            throw new NotFoundException(String.format("Пользователь по id = %s не найден!", id));
        }
    }
}
