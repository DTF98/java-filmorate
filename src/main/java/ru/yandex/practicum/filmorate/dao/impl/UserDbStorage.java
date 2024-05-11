package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.UserStorage;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.UserFeed;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static ru.yandex.practicum.filmorate.constant.ConstantError.ERROR_ENTITY_USER;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    public List<User> get() {
        return jdbcTemplate.query("select * from users order by id", this::mapRowToUser);
    }

    public User update(User item) {
        String sqlFilms = "UPDATE users SET email = ?, login = ?, name = ?, " +
                "birthday = ? WHERE id = ?;";
        if (jdbcTemplate.update(sqlFilms, item.getEmail(), item.getLogin(), item.getName(),
                item.getBirthday(), item.getId()) > 0) {
            return item;
        } else {
            return ERROR_ENTITY_USER;
        }
    }

    public User add(User user) {
        String sql = "insert into users (email, login, name, birthday) values (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        if (jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"id"});
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            stmt.setString(4, String.valueOf(user.getBirthday()));
            return stmt;
        }, keyHolder) > 0) {
            Integer userId = Objects.requireNonNull(keyHolder.getKey()).intValue();
            user.setId(userId);
            return user;
        } else {
            return ERROR_ENTITY_USER;
        }
    }

    public Optional<User> getById(Integer id) {
        return Optional.ofNullable(jdbcTemplate.queryForObject("select * from users where id = ?",
                this::mapRowToUser, id));
    }

    public boolean addFriend(Integer user, Integer friend) {
        String sqlGetFriends = "SELECT * FROM USER_FRIENDS WHERE user_id = ? AND friend_id = ?";
        SqlRowSet user1Friends = jdbcTemplate.queryForRowSet(sqlGetFriends, user, friend);
        SqlRowSet user2Friends = jdbcTemplate.queryForRowSet(sqlGetFriends, friend, user);
        boolean friendship1 = false;
        boolean friendship2 = false;
        if (!user1Friends.next()) {
            friendship1 = jdbcTemplate.update("INSERT INTO USER_FRIENDS (user_id, friend_id) VALUES (?, ?)",
                    user, friend) > 0;
            log.info("Пользователь id = {} добавил в друзья пользователя id = {}", user, friend);
        }
        if (user2Friends.next()) {
            friendship2 = jdbcTemplate.update("UPDATE USER_FRIENDS SET status = ? WHERE user_id IN (?, ?) AND friend_id IN (?, ?)",
                    true, user, friend, user, friend) > 0;
            log.info("Дружба у пользователей id = {} и id = {} подтверждена", user, friend);
        }
        return (friendship1 || friendship2);
    }

    public boolean deleteFriend(Integer user, Integer friend) {
        boolean bol = jdbcTemplate.update("DELETE FROM USER_FRIENDS WHERE user_id = ? AND friend_id = ?",
                        user, friend) > 0;
        if (bol) {
            jdbcTemplate.update("UPDATE USER_FRIENDS SET status = ? WHERE user_id = ? AND friend_id = ?",
                    false, friend, user);
        }
        return (bol);
    }

    public boolean delete(Integer id) {
        deleteFriendsLinks(id);
        deleteLikesLinks(id);
        String sqlQuery = "DELETE FROM users WHERE ID= ?";
        return jdbcTemplate.update(sqlQuery, id) > 0;
    }

    public void deleteFriendsLinks(Integer userId) {
        jdbcTemplate.update("DELETE FROM USER_FRIENDS WHERE user_id = ? OR friend_id = ?", userId, userId);
    }

    public void deleteLikesLinks(Integer userId) {
        jdbcTemplate.update("DELETE FROM FILM_LIKES WHERE user_id = ?", userId);
    }

    public List<User> getFriends(Integer userId) {
        return jdbcTemplate.query(String.format("SELECT * FROM USER_FRIENDS AS ur " +
                "LEFT JOIN users AS u ON ur.friend_id = u.id WHERE ur.user_id = %s", userId), this::mapRowToUser);
    }

    public List<UserFeed> getFeedByUserId(Integer userId) {
        return jdbcTemplate.query(String.format("SELECT * FROM USER_EVENT_FEED WHERE USER_ID = %s", userId),
                this::mapRowToUserFeed);
    }

    public List<Integer> getUserLikes(Integer id) {
        SqlRowSet userLike = jdbcTemplate.queryForRowSet("select film_id from film_likes where user_id=?", id);
        List<Integer> currentFilmIdsUser = new ArrayList<>();
        while (userLike.next()) {
            currentFilmIdsUser.add(userLike.getInt("film_id"));
        }
        return currentFilmIdsUser;
    }

    public List<Integer> getListUsersWithCommonLikes(int userId) {
        List<Integer> userIds = new ArrayList<>();
        String sql = "select user_id from film_likes where film_id IN " +
                "\n(select film_id from film_likes where user_id=?)";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql, userId);
        while (rs.next()) {
            if (userId != rs.getInt("user_id")) {
                userIds.add(rs.getInt("user_id"));
            }
        }
        return userIds;
    }

    private UserFeed mapRowToUserFeed(ResultSet resultSet, int rowNum) throws SQLException {
        return UserFeed.builder()
                .userId(resultSet.getInt("user_id"))
                .eventId(resultSet.getInt("event_id"))
                .entityId(resultSet.getInt("entity_id"))
                .operation(resultSet.getString("operation"))
                .eventType(resultSet.getString("event_type"))
                .timestamp(resultSet.getLong("time_stamp"))
                .build();
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

    public boolean isExistById(Integer id) {
        String sqlQuery = "SELECT EXISTS(SELECT 1 FROM USERS WHERE ID = ?)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sqlQuery, Boolean.class, id));
    }
}
