package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    private  final FilmStorage inMemoryFilmStorage;

    @Autowired
    public FilmService(FilmStorage inMemoryFilmStorage) {
        this.inMemoryFilmStorage = inMemoryFilmStorage;
    }

    public void addLike(Integer userID, Integer filmID) {
        if (inMemoryFilmStorage.containsFilm(filmID)) {
            inMemoryFilmStorage.getFilmById(filmID).setLike(userID);
            log.info(String.format("Добавлен лайк фильму: %s пользователем id = %s",
                    inMemoryFilmStorage.getFilmById(filmID), userID));
        } else {
            throw new NotFoundException(String.format("Фильм по id = %s не найден!", filmID));
        }
    }

    public void removeLike(Integer userID, Integer filmID) {
        if(inMemoryFilmStorage.containsFilm(filmID)){
            if (inMemoryFilmStorage.getFilmById(filmID).getLikes().contains(userID)) {
                log.info(String.format("Удалён лайк пользователя id = %s у фильма id = %s", userID, filmID));
                inMemoryFilmStorage.getFilmById(filmID).removeLike(userID);
            }
        } else {
            throw new ValidationException("Не найден фильм!");
        }
    }

    public List<Film> search10MostPopularFilms(Integer count)
            throws ValidationException {
        if (count > 0) {
            log.info("Получен список популярных фильмов!");
            return inMemoryFilmStorage.getFilms().stream()
                    .sorted(new FilmComparator())
                    .limit(count)
                    .collect(Collectors.toList());
        } else {
            throw new ValidationException("Передан параметр в count <= 0 ");
        }
    }

    public List<Film> getFilms() {
        return new ArrayList<>(inMemoryFilmStorage.getFilms());
    }

    public void setFilm(Film film) {
        inMemoryFilmStorage.setFilm(film);
        log.info(String.format("Добавлен фильм: {%s}", inMemoryFilmStorage.getFilmById(film.getId())));
    }

    public void updateFilm(Film film) {
        if(inMemoryFilmStorage.containsFilm(film.getId())) {
            inMemoryFilmStorage.setFilm(film);
            log.info("Обновлен фильм: {}", inMemoryFilmStorage.getFilmById(film.getId()));
        } else {
            throw new NotFoundException(String.format("Фильм не найден!"));
        }
    }

    public Film getFilmById(Integer id) {
        if(inMemoryFilmStorage.containsFilm(id)) {
            log.info(String.format("Получен фильм по id = %s : %s", id, inMemoryFilmStorage.getFilmById(id)));
            return inMemoryFilmStorage.getFilmById(id);
        } else {
            throw new NotFoundException(String.format("Фильм по id = %s не найден!", id));
        }
    }
}

class FilmComparator implements Comparator<Film> {

    public int compare(Film a, Film b){
        return -(Integer.compare(a.getLikes().size(), b.getLikes().size()));
    }
}