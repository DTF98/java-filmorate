package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UserStorage {
    private final HashMap<Integer, User> users = new HashMap<>();

    public UserStorage() {

    }

    public List<User> getUsers() {
        List<User> users1 = new ArrayList<>();
        users.forEach((k,v) -> users1.add(v));
        return users1;
    }

    public void setUser(User user) {
        users.put(user.getId(), user);
    }

    public User getUserById(int id) {
        return users.get(id);
    }

    public boolean containsUser(int id) {
        return users.containsKey(id);
    }
}
