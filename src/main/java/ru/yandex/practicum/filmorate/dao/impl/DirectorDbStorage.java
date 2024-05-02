package ru.yandex.practicum.filmorate.dao.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.DirectorStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@Slf4j
@AllArgsConstructor
public class DirectorDbStorage implements DirectorStorage {
    private final JdbcTemplate jdbcTemplate;

    public List<Director> get() {
        try {
            String sqlGet = "select * from directors";
            return jdbcTemplate.query(sqlGet, this::mapRowToDirector);
        } catch (DataAccessException e) {
            log.error("Ошибка при получении списка всех режисеров", e);
            return null;
        }
    }

    public Director add(Director director) {
        String sqlDirector = "insert into directors (director_name) values (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlDirector, new String[]{"director_id"});
            stmt.setString(1, director.getName());
            return stmt;
        }, keyHolder);
        Integer directorID = Objects.requireNonNull(keyHolder.getKey()).intValue();
        director.setId(directorID);
        return director;
    }

    public Director update(Director director) {
        try {
            String sqlDirector = "UPDATE directors SET director_name = ? WHERE director_id = ?;";
            if (jdbcTemplate.update(sqlDirector, director.getName(), director.getId()) > 0) {
                log.info("Обновлен режисер id = {}", director.getId());
                return director;
            } else {
                throw new NotFoundException("Режисер не найден!");
            }
        } catch (DataAccessException e) {
            return null;
        }
    }

    public Optional<Director> getById(Integer id) {
        try {
            String sqlDirector = "select * from directors where director_id = ?";
            return Optional.ofNullable(jdbcTemplate.queryForObject(sqlDirector,
                    this::mapRowToDirector, id));
        } catch (DataAccessException e) {
            throw new NotFoundException(String.format("Режисер по id = %s не найден!", id));
        }
    }

    public boolean delete(Integer id) {
        deleteFILMDirectorsLinks(id);
        String sqlDirector = "DELETE FROM directors WHERE DIRECTOR_ID= ?";
        return jdbcTemplate.update(sqlDirector, id) > 0;
    }

    private void deleteFILMDirectorsLinks(Integer dirId) {
        String sqlQuery = "DELETE FROM FILM_DIRECTORS " +
                "WHERE DIRECTOR_ID = ?";
        jdbcTemplate.update(sqlQuery, dirId);
        log.info("Удалены ссылки на фильмы у режисера id = {}", dirId);
    }

    private Director mapRowToDirector(ResultSet resultSet, int rowNum) throws SQLException {
        return Director.builder()
                .id(resultSet.getInt("director_id"))
                .name(resultSet.getString("director_name"))
                .build();
    }
}
