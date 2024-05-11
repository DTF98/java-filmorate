package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.UserStorage;
import ru.yandex.practicum.filmorate.exception.AddFeedException;
import ru.yandex.practicum.filmorate.exception.ApplicationException;
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
    private FilmService filmService;

    public User addToFriends(Integer userId, Integer friendId) {
        try {
            log.info("Добавление в друзья пользователей {} и {}", userId, friendId);
            Optional<User> user = userStorage.getById(userId);
            if (user.isPresent() && userStorage.addFriend(userId, friendId)) {
                if (userFeedService.addInHistoryFeed(userId, "FRIEND", "ADD", friendId)) {
                    log.info("Добавлено в историю добавление друга у пользователя id = {}", userId);
                    return user.get();
                } else {
                    throw new AddFeedException(String.format("Ошибка добваления в историю дружбы пользователей по id = %s," +
                            " и id = %s", userId, friendId));
                }
            }
        } catch (DataAccessException e) {
            log.error("Ошибка при добавлении в друзья", e);
            throw new NotFoundException("Не найден пользователь!");
        }
        throw new ApplicationException(String.format("Ошибка при добавлении в друзья пользователей по id = %s и %s",
                userId, friendId));
    }

    public Integer deleteFriend(Integer userId, Integer friendId) {
        try {
            log.info("Удаление из друзей userId={}, friendId={}", userId, friendId);
            if (userStorage.deleteFriend(userId, friendId)) {
                log.info("Пользователь id = {} удалил из друзей пользователя id = {}", userId, friendId);
                if (userFeedService.addInHistoryFeed(userId, "FRIEND", "REMOVE", friendId)) {
                    log.info("Добавлено в историю удаление друга у пользователя id = {}", userId);
                    return friendId;
                } else {
                    throw new AddFeedException(String.format("Ошибка удаления из друзей пользователей по id = %s," +
                            " и id = %s", userId, friendId));
                }
            }
        } catch (DataAccessException e) {
            log.error("Ошибка при удалении из друзей");
            throw new NotFoundException("Не найден пользователь!");
        }
        throw new ApplicationException(String.format("Ошибка при удалении из друзей пользователей по id = %s и %s",
                userId, friendId));
    }

    public List<User> getFriends(Integer id) {
        try {
            log.info("Получение списка друзей id={}", id);
            if (userStorage.isExistById(id)) {
                List<User> friends = userStorage.getFriends(id);
                log.info("Получен список друзей id={}", id);
                return friends;
            } else {
                throw new NotFoundException("Не найден пользователь!");
            }
        } catch (DataAccessException e) {
            log.error("Ошибка при получении списка друзей");
        }
        throw new ApplicationException("Ошибка при получении списка друзей");
    }

    public List<User> searchForCommonFriends(Integer id1, Integer id2) {
        try {
            if (userStorage.isExistById(id1) && userStorage.isExistById(id2)) {
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
            } else {
                throw new NotFoundException("Не найден пользователь!");
            }
        } catch (DataAccessException e) {
            log.error("Ошибка при получении списка общих друзей");
        }
        throw new ApplicationException("Ошибка при получении списка общих друзей");
    }

    public List<User> get() {
        try {
            log.info("Получение списка всех пользователей");
            return new ArrayList<>(userStorage.get());
        } catch (DataAccessException e) {
            log.error("Ошибка при получении списка пользователей");
        }
        throw new ApplicationException("Ошибка при получении списка пользователей");
    }

    public User add(User user) {
        try {
            User newUser = userStorage.add(user);
            if (newUser.getId() == 0) {
                throw new CreateEntityException(String.format("Пользователь не добавлен! %s", user));
            }
            log.info(String.format("Добавлен пользователь: {%s}", newUser));
            return newUser;
        } catch (DataAccessException e) {
            log.error("Ошибка при добавлении пользователя");
        }
        throw new ApplicationException("Ошибка при добавлении пользователя");
    }

    public User update(User user) {
        try {
            User newUser = userStorage.update(user);
            if (newUser.getId() == 0) {
                throw new NotFoundException("Не найден пользователь!");
            }
            log.info(String.format("Обновлён пользователь: {%s}", newUser));
            return newUser;
        } catch (DataAccessException e) {
            log.error("Ошибка при обновлении пользователя");
        }
        throw new ApplicationException("Ошибка при обновлении пользователя");
    }

    public boolean delete(Integer userID) {
        try {
            log.info("Удаление юзера id = {}", userID);
            boolean isDelete = userStorage.delete(userID);
            if (!isDelete) {
                throw new NotFoundException(String.format("Пользователь по id = {%s} не найдеен", userID));
            } else {
                log.info("Удален пользователь id = {}", userID);
                return true;
            }
        } catch (DataAccessException e) {
            log.error("Ошибка при удалении пользователя");
        }
        throw new ApplicationException("Ошибка при удалении пользователя");
    }

    public User getUserById(Integer id) {
        try {
            Optional<User> user = userStorage.getById(id);
            if (user.isPresent()) {
                log.info("Получен пользователь по id = {}", id);
                return user.get();
            }
        } catch (DataAccessException e) {
            log.error("Ошибка при получении пользователя по id = {}", id);
            throw new NotFoundException(String.format("Пользователь по id = {%s} не найдеен", id));
        }
        throw new ApplicationException("Ошибка при получении пользователя");
    }

    public List<UserFeed> getFeed(Integer userId) {
        try {
            log.info("Получение ленты событий для пользователя по id = {}", userId);
            if (!userStorage.isExistById(userId)) {
                throw new NotFoundException(String.format("Отзывов по id = %s не найдено!", userId));
            }
            return userFeedService.getFeedByUserId(userId);
        } catch (DataAccessException e) {
            log.error("Ошибка при получении ленты событий для пользователя по id = {}", userId);
        }
        throw new ApplicationException("Ошибка при получении ленты событий");
    }

    public List<Film> getRecommendations(int id) {
        try {
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
                    films.add(filmService.getById(i));
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
                        anotherFilms.add(filmService.getById(i));
                    }
                }
                return anotherFilms;
            } else {
                return films;
            }
        } catch (DataAccessException e) {
            log.error("Ошибка при получении рекомендаций по id = {}", id);
        }
        throw new ApplicationException("Ошибка при получении рекомендаций");
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
