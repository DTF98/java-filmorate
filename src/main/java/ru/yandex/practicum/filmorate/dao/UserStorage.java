package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage extends Storage<User> {
    public User addFriend(Integer user, Integer friend);

    public Integer removeFriend(Integer user, Integer friend);

    public List<User> getFriends(Integer userId);
}
