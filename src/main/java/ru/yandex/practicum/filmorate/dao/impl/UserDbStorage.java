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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
        jdbcTemplate.update(sqlFilms, item.getEmail(), item.getLogin(), item.getName(),
                item.getBirthday(), item.getId());
        return item;
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
        return Optional.ofNullable(jdbcTemplate.queryForObject("select * from users where id = ?",
                this::mapRowToUser, id));
    }

    public void addFriend(Integer user, Integer friend) {
        String sqlGetFriends = "SELECT * FROM USER_FRIENDS WHERE user_id = ? AND friend_id = ?";
        SqlRowSet user1Friends = jdbcTemplate.queryForRowSet(sqlGetFriends, user, friend);
        SqlRowSet user2Friends = jdbcTemplate.queryForRowSet(sqlGetFriends, friend, user);
        if (!user1Friends.next()) {
            jdbcTemplate.update("INSERT INTO USER_FRIENDS (user_id, friend_id) VALUES (?, ?)",
                    user, friend);
            log.info("Пользователь id = {} добавил в друзья пользователя id = {}", user, friend);
        }
        if (user2Friends.next()) {
            jdbcTemplate.update("UPDATE USER_FRIENDS SET status = ? WHERE user_id IN (?, ?) AND friend_id IN (?, ?)",
                    true, user, friend, user, friend);
            log.info("Дружба у пользователей id = {} и id = {} подтверждена", user, friend);
        }
    }

    public void deleteFriend(Integer user, Integer friend) {
        boolean bol = jdbcTemplate.update("DELETE FROM USER_FRIENDS WHERE user_id = ? AND friend_id = ?",
                        user, friend) > 0;
        if (bol) {
            jdbcTemplate.update("UPDATE USER_FRIENDS SET status = ? WHERE user_id = ? AND friend_id = ?",
                    false, friend, user);
        }
    }

    public void delete(Integer id) {
        deleteFriendsLinks(id);
        deleteLikesLinks(id);
        String sqlQuery = "DELETE FROM users WHERE ID= ?";
        jdbcTemplate.update(sqlQuery, id);
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

    private User mapRowToUser(ResultSet resultSet, int rowNum) throws SQLException {
        return User.builder()
                .id(resultSet.getInt("id"))
                .email(resultSet.getString("email"))
                .login(resultSet.getString("login"))
                .name(resultSet.getString("name"))
                .birthday(LocalDate.parse(resultSet.getString("birthday")))
                .build();
    }
}
