package ru.yandex.practicum.filmorate.dao;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.impl.UserStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<User> get() {
        return jdbcTemplate.query("select * from users order by id", this::mapRowToUser);
    }

    public User update(User item) {
        if (contains(item.getId())) {
            String sqlFilms = "UPDATE users SET email = ?, login = ?, name = ?, " +
                    "birthday = ? WHERE id = ?;";
            jdbcTemplate.update(sqlFilms, item.getEmail(), item.getLogin(), item.getName(),
                    item.getBirthday(), item.getId());
            return item;
        } else {
            throw new NotFoundException(String.format("Пользователь не найден!"));
        }
    }

    public User set(User user) {
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

    public User getById(Integer id) {
        if (contains(id)) {
            return jdbcTemplate.query(String.format("select * from users where id = %s", id),
                    this::mapRowToUser).stream().findAny().orElse(null);
        } else {
            throw new NotFoundException(String.format("Пользователь по id = %s не найден!", id));
        }
    }

    public void addFriend(Integer user, Integer friend) {
        if (contains(user) && contains(friend)) {
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
        } else {
            throw new NotFoundException("Не найден пользователь!");
        }
    }

    public void removeFriend(Integer user, Integer friend) {
        if (contains(user) && contains(friend)) {
            int updated = jdbcTemplate.update("DELETE FROM USER_FRIENDS WHERE user_id = ? AND friend_id = ?",
                    user, friend);
            if (updated > 0) {
                jdbcTemplate.update("UPDATE USER_FRIENDS SET status = ? WHERE user_id = ? AND friend_id = ?",
                        false, friend, user);
            }
        } else {
            throw new NotFoundException("Не найден пользователь!");
        }
    }

    public List<User> getFriends(Integer userId) {
        if (contains(userId)) {
            return jdbcTemplate.query(String.format("SELECT * FROM USER_FRIENDS AS ur " +
                    "LEFT JOIN users AS u ON ur.friend_id = u.id WHERE ur.user_id = %s", userId), this::mapRowToUser);
        } else {
            throw new NotFoundException("Не найден пользователь!");
        }
    }

    public boolean contains(Integer id) {
        try {
            Optional<User> user = Optional.ofNullable(jdbcTemplate.queryForObject(
                String.format("select * from users where id = %s", id),
                this::mapRowToUser));
            return user.isPresent();
        } catch (EmptyResultDataAccessException e) {
            return false;
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
}
