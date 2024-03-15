package ru.yandex.practicum.filmorate.utils;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Comparator;

public class FilmComparator implements Comparator<Film> {

    public int compare(Film a, Film b) {
        return -(Integer.compare(a.getLikes().size(), b.getLikes().size()));
    }
}
