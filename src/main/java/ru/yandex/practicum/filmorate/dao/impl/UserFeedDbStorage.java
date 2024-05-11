package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.UserFeedStorage;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserFeedDbStorage implements UserFeedStorage {
    private final JdbcTemplate jdbcTemplate;

    public boolean addInHistory(Integer userId, String eventType, String operation, Integer entityId) {
        String sql = "INSERT INTO USER_EVENT_FEED (user_id, event_type, operation, entity_id, time_stamp) " +
                "VALUES (?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql, userId, eventType, operation, entityId,
                Instant.now().toEpochMilli()) > 0;
    }
}
