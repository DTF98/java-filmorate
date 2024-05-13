package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.UserFeedStorage;
import ru.yandex.practicum.filmorate.model.UserFeed;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserFeedDbStorage implements UserFeedStorage {
    private final JdbcTemplate jdbcTemplate;

    public void addInHistory(Integer userId, String eventType, String operation, Integer entityId) {
        String sql = "INSERT INTO USER_EVENT_FEED (user_id, event_type, operation, entity_id, time_stamp) " +
                "VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, userId, eventType, operation, entityId,
                Instant.now().toEpochMilli());
    }

    public List<UserFeed> getFeedByUserId(Integer userId) {
        return jdbcTemplate.query(String.format("SELECT * FROM USER_EVENT_FEED WHERE USER_ID = %s", userId),
                this::mapRowToUserFeed);
    }

    private UserFeed mapRowToUserFeed(ResultSet resultSet, int rowNum) throws SQLException {
        return UserFeed.builder()
                .userId(resultSet.getInt("user_id"))
                .eventId(resultSet.getInt("event_id"))
                .entityId(resultSet.getInt("entity_id"))
                .operation(resultSet.getString("operation"))
                .eventType(resultSet.getString("event_type"))
                .timestamp(resultSet.getLong("time_stamp"))
                .build();
    }
}
