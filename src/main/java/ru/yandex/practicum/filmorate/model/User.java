package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    private Set<Integer> friends = new HashSet<>();
    private Integer id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;

    public void removeFriend(Integer id) {
            friends.remove(id);
    }

    public void setFriend(Integer id) {
        friends.add(id);

    }
}
