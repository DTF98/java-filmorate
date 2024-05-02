package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage extends Storage<User> {
    public User addFriend(Integer user, Integer friend);

    public Integer removeFriend(Integer user, Integer friend);

    public List<User> getFriends(Integer userId);
    public List<Optional<Film>> getRecommendations(int id);
}
