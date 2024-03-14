package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {

    public List<User> getUsers();

    public void setUser(User user);

    public User getUserById(Integer id);

    public boolean containsUser(int id);
    }
