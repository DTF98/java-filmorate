package ru.yandex.practicum.filmorate.model;

import lombok.*;
import lombok.extern.jackson.Jacksonized;
import ru.yandex.practicum.filmorate.validation.ValidateDate;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class Film {
    private Set<Integer> likes;
    @EqualsAndHashCode.Include
    private Integer id;
    @NotBlank
    private String name;
    @Size(max = 200, message = "максимальная длина описания — 200 символов")
    private String description;
    @NotNull
    @ValidateDate(message = "Дата релиза должна быть не раньше 28 декабря 1895 года")
    private LocalDate releaseDate;
    @NotNull
    @Positive
    private Integer duration;

    @Builder
    @Jacksonized
    public Film(Integer id, String name, String description, LocalDate releaseDate, Integer duration,
                 Set<Integer> likes) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.likes = likes == null ? new HashSet<>() : likes;
    }

    public void removeLike(Integer userID) {
        likes.remove(userID);
    }

    public void setLike(Integer id) {
        likes.add(id);
    }
}
