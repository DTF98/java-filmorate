package ru.yandex.practicum.filmorate.model;

import lombok.*;
import lombok.experimental.NonFinal;
import lombok.extern.jackson.Jacksonized;
import ru.yandex.practicum.filmorate.validation.ValidateDate;
import ru.yandex.practicum.filmorate.validation.ValidateGenre;
import ru.yandex.practicum.filmorate.validation.ValidateMPA;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@ToString
public class Film {
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
    @NotNull
    @NonFinal
    @ValidateGenre("Номер жанра не должен быть больше 6 и меньше 1")
    private Set<Genre> genres;
    @ValidateMPA("Номер рейтинга не должен быть больше 5 и меньше 1")
    private MPA mpa;

    @Builder
    @Jacksonized
    public Film(Integer id, String name, String description, LocalDate releaseDate, Integer duration, Set<Genre> genres,
                MPA mpa) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.genres = Objects.requireNonNullElseGet(genres, HashSet::new);
        this.mpa = mpa;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Film item = (Film) o;
        return Objects.equals(id, item.id) && Objects.equals(name, item.name) && Objects.equals(description, item.description) &&
                releaseDate.isEqual(item.releaseDate) && Objects.equals(duration, item.duration);
    }
}
