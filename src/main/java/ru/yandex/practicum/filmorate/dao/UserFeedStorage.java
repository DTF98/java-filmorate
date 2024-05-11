package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.UserFeed;

import java.util.List;

public interface UserFeedStorage {
    boolean addInHistory(Integer userId, String eventType, String operation, Integer entityId);

    List<UserFeed> getFeedByUserId(Integer userId);
}
