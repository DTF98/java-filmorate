package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.ReviewLikeStorage;
import ru.yandex.practicum.filmorate.model.ReviewLike;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewLikeDbStorageImpl implements ReviewLikeStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Integer addLike(Integer reviewId, Integer userId) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("review_like")
                .usingGeneratedKeyColumns("id")
                .usingColumns("review_id", "user_id", "like_type");

        Integer reviewLikeId = simpleJdbcInsert.executeAndReturnKey(
                toReviewLikeMap(new ReviewLike(reviewId, userId, true))).intValue();
        log.info("Dislike отзыву успешно поставлен");
        return reviewLikeId;
    }

    @Override
    public Integer addDislike(Integer reviewId, Integer userId) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("review_like")
                .usingGeneratedKeyColumns("id")
                .usingColumns("review_id", "user_id", "like_type");

        Integer reviewDislikeId = simpleJdbcInsert.executeAndReturnKey(
                toReviewLikeMap(new ReviewLike(reviewId, userId, true))).intValue();
        log.info("Like отзыву успешно поставлен");
        return reviewDislikeId;
    }

    /**
     * Delete all review likes and dislikes
     */
    @Override
    public boolean deleteLikes(Integer reviewId) {
        String sqlQuery = "DELETE FROM REVIEW_LIKE" +
                " WHERE REVIEW_ID = ?;";
        return jdbcTemplate.update(sqlQuery, reviewId) > 0;
    }

    /**
     * Delete review like from user
     */
    @Override
    public boolean deleteLike(Integer reviewId, int userId) {
        String sqlQuery = "DELETE FROM REVIEW_LIKE" +
                " WHERE REVIEW_ID = ? " +
                " AND USER_ID = ? " +
                " AND LIKE_TYPE = TRUE;";
        return jdbcTemplate.update(sqlQuery, reviewId, userId) > 0;
    }

    /**
     * Delete review dislike from user
     */
    @Override
    public boolean deleteDislike(Integer reviewId, int userId) {
        String sqlQuery = "DELETE FROM REVIEW_LIKE" +
                " WHERE REVIEW_ID = ? " +
                " AND USER_ID = ? " +
                " AND LIKE_TYPE = FALSE;";
        return jdbcTemplate.update(sqlQuery, reviewId, userId) > 0;
    }

    public Map<String, Object> toReviewLikeMap(ReviewLike reviewLike) {
        Map<String, Object> values = new HashMap<>();
        values.put("review_id", reviewLike.getReviewId());
        values.put("user_id", reviewLike.getUserId());
        values.put("type", reviewLike.getType());

        return values;
    }

}
