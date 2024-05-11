package ru.yandex.practicum.filmorate.dao.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.MPAStorage;
import ru.yandex.practicum.filmorate.model.MPA;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static ru.yandex.practicum.filmorate.constant.ConstantError.ERROR_ENTITY_MPA;

@Component
@Slf4j
@AllArgsConstructor
public class MPADbStorage implements MPAStorage {
    private final JdbcTemplate jdbcTemplate;

    public MPA add(MPA mpa) {
        String sql = "insert into mpa (name) values (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        if (jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"mpa_id"});
            stmt.setString(1, mpa.getName());
            return stmt;
        }, keyHolder) > 0) {
            Integer directorID = Objects.requireNonNull(keyHolder.getKey()).intValue();
            mpa.setId(directorID);
            return mpa;
        } else {
            return ERROR_ENTITY_MPA;
        }
    }

    public Optional<MPA> getById(Integer id) {
        return Optional.ofNullable(jdbcTemplate.queryForObject("select * from mpa where mpa_id = ?",
                this::mapRowToMPA, id));
    }

    public List<MPA> get() {
        return jdbcTemplate.query("select * from mpa",
                this::mapRowToMPA);
    }

    public MPA update(MPA mpa) {
        String sql = "UPDATE mpa SET name = ? WHERE id = ?;";
        if (jdbcTemplate.update(sql, mpa.getName(), mpa.getId()) > 0) {
            log.info("Обновили рейтинг по id = {}", mpa.getId());
            return mpa;
        } else {
            return ERROR_ENTITY_MPA;
        }
    }

    public boolean delete(Integer id) {
        String sql = "DELETE FROM mpa WHERE mpa_id = ?;";
        return jdbcTemplate.update(sql, id) > 0;
    }

    private MPA mapRowToMPA(ResultSet resultSet, int rowNum) throws SQLException {
        return MPA.builder()
                .id(resultSet.getInt("mpa_id"))
                .name(resultSet.getString("mpa"))
                .build();
    }

    public boolean isExistMpaById(Integer id) {
        String sqlQuery = "SELECT EXISTS(SELECT 1 FROM MPA WHERE MPA_ID = ?)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sqlQuery, Boolean.class, id));
    }
}
