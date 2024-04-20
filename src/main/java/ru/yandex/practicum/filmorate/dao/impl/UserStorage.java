package ru.yandex.practicum.filmorate.dao.impl;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage extends Storage<User>{
    public void addFriend(Integer user, Integer friend);

    public void removeFriend(Integer user, Integer friend);

    public List<User> getFriends(Integer userId);
}
