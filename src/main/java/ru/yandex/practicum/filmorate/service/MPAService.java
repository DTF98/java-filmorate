package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.MPADbStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MPA;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class MPAService {
    private final MPADbStorage mpaStorage;

    public List<MPA> get() {
        log.info("Получение списка всех Mpa");
        return mpaStorage.get();
    }

    public MPA getByID(Integer id) throws NotFoundException {
        MPA mpa = mpaStorage.getById(id);
        if (mpa == null) {
            throw new NotFoundException(String.format("Жанр по id = %s не найден", id));
        } else {
            log.info("Получение Mpa id={}", id);
            return mpa;
        }
    }
}
