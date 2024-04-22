package ru.yandex.practicum.filmorate.model;

import lombok.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Getter
@Setter
@AllArgsConstructor
@Builder
@ToString
public class Genre {
    @Min(value = 1, message = "Номер жанра не должен быть меньше 1")
    @Max(value = 6, message = "Номер жанра не должен быть больше 6")
    private Integer id;
    private String name;
}
