package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.UserFeedStorage;
import ru.yandex.practicum.filmorate.model.UserFeed;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserFeedService {
    private final UserFeedStorage userFeedStorage;

    public void addInHistoryFeed(Integer userId, String eventType, String operation, Integer entityId) {
        userFeedStorage.addInHistory(userId, eventType, operation, entityId);
    }

    public List<UserFeed> getFeedByUserId(Integer userId) {
        return userFeedStorage.getFeedByUserId(userId);
    }
}
