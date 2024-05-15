package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.GenreStorage;
import ru.yandex.practicum.filmorate.exception.CreateEntityException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class GenreService {
    private final GenreStorage genreStorage;

    public List<Genre> get() {
        log.info("Получение списка всех жанров");
        List<Genre> genres = genreStorage.get();
        log.info("Получен список всех жанров {}", genres);
        return genres;
    }

    public Genre getByID(Integer id) {
        Genre genre = genreStorage.getById(id).orElseThrow(() ->
                new NotFoundException(String.format("Жанр по id = %s не найден", id)));
        log.info("Получен жанр по id = {}", id);
        return genre;
    }

    public Genre add(Genre genre) {
        Genre added = genreStorage.update(genre);
        if (added.getId() == null) {
            throw new CreateEntityException(String.format("Жанр не добавлен! %s", genre));
        }
        log.info("Добавлен жанр {}", added);
        return added;
    }

    public Genre update(Genre genre) {
        getByID(genre.getId());
        Genre updated = genreStorage.update(genre);
        log.info("Обновлен жанр по id = {}", genre.getId());
        return updated;
    }

    public Genre delete(Integer id) {
        Genre deleted = getByID(id);
        genreStorage.delete(id);
        log.info("Жанр по id = {} удален", id);
        return deleted;
    }
}