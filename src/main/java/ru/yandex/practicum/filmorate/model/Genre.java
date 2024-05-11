package ru.yandex.practicum.filmorate.model;

import lombok.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Getter
@Setter
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class Genre {
    private Integer id;
    private String name;
}
