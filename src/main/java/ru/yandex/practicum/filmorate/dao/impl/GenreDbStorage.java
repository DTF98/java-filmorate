package ru.yandex.practicum.filmorate.dao.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.GenreStorage;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@AllArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    public Genre add(Genre item) {
        String sql = "insert into genres (name) values (?)";
        jdbcTemplate.update(sql, item.getName());
        return item;
    }

    public Optional<Genre> getById(Integer id) {
        return jdbcTemplate.query(String.format("select * from genres where genre_id = %s", id),
                this::mapRowToGenre).stream().findAny();
    }

    public List<Genre> get() {
        return jdbcTemplate.query("select * from genres",
                this::mapRowToGenre);
    }

    public Genre update(Genre item) {
        String sql = "UPDATE genres SET name = ? WHERE id = ?;";
        jdbcTemplate.update(sql, item.getName(), item.getId());
        return item;
    }

    private Genre mapRowToGenre(ResultSet resultSet, int rowNum) throws SQLException {
        return Genre.builder()
                .id(resultSet.getInt("genre_id"))
                .name(resultSet.getString("genre"))
                .build();
    }
}
