package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.UserFeedStorage;
import ru.yandex.practicum.filmorate.exception.ApplicationException;
import ru.yandex.practicum.filmorate.model.UserFeed;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserFeedService {
    private final UserFeedStorage userFeedStorage;

    public boolean addInHistoryFeed(Integer userId, String eventType, String operation, Integer entityId) {
        try {
            return userFeedStorage.addInHistory(userId, eventType, operation, entityId);
        } catch (DataAccessException e) {
            log.error("Ошибка при добавлении в историю событий", e);
        }
        throw new ApplicationException("Ошибка при добавлении в историю событий");
    }

    public List<UserFeed> getFeedByUserId(Integer userId) {
        try {
            return userFeedStorage.getFeedByUserId(userId);
        } catch (DataAccessException e) {
            log.error("Ошибка при получении истории событий пользователя id = {}", userId, e);
        }
        throw new ApplicationException("Ошибка при получении истории событий пользователя");
    }
}
