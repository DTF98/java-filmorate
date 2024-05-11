package ru.yandex.practicum.filmorate.constant;

import ru.yandex.practicum.filmorate.model.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

public class ConstantError {
    public static final Director ERROR_ENTITY_DIRECTOR = new Director(0, "ErrorDirectorName");
    public static final MPA ERROR_ENTITY_MPA = new MPA(0, "ErrorMpaName");
    public static final Genre ERROR_ENTITY_GENRE = new Genre(0, "ErrorGenreName");
    public static final Film ERROR_ENTITY_FILM = new Film(0, "ErrorFilmName", "ErrorFilmDescription",
            LocalDate.now(), 0, new HashSet<>(List.of(ERROR_ENTITY_GENRE)), ERROR_ENTITY_MPA,
            new HashSet<>(List.of(ERROR_ENTITY_DIRECTOR)));
    public static final User ERROR_ENTITY_USER = new User(0, "Error@mail", "ErrorLogin",
            "ErrorName",LocalDate.now());
    public static final Review ERROR_ENTITY_REVIEW = new Review(0, 0, 0, "ErrorContent",0,
            false);
}
