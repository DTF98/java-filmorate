package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.DirectorStorage;
import ru.yandex.practicum.filmorate.exception.CreateEntityException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorStorage directorStorage;

    public List<Director> get() {
        List<Director> directors = new ArrayList<>(directorStorage.get());
        log.info("Получен список всех режисеров {}", directors);
        return directors;
    }

    public Director getById(Integer id) {
        Director director = directorStorage.getById(id).orElseThrow(() ->
                new NotFoundException(String.format("Режисер по id = %s не найден!", id)));
        log.info("Получен режисер по id = {}", id);
        return director;
    }

    public Director add(Director director) {
        Director added = directorStorage.add(director);
        if (added.getId() == null) {
            throw new CreateEntityException(String.format("Режисер не добавлен! %s", director));
        }
        return added;
    }

    public Director update(Director director) {
        getById(director.getId());
        Director updated = directorStorage.update(director);
        log.info("Обновлен режисер по id = {}", director.getId());
        return updated;
    }

    public Director delete(Integer id) {
        Director deleted = getById(id);
        directorStorage.delete(id);
        log.info("Режисер по id = {} удален", id);
        return deleted;
    }
}
