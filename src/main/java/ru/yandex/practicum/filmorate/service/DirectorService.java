package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.DirectorStorage;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class DirectorService {
    private final DirectorStorage directorStorage;

    public List<Director> get() {
        log.info("Получение списка всех режисеров");
        return new ArrayList<>(directorStorage.get());
    }

    public Director getById(Integer id) {
        log.info("Получение режисера по id : {}", id);
        Optional<Director> director = directorStorage.getById(id);
        if (director.isPresent()) {
            log.info(String.format("Получен фильм по id = %s : %s", id, director));
            return director.get();
        }
        return null;
    }

    public Director add(Director director) {
        log.info("Добавление режисера: {}", director.getId());
        return directorStorage.add(director);
    }

    public Director update(Director director) {
        log.info("Обновление режисера: {}", director.getId());
        return directorStorage.update(director);
    }

    public boolean deleteById(Integer id) {
        log.info("Удаление режисера: {}", id);
        return directorStorage.delete(id);
    }
}
