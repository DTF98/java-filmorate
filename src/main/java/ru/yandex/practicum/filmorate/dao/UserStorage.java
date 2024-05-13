package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage extends Storage<User> {
    void addFriend(Integer user, Integer friend);

    void deleteFriend(Integer user, Integer friend);

    List<User> getFriends(Integer userId);

    List<Integer> getUserLikes(Integer id);

    List<Integer> getListUsersWithCommonLikes(int userId);
}
