package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.ReviewLikeStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
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
        try {

            SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                    .withTableName("review_like")
                    .usingGeneratedKeyColumns("id")
                    .usingColumns("review_id", "user_id", "like_type");

            Integer reviewLikeId = simpleJdbcInsert.executeAndReturnKey(
                    toReviewLikeMap(new ReviewLike(reviewId, userId, true))).intValue();
            log.info("Dislike отзыву успешно поставлен");
            return reviewLikeId;

        } catch (Exception e) {
            log.error("Error in addLike", e);
        }
        return null;
    }

    @Override
    public Integer addDislike(Integer reviewId, Integer userId) {
        try {

            SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                    .withTableName("review_like")
                    .usingGeneratedKeyColumns("id")
                    .usingColumns("review_id", "user_id", "like_type");

            Integer reviewDislikeId = simpleJdbcInsert.executeAndReturnKey(
                    toReviewLikeMap(new ReviewLike(reviewId, userId, true))).intValue();
            log.info("Like отзыву успешно поставлен");
            return reviewDislikeId;

        } catch (Exception e) {
            log.error("Error in addDislike", e);
        }
        return null;
    }

    /**
     * Delete all review likes and dislikes
     */
    @Override
    public void deleteLikes(Integer reviewId) {
        try {
            String sqlQuery = "DELETE FROM REVIEW_LIKE" +
                    " WHERE REVIEW_ID = ?;";

            boolean isSuccess = jdbcTemplate.update(sqlQuery, reviewId) > 0;

            if (!isSuccess) {
                throw new NotFoundException("Лайки отзыва не удалены!");
            }
        } catch (DataAccessException e) {
            log.error("Error in delete review like", e);
        }
    }

    /**
     * Delete review like from user
     */
    @Override
    public boolean deleteLike(Integer reviewId, int userId) {
        try {
            String sqlQuery = "DELETE FROM REVIEW_LIKE" +
                    " WHERE REVIEW_ID = ? " +
                    " AND USER_ID = ? " +
                    " AND LIKE_TYPE = TRUE;";

            boolean isSuccess = jdbcTemplate.update(sqlQuery, reviewId, userId) > 0;

            if (!isSuccess) {
                throw new NotFoundException("Лайк отзыва не удален!");
            }
            log.info("Лайк к отзыву успешно удален");
            return true;
        } catch (DataAccessException e) {
            log.error("Error in delete review like", e);
        }
        return false;
    }

    /**
     * Delete review dislike from user
     */
    @Override
    public boolean deleteDislike(Integer reviewId, int userId) {
        try {
            String sqlQuery = "DELETE FROM REVIEW_LIKE" +
                    " WHERE REVIEW_ID = ? " +
                    " AND USER_ID = ? " +
                    " AND LIKE_TYPE = FALSE;";

            boolean isSuccess = jdbcTemplate.update(sqlQuery, reviewId, userId) > 0;

            if (!isSuccess) {
                throw new NotFoundException("Дизлайк отзыва не удален!");
            }
            log.info("Дизлайк к отзыву успешно удален");
            return true;
        } catch (DataAccessException e) {
            log.error("Error in delete review like", e);
        }
        return false;
    }

    public Map<String, Object> toReviewLikeMap(ReviewLike reviewLike) {
        Map<String, Object> values = new HashMap<>();
        values.put("review_id", reviewLike.getReviewId());
        values.put("user_id", reviewLike.getUserId());
        values.put("type", reviewLike.getType());

        return values;
    }

}
