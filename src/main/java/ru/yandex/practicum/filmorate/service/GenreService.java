package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.GenreStorage;
import ru.yandex.practicum.filmorate.exception.CreateEntityException;
import ru.yandex.practicum.filmorate.exception.ApplicationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class GenreService {
    private final GenreStorage genreStorage;

    public List<Genre> get() {
        try {
            log.info("Получение списка всех жанров");
            List<Genre> genres = genreStorage.get();
            log.info("Получен список всех жанров {}", genres);
            return genres;
        } catch (DataAccessException e) {
            log.error("Ошибка при получении списка всех жанров", e);
        }
        throw new ApplicationException("Ошибка при получении списка всех жанров");
    }

    public Genre getByID(Integer id) {
        try {
            Optional<Genre> genre = genreStorage.getById(id);
            if (genre.isPresent()) {
                log.info("Получен жанр по id = {}", id);
                return genre.get();
            }
        } catch (DataAccessException e) {
            log.error("Ошибка при получении жанра по id = {}", id, e);
            throw new NotFoundException(String.format("Жанр по id = %s не найден", id));
        }
        throw new ApplicationException(String.format("Ошибка при получении жанра по id = %s", id));
    }

    public Genre add(Genre genre) {
        try {
            Genre added = genreStorage.update(genre);
            if (added.getId() == 0) {
                throw new CreateEntityException(String.format("Жанр не добавлен! %s", genre));
            }
            return added;
        } catch (DataAccessException e) {
            log.error("Ошибка при добавлении жанра {}", genre, e);
        }
        throw new ApplicationException(String.format("Ошибка при добавлении жанра %s", genre));
    }

    public Genre update(Genre genre) {
        try {
            Genre updated = genreStorage.update(genre);
            if (updated.getId() == 0) {
                throw new NotFoundException("Жанр не найден!");
            }
            return updated;
        } catch (DataAccessException e) {
            log.error("Ошибка при обновлении жанра {}", genre, e);
        }
        throw new ApplicationException(String.format("Ошибка при обновлении жанра %s", genre));
    }

    public Boolean delete(Integer id) {
        try {
            if (genreStorage.isExistGenreById(id)) {
                if (genreStorage.delete(id)) {
                    log.info("Жанр по id = {} удален", id);
                    return true;
                }
            } else {
                throw new NotFoundException(String.format("Жанр по id = %s не найден!", id));
            }
        } catch (DataAccessException e) {
            log.error("Ошибка при удалении жанра по id = {}", id, e);
        }
        throw new ApplicationException(String.format("Ошибка при удалении жанра по id = %s", id));
    }
}