package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.ReviewStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
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
            int reviewId = simpleJdbcInsert.executeAndReturnKey(toReviewMap(review)).intValue();
            review.setId(reviewId);
            log.info("Отзыв по id = {} успешно добавлен", review.getId());

            String sql = "INSERT INTO USER_EVENT_FEED (user_id, event_type, operation, entity_id, time_stamp) VALUES (?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql, review.getUserId(), "REVIEW", "ADD", review.getId(), Instant.now().toEpochMilli());
            log.info("Добавлено в историю создание отзыва пользователем id = {} фильму id = {}", review.getId(), review.getFilmId());

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

            if (jdbcTemplate.update(sqlQuery, review.getContent(), review.getIsPositive(), review.getId()) > 0) {
                log.info("Отзыв по id = {} успешно обновлен", review.getId());
                Review updateReview = getById(review.getId()).get();
                String sql = "INSERT INTO USER_EVENT_FEED (user_id, event_type, operation, entity_id, time_stamp) VALUES (?, ?, ?, ?, ?)";
                jdbcTemplate.update(sql, updateReview.getUserId(), "REVIEW", "UPDATE", updateReview.getId(), Instant.now().toEpochMilli());
                log.info("Добавлено в историю обновление отзыва пользователем id = {} фильму id = {}", review.getId(), review.getFilmId());
            }

        } catch (DataAccessException e) {
            log.error("Error in update review", e);
        }

        return review;
    }

    @Override
    public int delete(int id) {
        try {
            Optional<Review> delete = getById(id);
            if (delete.isPresent()) {
                String sqlQuery = "delete from REVIEW where ID = ?;";
                jdbcTemplate.update(sqlQuery, id);
                log.info("Отзыв успешно удален");

                String sql = "INSERT INTO USER_EVENT_FEED (user_id, event_type, operation, entity_id, time_stamp) VALUES (?, ?, ?, ?, ?)";
                jdbcTemplate.update(sql, delete.get().getUserId(), "REVIEW", "REMOVE", delete.get().getId(), Instant.now().toEpochMilli());
                log.info("Добавлено в историю удаление отзыва пользователем id = {} фильму id = {}", delete.get().getId(), delete.get().getFilmId());
            } else {
                throw new NotFoundException("Отзыв не найден!");
            }
        } catch (DataAccessException e) {
            log.error("Error in delete review", e);
        }
        return id;
    }

    @Override
    public void increaseUseful(int id) {
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
    public void decreaseUseful(int id) {
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
    public Optional<Review> getById(int id) {
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
    public Collection<Review> getByFilmId(int filmId) {
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
                    .id(resultSet.getInt("id"))
                    .filmId(resultSet.getInt("film_id"))
                    .userId(resultSet.getInt("user_id"))
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
