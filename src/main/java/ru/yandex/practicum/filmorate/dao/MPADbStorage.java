package ru.yandex.practicum.filmorate.dao;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.impl.MPAStorage;
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

    public MPA set(MPA mpa) {
        String sql = "insert into mpa (name) values (?)";
        jdbcTemplate.update(sql, mpa.getName());
        return mpa;
    }

    public MPA getById(Integer id) {
        return jdbcTemplate.query(String.format("select * from mpa where mpa_id = %s", id),
                this::mapRowToMPA).stream().findAny().orElse(null);
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

    public boolean contains(Integer id) {
        Optional<MPA> mpa = Optional.ofNullable(jdbcTemplate.queryForObject(
                String.format("select * from mpa where id = %s", id),
                this::mapRowToMPA));
        return mpa.isPresent();
    }

    private MPA mapRowToMPA(ResultSet resultSet, int rowNum) throws SQLException {
        return MPA.builder()
                .id(resultSet.getInt("mpa_id"))
                .name(resultSet.getString("mpa"))
                .build();
    }
}
