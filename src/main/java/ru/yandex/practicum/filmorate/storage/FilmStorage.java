package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FilmStorage {
    private final HashMap<Integer, Film> films = new HashMap<>();

    public FilmStorage() {

    }

    public List<Film> getFilms() {
        List<Film> films1 = new ArrayList<>();
        films.forEach((k,v) -> films1.add(v));
        return films1;
    }

    public void setFilm(Film film) {
        films.put(film.getId(), film);
    }

    public Film getFilmById(int id) {
        return films.get(id);
    }

    public boolean containsFilm(int id) {
        return films.containsKey(id);
    }
}
