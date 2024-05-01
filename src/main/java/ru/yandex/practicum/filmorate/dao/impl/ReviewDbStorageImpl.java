package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.ReviewStorage;

import ru.yandex.practicum.filmorate.model.Review;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewDbStorageImpl implements ReviewStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Review add(Review review) {
        try {
            SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                    .withTableName("review")
                    .usingGeneratedKeyColumns("id");
            long reviewId = simpleJdbcInsert.executeAndReturnKey(toReviewMap(review)).longValue();
            review.setId(reviewId);


        } catch (Exception e) {
            log.error("Error in add review", e);
        }

        return review;
    }

    @Override
    public Review update(Review review) {
        try {
            String sqlQuery = "update REVIEW set " +
                    " CONTENT = ?, IS_POSITIVE = ?" +
                    " where ID = ?";

            jdbcTemplate.update(sqlQuery,
                    review.getContent(),
                    review.getIsPositive(),
                    review.getId());
        } catch (DataAccessException e) {
            log.error("Error in update review", e);
        }

        return review;
    }

    @Override
    public long delete(long id) {
        try {
            String sqlQuery = "delete from REVIEW where ID = ?;";
            boolean isSuccess = jdbcTemplate.update(sqlQuery, id) > 0;
        } catch (DataAccessException e) {
            log.error("Error in delete review", e);
        }

        return id;
    }

    @Override
    public void increaseUseful(long id) {
        try {
            String sqlQuery = "update REVIEW set " +
                    " USEFUL = (USEFUL + 1)" +
                    " where ID = ?";

            jdbcTemplate.update(sqlQuery, id);
        } catch (DataAccessException e) {
            log.error("Error in increaseUseful", e);
        }

    }

    @Override
    public void decreaseUseful(long id) {
        try {
            String sqlQuery = "update REVIEW set " +
                    " USEFUL = (USEFUL - 1)" +
                    " where ID = ?";

            jdbcTemplate.update(sqlQuery, id);
        } catch (DataAccessException e) {
            log.error("Error in decreaseUseful", e);
        }

    }

    @Override
    public Collection<Review> getAll() {
        String sql = "SELECT id, film_id, user_id, content, useful, is_positive" +
                " FROM REVIEW " +
                " ORDER BY USEFUL DESC";
        Collection<Review> reviews = new ArrayList<>();

        try {
            reviews = jdbcTemplate.query(sql, this::mapRow);
        } catch (DataAccessException e) {
            log.error("Error in getAll", e);
        }

        return reviews;
    }

    @Override
    public Optional<Review> getById(long id) {
        try {
            String sqlQuery = "SELECT id, film_id, user_id, content, useful, is_positive " +
                    " FROM REVIEW WHERE id = ?";

            return Optional.ofNullable(jdbcTemplate.queryForObject(sqlQuery, this::mapRow, id));
        } catch (DataAccessException e) {
            log.error("Error in getById", e);
        }

        return Optional.empty();
    }

    @Override
    public Collection<Review> getByFilmId(long filmId) {
        String sql = "SELECT id, film_id, user_id, content, useful, is_positive" +
                " FROM REVIEW" +
                " WHERE FILM_ID = ?" +
                " ORDER BY USEFUL DESC";

        Collection<Review> reviews = new ArrayList<>();

        try {
            reviews = jdbcTemplate.query(sql, this::mapRow, filmId);
        } catch (DataAccessException e) {
            log.error("Error in getByFilmId", e);
        }

        return reviews;
    }

    private Review mapRow(ResultSet resultSet, int rowNum) {
        try {
            return Review.builder()
                    .id(resultSet.getLong("id"))
                    .filmId(resultSet.getLong("film_id"))
                    .userId(resultSet.getLong("user_id"))
                    .content(resultSet.getString("content"))
                    .useful(resultSet.getInt("useful"))
                    .isPositive(resultSet.getBoolean("is_positive"))
                    .build();
        } catch (SQLException e) {
            log.error("Error in mapRow ", e);
            return null;
        }
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
