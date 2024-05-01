package ru.yandex.practicum.filmorate.dao.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.MPAStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MPA;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@AllArgsConstructor
public class MPADbStorage implements MPAStorage {
    private final JdbcTemplate jdbcTemplate;

    public MPA add(MPA mpa) {
        String sql = "insert into mpa (name) values (?)";
        jdbcTemplate.update(sql, mpa.getName());
        return mpa;
    }

    public Optional<MPA> getById(Integer id) {
        return jdbcTemplate.query(String.format("select * from mpa where mpa_id = %s", id),
                this::mapRowToMPA).stream().findAny();
    }

    public List<MPA> get() {
        return jdbcTemplate.query("select * from mpa",
                this::mapRowToMPA);
    }

    public MPA update(MPA mpa) {
        String sql = "UPDATE mpa SET name = ? WHERE id = ?;";
        jdbcTemplate.update(sql, mpa.getName(), mpa.getId());
        return mpa;
    }

    public boolean delete(Integer id) {
        if (id > 0 && id < 6) {
            String sql = "DELETE FROM mpa WHERE mpa_id = ?;";
            return jdbcTemplate.update(sql, id) > 0;
        } else {
            throw new NotFoundException(String.format("Рейтинг не найден!", id));
        }
    }

    private MPA mapRowToMPA(ResultSet resultSet, int rowNum) throws SQLException {
        return MPA.builder()
                .id(resultSet.getInt("mpa_id"))
                .name(resultSet.getString("mpa"))
                .build();
    }
}
