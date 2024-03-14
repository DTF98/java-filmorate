package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {
    private Set<Integer> likes = new HashSet<>();
    private Integer id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private int duration;

    public void removeLike(Integer userID) {
        likes.remove(userID);
    }

    public void setLike(Integer id) {
        likes.add(id);
    }
}
