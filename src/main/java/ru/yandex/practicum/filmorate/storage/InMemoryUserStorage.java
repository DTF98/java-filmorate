package ru.yandex.practicum.filmorate.storage;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
@NoArgsConstructor
public class InMemoryUserStorage implements UserStorage {
    private final HashMap<Integer, User> users = new HashMap<>();

    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    public void setUser(User user) {
        users.put(user.getId(), user);
    }

    public User getUserById(Integer id) {
        return users.get(id);
    }

    public boolean containsUser(int id) {
        return users.containsKey(id);
    }
}
