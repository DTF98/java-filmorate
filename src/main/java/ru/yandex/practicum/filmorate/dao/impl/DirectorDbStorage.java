package ru.yandex.practicum.filmorate.dao.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.DirectorStorage;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static ru.yandex.practicum.filmorate.constant.ConstantError.ERROR_ENTITY_DIRECTOR;

@Component
@Slf4j
@AllArgsConstructor
public class DirectorDbStorage implements DirectorStorage {
    private final JdbcTemplate jdbcTemplate;

    public List<Director> get() {
        String sqlGet = "select * from directors";
        return jdbcTemplate.query(sqlGet, this::mapRowToDirector);
    }

    public Director add(Director director) {
        String sqlDirector = "insert into directors (director_name) values (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        if (jdbcTemplate.update(connection -> {
                PreparedStatement stmt = connection.prepareStatement(sqlDirector, new String[]{"director_id"});
                stmt.setString(1, director.getName());
                return stmt;
            }, keyHolder) > 0) {
            Integer directorID = Objects.requireNonNull(keyHolder.getKey()).intValue();
            director.setId(directorID);
            log.info("Добавлен режисер id = {}", director.getId());
            return director;
        } else {
            return ERROR_ENTITY_DIRECTOR;
        }
    }

    public Director update(Director director) {
        String sqlDirector = "UPDATE directors SET director_name = ? WHERE director_id = ?;";
        if (jdbcTemplate.update(sqlDirector, director.getName(), director.getId()) > 0) {
            log.info("Обновлен режисер id = {}", director.getId());
            return director;
        } else {
            return ERROR_ENTITY_DIRECTOR;
        }
    }

    public Optional<Director> getById(Integer id) {
        String sqlDirector = "select * from directors where director_id = ?";
        return Optional.ofNullable(jdbcTemplate.queryForObject(sqlDirector,
                this::mapRowToDirector, id));
    }

    public boolean delete(Integer id) {
        String sqlDirector = "DELETE FROM directors WHERE DIRECTOR_ID= ?";
        return jdbcTemplate.update(sqlDirector, id) > 0;
    }

    private Director mapRowToDirector(ResultSet resultSet, int rowNum) throws SQLException {
        return Director.builder()
                .id(resultSet.getInt("director_id"))
                .name(resultSet.getString("director_name"))
                .build();
    }

    public boolean isExistDirectorById(Integer id) {
        String sqlQuery = "SELECT EXISTS(SELECT 1 FROM DIRECTORS WHERE DIRECTOR_ID = ?)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sqlQuery, Boolean.class, id));
    }
}
