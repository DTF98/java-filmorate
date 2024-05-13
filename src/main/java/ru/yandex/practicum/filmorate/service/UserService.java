package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.FilmStorage;
import ru.yandex.practicum.filmorate.dao.UserStorage;
import ru.yandex.practicum.filmorate.exception.CreateEntityException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.UserFeed;

import java.util.*;

@Service
@Slf4j
@AllArgsConstructor
public class UserService {
    private final UserStorage userStorage;
    private final UserFeedService userFeedService;
    private FilmStorage filmStorage;

    public User addToFriends(Integer userId, Integer friendId) {
        log.info("Добавление в друзья пользователей {} и {}", userId, friendId);
        User user = getUserById(userId);
        getUserById(friendId);
        userStorage.addFriend(userId, friendId);
        userFeedService.addInHistoryFeed(userId, "FRIEND", "ADD", friendId);
        log.info("Добавлено в историю добавление друга у пользователя id = {}", userId);
        return user;
    }

    public User deleteFriend(Integer userId, Integer friendId) {
        log.info("Удаление из друзей userId={}, friendId={}", userId, friendId);
        getUserById(userId);
        User friend = getUserById(friendId);
        userStorage.deleteFriend(userId, friendId);
        log.info("Пользователь id = {} удалил из друзей пользователя id = {}", userId, friendId);
        userFeedService.addInHistoryFeed(userId, "FRIEND", "REMOVE", friendId);
        log.info("Добавлено в историю удаление друга у пользователя id = {}", userId);
        return friend;
    }

    public List<User> getFriends(Integer id) {
        log.info("Получение списка друзей id={}", id);
        getUserById(id);
        List<User> friends = userStorage.getFriends(id);
        log.info("Получен список друзей id={}", id);
        return friends;
    }

    public List<User> searchForCommonFriends(Integer id1, Integer id2) {
        getUserById(id1);
        getUserById(id2);
        List<User> friends1 = userStorage.getFriends(id1);
        List<User> friends2 = userStorage.getFriends(id2);
        friends1.retainAll(friends2);
        if (!friends1.isEmpty()) {
            log.info("Получен список общих друзей: {}", friends1);
            return friends1;
        } else {
            log.info("Общие друзья не найдены!");
            return new ArrayList<>();
        }
    }

    public List<User> get() {
        log.info("Получение списка всех пользователей");
        return new ArrayList<>(userStorage.get());
    }

    public User add(User user) {
        User newUser = userStorage.add(user);
        if (newUser.getId() == null) {
            throw new CreateEntityException(String.format("Пользователь не добавлен! %s", user));
        }
        log.info(String.format("Добавлен пользователь: {%s}", newUser));
        return newUser;
    }

    public User update(User user) {
        getUserById(user.getId());
        User newUser = userStorage.update(user);
        log.info(String.format("Обновлён пользователь: {%s}", newUser));
        return newUser;
    }

    public User delete(Integer userID) {
        log.info("Удаление юзера id = {}", userID);
        User user = getUserById(userID);
        userStorage.delete(userID);
        log.info("Удален пользователь id = {}", userID);
        return user;
    }

    public User getUserById(Integer id) {
        User user = userStorage.getById(id).orElseThrow(() ->
                new NotFoundException(String.format("Не найден пользователь по id = %s", id)));
        log.info("Получен пользователь по id = {}", id);
        return user;
    }

    public List<UserFeed> getFeed(Integer userId) {
        log.info("Получение ленты событий для пользователя по id = {}", userId);
        getUserById(userId);
        return userFeedService.getFeedByUserId(userId);
    }

    public List<Film> getRecommendations(int id) {
        log.info("Полученние рекомендаций по id = {}", id);

        List<Integer> currentFilmIdsUser = userStorage.getUserLikes(id);
        if (currentFilmIdsUser.isEmpty()) {
            log.info(String.format("Для пользователя %d нет рекомендаций.", id));
            return new ArrayList<>();
        }
        List<Integer> ids = userStorage.getListUsersWithCommonLikes(id);
        Optional<User> anotherUser;
        if (ids.isEmpty()) {
            anotherUser = Optional.empty();
        } else {
            anotherUser = userStorage.getById(mostCommon(ids));
        }
        if (anotherUser.isEmpty()) {
            log.info(String.format("Для пользователя %d нет рекомендаций.", id));
            return new ArrayList<>();
        }
        List<Integer> filmIdsAnotherUser = userStorage.getUserLikes(anotherUser.get().getId());
        List<Film> films = new ArrayList<>();
        for (Integer i : filmIdsAnotherUser) {
            if (!currentFilmIdsUser.contains(i)) {
                films.add(filmStorage.getById(i).orElseThrow(() ->
                        new NotFoundException(String.format("Фильм по id = %s не найден", i))));
            }
        }

        if (films.isEmpty()) {
            Optional<User> newUser = getAnotherUserWithMaxCommonLikes(id);
            if (newUser.isEmpty()) {
                log.info(String.format("Для пользователя %d нет рекомендаций.", id));
                return new ArrayList<>();
            }
            List<Integer> newFilmIdsUser = userStorage.getUserLikes(newUser.get().getId());
            List<Film> anotherFilms = new ArrayList<>();
            for (Integer i : newFilmIdsUser) {
                if (!currentFilmIdsUser.contains(i)) {
                    anotherFilms.add(filmStorage.getById(i).orElseThrow(() ->
                            new NotFoundException(String.format("Фильм по id = %s не найден", i))));
                }
            }
            return anotherFilms;
        } else {
            return films;
        }
    }

    private Optional<User> getAnotherUserWithMaxCommonLikes(Integer id) {
        Optional<User> newUser;
        List<Integer> userIds = userStorage.getListUsersWithCommonLikes(id);
        if (userIds.isEmpty()) {
            return Optional.empty();
        } else {
            newUser = userStorage.getById(mostCommon(userIds));
        }
        List<Integer> newUserLikes = userStorage.getUserLikes(newUser.get().getId());
        if (newUserLikes.isEmpty()) {
            return Optional.empty();
        }

        List<Integer> currentUserLikes = userStorage.getUserLikes(id);

        List<Integer> newListUserIds = new ArrayList<>();
        if (new HashSet<>(currentUserLikes).containsAll(newUserLikes)) {
            for (Integer elem : userIds) {
                if (!elem.equals(newUser.get().getId())) {
                    newListUserIds.add(elem);
                }
            }
        }

        if (newListUserIds.isEmpty()) {
            return Optional.empty();
        }

        return userStorage.getById(mostCommon(newListUserIds));
    }

    private  <T> T mostCommon(List<T> list) {

        Map<T, Integer> map = new HashMap<>();

        for (T t : list) {
            Integer val = map.get(t);
            map.put(t, val == null ? 1 : val + 1);
        }

        Map.Entry<T, Integer> max = null;

        for (Map.Entry<T, Integer> e : map.entrySet()) {
            if (max == null || e.getValue() > max.getValue())
                max = e;
        }

        return Objects.requireNonNull(max).getKey();
    }
}
