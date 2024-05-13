package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.ReviewStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewDbStorageImpl implements ReviewStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Review add(Review review) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("review")
                .usingGeneratedKeyColumns("id");
        int reviewId = simpleJdbcInsert.executeAndReturnKey(toReviewMap(review)).intValue();
        review.setId(reviewId);
        return review;
    }

    @Override
    public Review update(Review review) {
        String sqlQuery = "update REVIEW set " +
                " CONTENT = ?, IS_POSITIVE = ?" +
                " where ID = ?";
        jdbcTemplate.update(sqlQuery, review.getContent(), review.getIsPositive(), review.getId());
        log.info("Отзыв по id = {} успешно обновлен", review.getId());
        return getById(review.getId()).orElseThrow(() ->
                new NotFoundException(String.format("Не найден отзыв по id = %s", review.getId())));
    }

    @Override
    public void delete(int id) {
        String sqlQuery = "delete from REVIEW where ID = ?;";
        jdbcTemplate.update(sqlQuery, id);
        log.info("Отзыв успешно удален");
    }

    @Override
    public void increaseUseful(int id) {
        String sqlQuery = "update REVIEW set " +
                " USEFUL = (USEFUL + 1)" +
                " where ID = ?";
        jdbcTemplate.update(sqlQuery, id);
    }

    @Override
    public void decreaseUseful(int id) {
        String sqlQuery = "update REVIEW set " +
                " USEFUL = (USEFUL - 1)" +
                " where ID = ?";

        jdbcTemplate.update(sqlQuery, id);
    }

    @Override
    public Collection<Review> getAll() {
        String sql = "SELECT id, film_id, user_id, content, useful, is_positive" +
                " FROM REVIEW " +
                " ORDER BY USEFUL DESC";
        return jdbcTemplate.query(sql, this::mapRow);
    }

    @Override
    public Optional<Review> getById(int id) {
        String sqlQuery = "SELECT id, film_id, user_id, content, useful, is_positive " +
                " FROM REVIEW WHERE id = ?";
        return Optional.ofNullable(jdbcTemplate.queryForObject(sqlQuery, this::mapRow, id));
    }

    @Override
    public Collection<Review> getByFilmId(int filmId) {
        String sql = "SELECT id, film_id, user_id, content, useful, is_positive" +
                " FROM REVIEW" +
                " WHERE FILM_ID = ?" +
                " ORDER BY USEFUL DESC";
        return jdbcTemplate.query(sql, this::mapRow, filmId);
    }

    private Review mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        return Review.builder()
                .id(resultSet.getInt("id"))
                .filmId(resultSet.getInt("film_id"))
                .userId(resultSet.getInt("user_id"))
                .content(resultSet.getString("content"))
                .useful(resultSet.getInt("useful"))
                .isPositive(resultSet.getBoolean("is_positive"))
                .build();
    }

    public Map<String, Object> toReviewMap(Review review) {
        Map<String, Object> values;
        try {
            values = new HashMap<>();
            values.put("film_id", review.getFilmId());
            values.put("user_id", review.getUserId());
            values.put("content", review.getContent());
            values.put("useful", review.getUseful());
            values.put("is_positive", review.getIsPositive());
        } catch (Exception e) {
            log.error("Error in toReviewMap ", e);
            return new HashMap<>();
        }
        return values;
    }
}
