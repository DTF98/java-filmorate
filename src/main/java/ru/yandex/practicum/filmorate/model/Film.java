package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.NonFinal;
import ru.yandex.practicum.filmorate.validation.ValidateDate;
import ru.yandex.practicum.filmorate.validation.ValidateGenre;
import ru.yandex.practicum.filmorate.validation.ValidateMPA;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.*;

@Builder
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
    @ValidateDate(message = "28.12.1895")
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
    @NotNull
    @NonFinal
    private Set<Director> directors;

    @NotNull
    @NonFinal
    private Set<Director> directors;

    public Film(Integer id, String name, String description, LocalDate releaseDate, Integer duration, Set<Genre> genres,
                MPA mpa, Set<Director> directors) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.genres = Objects.requireNonNullElseGet(genres, HashSet::new);
        this.mpa = mpa;
        this.directors = Objects.requireNonNullElseGet(directors, HashSet::new);
    }
}
