package ru.yandex.practicum.filmorate.dao.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.FilmStorage;
import ru.yandex.practicum.filmorate.dao.UserStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
@AllArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmStorage filmStorage;

    public List<User> get() {
        return jdbcTemplate.query("select * from users order by id", this::mapRowToUser);
    }

    public User update(User item) {
        try {
            String sqlFilms = "UPDATE users SET email = ?, login = ?, name = ?, " +
                    "birthday = ? WHERE id = ?;";
            if (jdbcTemplate.update(sqlFilms, item.getEmail(), item.getLogin(), item.getName(),
                    item.getBirthday(), item.getId()) > 0) {
                return item;
            } else {
                throw new NotFoundException("Пользователь не найден!");
            }
        } catch (DataAccessException e) {
            log.error("Ошибка при обновлении пользователя по id = {}", item.getId());
            return null;
        }
    }

    public User add(User user) {
        String sql = "insert into users (email, login, name, birthday) values (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"id"});
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            stmt.setString(4, String.valueOf(user.getBirthday()));
            return stmt;
        }, keyHolder);
        Integer userId = Objects.requireNonNull(keyHolder.getKey()).intValue();
        user.setId(userId);
        return user;
    }

    public Optional<User> getById(Integer id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("select * from users where id = ?",
                    this::mapRowToUser, id));
        } catch (DataAccessException e) {
            throw new NotFoundException(String.format("Пользователь по id = %s не найден!", id));
        }
    }

    public User addFriend(Integer user, Integer friend) {
        try {
            String sqlGetFriends = "SELECT * FROM USER_FRIENDS WHERE user_id = ? AND friend_id = ?";
            SqlRowSet user1Friends = jdbcTemplate.queryForRowSet(sqlGetFriends, user, friend);
            SqlRowSet user2Friends = jdbcTemplate.queryForRowSet(sqlGetFriends, friend, user);
            if (!user1Friends.next()) {
                jdbcTemplate.update("INSERT INTO USER_FRIENDS (user_id, friend_id) VALUES (?, ?)",
                        user, friend);
            }
            if (user2Friends.next()) {
                jdbcTemplate.update("UPDATE USER_FRIENDS SET status = ? WHERE user_id IN (?, ?) AND friend_id IN (?, ?)",
                        true, user, friend, user, friend);
            }
            Optional<User> userBuf = getById(user);
            if (userBuf.isPresent()) {
                return userBuf.get();
            } else {
                throw new NotFoundException("Не найден пользователь!");
            }
        } catch (DataAccessException e) {
            throw new NotFoundException("Не найден пользователь!");
        }
    }

    public Integer deleteFriend(Integer user, Integer friend) {
        if (isExistById(user) && isExistById(friend)) {
            try {
                int updated = jdbcTemplate.update("DELETE FROM USER_FRIENDS WHERE user_id = ? AND friend_id = ?",
                        user, friend);
                if (updated > 0) {
                    jdbcTemplate.update("UPDATE USER_FRIENDS SET status = ? WHERE user_id = ? AND friend_id = ?",
                            false, friend, user);
                }
                return friend;
            } catch (DataAccessException e) {
                log.error("Ошибка при удалении из друзей");
                return null;
            }
        } else {
            throw new NotFoundException("Не найден пользователь!");
        }
    }

    public boolean delete(Integer id) {
        String sqlQuery = "DELETE FROM users WHERE ID= ?";
        deleteFriendsLinks(id);
        deleteLikesLinks(id);
        return jdbcTemplate.update(sqlQuery, id) > 0;
    }

    public void deleteFriendsLinks(Integer userId) {
        jdbcTemplate.update("DELETE FROM USER_FRIENDS WHERE user_id = ? OR friend_id = ?", userId, userId);
    }

    public void deleteLikesLinks(Integer userId) {
        jdbcTemplate.update("DELETE FROM FILM_LIKES WHERE user_id = ?", userId);
    }

    public List<User> getFriends(Integer userId) {
        if (isExistById(userId)) {
            try {
                return jdbcTemplate.query(String.format("SELECT * FROM USER_FRIENDS AS ur " +
                        "LEFT JOIN users AS u ON ur.friend_id = u.id WHERE ur.user_id = %s", userId), this::mapRowToUser);
            } catch (DataAccessException e) {
                log.error("Ошибка при получении списка друзей");
                return null;
            }
        } else {
            throw new NotFoundException("Не найден пользователь!");
        }
    }

    private User mapRowToUser(ResultSet resultSet, int rowNum) throws SQLException {
        return User.builder()
                .id(resultSet.getInt("id"))
                .email(resultSet.getString("email"))
                .login(resultSet.getString("login"))
                .name(resultSet.getString("name"))
                .birthday(LocalDate.parse(resultSet.getString("birthday")))
                .build();
    }

    private boolean isExistById(Integer id) {
        String sqlQuery = "SELECT EXISTS(SELECT 1 FROM USERS WHERE ID = ?)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sqlQuery, Boolean.class, id));
    }

    @Override
    public List<Optional<Film>> getRecommendations(int id) {
        SqlRowSet userLike = jdbcTemplate.queryForRowSet("select film_id from film_likes where user_id=?", id);
        List<Integer> currentFilmIdsUser = new ArrayList<>();
        while (userLike.next()) {
            currentFilmIdsUser.add(userLike.getInt("film_id"));
        }
        if (currentFilmIdsUser.isEmpty()) {
            List<Optional<Film>> recommendations = new ArrayList<>();
            log.info(String.format("Для пользователя %d нет рекомендаций.", id));
            return recommendations;
        }

        Optional<User> anotherUser = getUserWithMaxCommonLikes(id);
        if (anotherUser == null) {
            List<Optional<Film>> recommendations = new ArrayList<>();
            log.info(String.format("Для пользователя %d нет рекомендаций.", id));
            return recommendations;
        }
        SqlRowSet anotherUserLike = jdbcTemplate.queryForRowSet("select film_id from film_likes where user_id=?",
                anotherUser.get().getId());

        List<Integer> filmIdsAnotherUser = new ArrayList<>();
        while (anotherUserLike.next()) {
            filmIdsAnotherUser.add(anotherUserLike.getInt("film_id"));
        }

        List<Optional<Film>> films = new ArrayList<>();
        for (Integer i : filmIdsAnotherUser) {
            if (!currentFilmIdsUser.contains(i)) {
                films.add(filmStorage.getById(i));
            }
        }
        if (films.isEmpty()) {
            Optional<User> newUser = getAnotherUserWithMaxCommonLikes(id);

            if (newUser == null) {
                List<Optional<Film>> recommendations = new ArrayList<>();
                log.info(String.format("Для пользователя %d нет рекомендаций.", id));
                return recommendations;
            }
            SqlRowSet newUserLikes = jdbcTemplate.queryForRowSet("select film_id from film_likes where user_id=?",
                    newUser.get().getId());

            List<Integer> filmIdsNewUser = new ArrayList<>();
            while (newUserLikes.next()) {
                filmIdsNewUser.add(newUserLikes.getInt("film_id"));
            }

            List<Optional<Film>> anotherListFilms = new ArrayList<>();
            for (Integer i : filmIdsNewUser) {
                if (!currentFilmIdsUser.contains(i)) {
                    anotherListFilms.add(filmStorage.getById(i));
                }
            }
            return anotherListFilms;
        } else {
            return films;
        }
    }


    public Optional<User> getUserWithMaxCommonLikes(int userId) {
        List<Integer> userIds = new ArrayList<>();
        String sql = "select user_id from film_likes where film_id IN " +
                "\n(select film_id from film_likes where user_id=?)";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql, userId);
        while (rs.next()) {
            if (userId != rs.getInt("user_id")) {
                userIds.add(rs.getInt("user_id"));
            }
        }
        if (userIds.isEmpty()) {
            return null;
        }
        return getById(mostCommon(userIds));
    }


    public Optional<User> getAnotherUserWithMaxCommonLikes(int userId) {
        List<Integer> userIds = new ArrayList<>();

        String sql = "select user_id from film_likes where film_id IN " +
                "\n(select film_id from film_likes where user_id=?)";

        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql, userId);
        while (rs.next()) {
            if (userId != rs.getInt("user_id")) {
                userIds.add(rs.getInt("user_id"));
            }
        }
        if (userIds.isEmpty()) {
            return null;
        }

        Optional<User> newUser = getById(mostCommon(userIds));

        SqlRowSet newUserLikesSql = jdbcTemplate.queryForRowSet("select film_id from FILM_LIKES where user_id=?",
                newUser.get().getId());
        List<Integer> newUserLikes = new ArrayList<>();
        while (newUserLikesSql.next()) {
            newUserLikes.add(newUserLikesSql.getInt("film_id"));
        }
        if (newUserLikes.isEmpty()) {
            return null;
        }

        SqlRowSet currentUserLikesSql = jdbcTemplate.queryForRowSet("select film_id from film_likes where user_id=?", userId);
        List<Integer> currentUserLikes = new ArrayList<>();

        while (currentUserLikesSql.next()) {
            currentUserLikes.add(currentUserLikesSql.getInt("film_id"));
        }

        List<Integer> newListUserIds = new ArrayList<>();
        if (currentUserLikes.containsAll(newUserLikes)) {
            for (Integer id : userIds) {
                if (!id.equals(newUser.get().getId())) {
                    newListUserIds.add(id);
                }
            }
        }

        if (newListUserIds.isEmpty()) {
            return null;
        }


        return getById(mostCommon(newListUserIds));
    }


    public static <T> T mostCommon(List<T> list) {
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

        return max.getKey();
    }
}
