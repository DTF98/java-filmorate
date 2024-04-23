package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.impl.GenreStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class GenreService {
    private final GenreStorage genreStorage;

    public List<Genre> get() {
        log.info("Получен список всех жанров");
        return genreStorage.get();
    }

    public Genre getByID(Integer id) {
        Genre genre = genreStorage.getById(id);
        if (genre == null) {
            throw new NotFoundException(String.format("Жанр по id = %s не найден", id));
        } else {
            log.info("Получен жанр по id = {}", id);
            return genre;
        }
    }
}