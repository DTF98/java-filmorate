package ru.yandex.practicum.filmorate.dao.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.GenreStorage;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@Slf4j
@AllArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    public Genre add(Genre genre) {
        String sql = "insert into genres (name) values (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"genre_id"});
            stmt.setString(1, genre.getName());
            return stmt;
        }, keyHolder);
        Integer directorID = Objects.requireNonNull(keyHolder.getKey()).intValue();
        genre.setId(directorID);
        return genre;
    }

    public Optional<Genre> getById(Integer id) {
        return Optional.ofNullable(jdbcTemplate.queryForObject("select * from genres where genre_id = ?",
                this::mapRowToGenre, id));
    }

    public List<Genre> get() {
        return jdbcTemplate.query("select * from genres",
                this::mapRowToGenre);
    }

    public Genre update(Genre genre) {
        String sql = "UPDATE genres SET name = ? WHERE id = ?;";
        jdbcTemplate.update(sql, genre.getName(), genre.getId());
        log.info("Обновлен жанр id = {}", genre.getId());
        return genre;
    }

    public void delete(Integer id) {
        String sql = "DELETE FROM genres WHERE genre_id = ?;";
        jdbcTemplate.update(sql, id);
    }

    private Genre mapRowToGenre(ResultSet resultSet, int rowNum) throws SQLException {
        return Genre.builder()
                .id(resultSet.getInt("genre_id"))
                .name(resultSet.getString("genre"))
                .build();
    }

    public boolean isExistGenreById(Integer id) {
        String sqlQuery = "SELECT EXISTS(SELECT 1 FROM GENRES WHERE GENRE_ID = ?)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sqlQuery, Boolean.class, id));
    }
}
