package ru.yandex.practicum.filmorate.dao;

public interface UserFeedStorage {
    boolean addInHistory(Integer userId, String eventType, String operation, Integer entityId);
}
