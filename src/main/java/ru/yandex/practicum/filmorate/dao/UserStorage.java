package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.UserFeed;

import java.util.List;

public interface UserStorage extends Storage<User> {
    boolean addFriend(Integer user, Integer friend);

    boolean deleteFriend(Integer user, Integer friend);

    List<User> getFriends(Integer userId);

    boolean isExistById(Integer id);

    List<Integer> getUserLikes(Integer id);

    List<Integer> getListUsersWithCommonLikes(int userId);
}
