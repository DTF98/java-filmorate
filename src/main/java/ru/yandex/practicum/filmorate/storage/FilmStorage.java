package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {

    public List<Film> getFilms();

    public void setFilm(Film film);

    public Film getFilmById(int id);

    public boolean containsFilm(int id);
}
