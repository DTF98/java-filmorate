package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.DirectorStorage;
import ru.yandex.practicum.filmorate.exception.CreateEntityException;
import ru.yandex.practicum.filmorate.exception.ApplicationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorStorage directorStorage;

    public List<Director> get() {
        try {
            List<Director> directors = new ArrayList<>(directorStorage.get());
            log.info("Получен список всех режисеров {}", directors);
            return directors;
        } catch (DataAccessException e) {
            log.error("Ошибка при получении списка всех режисеров", e);
        }
        throw new ApplicationException("Ошибка при получении списка всех режисеров");
    }

    public Director getById(Integer id) {
        try {
            Optional<Director> director = directorStorage.getById(id);
            if (director.isPresent()) {
                log.info(String.format("Получен фильм по id = %s : %s", id, director));
                return director.get();
            }
        } catch (DataAccessException e) {
            log.error("Ошибка при получении режисера по id = {}",id, e);
            throw new NotFoundException(String.format("Режисер по id = %s не найден!", id));
        }
        throw new ApplicationException(String.format("Ошибка при получении режисера по id = %s", id));
    }

    public Director add(Director director) {
        try {
            Director added = directorStorage.add(director);
            if (added.getId() == 0) {
                throw new CreateEntityException(String.format("Режисер не добавлен! %s", director));
            }
            return added;
        } catch (DataAccessException e) {
            log.error("Ошибка при добавлении режисера {}",director, e);
        }
        throw new ApplicationException(String.format("Ошибка при добавлении режисера %s",director));
    }

    public Director update(Director director) {
        try {
            Director updated = directorStorage.update(director);
            if (updated.getId() == 0) {
                throw new NotFoundException("Режисер не найден!");
            }
            return updated;
        } catch (DataAccessException e) {
            log.error("Ошибка при обновлении режисера {}",director, e);
        }
        throw new ApplicationException(String.format("Ошибка при обновлении режисера %s",director));
    }

    public boolean deleteById(Integer id) {
        try {
            if (directorStorage.isExistDirectorById(id)) {
                if (directorStorage.delete(id)) {
                    log.info("Режисер по id = {} удален", id);
                    return true;
                }
            }
        } catch (DataAccessException e) {
            log.error("Ошибка при удалении режисера {}", id, e);
        }
        throw new ApplicationException(String.format("Ошибка при удалении режисера %s", id));
    }
}
