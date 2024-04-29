package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.GenreStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

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
        Optional<Genre> genre = genreStorage.getById(id);
        if (genre.isPresent()) {
            log.info("Получен жанр по id = {}", id);
            return genre.get();
        } else {
            throw new NotFoundException(String.format("Жанр по id = %s не найден", id));
        }
    }
}