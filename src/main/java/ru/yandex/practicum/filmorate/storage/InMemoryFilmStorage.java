package ru.yandex.practicum.filmorate.storage;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
@NoArgsConstructor
public class InMemoryFilmStorage implements FilmStorage {
    private final HashMap<Integer, Film> films = new HashMap<>();

    public List<Film> getFilms() {
        return new ArrayList<>(films.values());
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
