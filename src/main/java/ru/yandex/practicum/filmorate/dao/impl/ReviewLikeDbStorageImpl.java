package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.ReviewLikeStorage;
import ru.yandex.practicum.filmorate.model.ReviewLike;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewLikeDbStorageImpl implements ReviewLikeStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Long addLike(Long reviewId, Long userId) {
        Long reviewLikeId = null;
        try {

            SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                    .withTableName("review_like")
                    .usingGeneratedKeyColumns("id")
                    .usingColumns("review_id", "user_id", "like_type");

            reviewLikeId = simpleJdbcInsert.executeAndReturnKey(
                    toReviewLikeMap(new ReviewLike(reviewId, userId, true))).longValue();

        } catch (Exception e) {
            log.error("Error in addLike", e);
        }

        return reviewLikeId;
    }

    @Override
    public Long addDislike(Long reviewId, Long userId) {
        Long reviewDislikeId = null;
        try {

            SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                    .withTableName("review_like")
                    .usingGeneratedKeyColumns("id")
                    .usingColumns("review_id", "user_id", "like_type");

            reviewDislikeId = simpleJdbcInsert.executeAndReturnKey(
                    toReviewLikeMap(new ReviewLike(reviewId, userId, true))).longValue();

        } catch (Exception e) {
            log.error("Error in addDislike", e);
        }

        return reviewDislikeId;
    }

    /**
     * Delete all review likes and dislikes
     */
    @Override
    public void deleteLikes(Long reviewId) {
        try {
            String sqlQuery = "DELETE FROM REVIEW_LIKE" +
                    " WHERE REVIEW_ID = ?;";

            boolean isSuccess = jdbcTemplate.update(sqlQuery, reviewId) > 0;
        } catch (DataAccessException e) {
            log.error("Error in delete review like", e);
        }
    }

    /**
     * Delete review like from user
     */
    @Override
    public void deleteLike(Long reviewId, long userId) {
        try {
            String sqlQuery = "DELETE FROM REVIEW_LIKE" +
                    " WHERE REVIEW_ID = ? " +
                    " AND USER_ID = ? " +
                    " AND LIKE_TYPE = TRUE;";
            boolean isSuccess = jdbcTemplate.update(sqlQuery, reviewId, userId) > 0;
        } catch (DataAccessException e) {
            log.error("Error in delete review like", e);
        }
    }

    /**
     * Delete review dislike from user
     */
    @Override
    public void deleteDislike(Long reviewId, long userId) {
        try {
            String sqlQuery = "DELETE FROM REVIEW_LIKE" +
                    " WHERE REVIEW_ID = ? " +
                    " AND USER_ID = ? " +
                    " AND LIKE_TYPE = FALSE;";
            boolean isSuccess = jdbcTemplate.update(sqlQuery, reviewId, userId) > 0;
        } catch (DataAccessException e) {
            log.error("Error in delete review like", e);
        }
    }

    public Map<String, Object> toReviewLikeMap(ReviewLike reviewLike) {
        Map<String, Object> values = new HashMap<>();
        values.put("review_id", reviewLike.getReviewId());
        values.put("user_id", reviewLike.getUserId());
        values.put("type", reviewLike.getType());

        return values;
    }

}
