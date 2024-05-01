package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage extends Storage<User> {
    User addFriend(Integer user, Integer friend);

    Integer deleteFriend(Integer user, Integer friend);

    List<User> getFriends(Integer userId);
}
