package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.UserFeed;

import java.util.List;
import java.util.Optional;

public interface UserStorage extends Storage<User> {
    User addFriend(Integer user, Integer friend);

    Integer deleteFriend(Integer user, Integer friend);

    List<User> getFriends(Integer userId);

    List<UserFeed> getFeedByUserId(Integer userId);

    List<Optional<Film>> getRecommendations(int id);
}
