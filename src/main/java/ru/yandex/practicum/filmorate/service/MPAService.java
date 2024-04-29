package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.impl.MPADbStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MPA;

import java.util.List;
import java.util.Optional;

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
        Optional<MPA> mpa = mpaStorage.getById(id);
        if (mpa.isPresent()) {
            log.info("Получение Mpa id={}", id);
            return mpa.get();
        } else {
            throw new NotFoundException(String.format("Рейтинг по id = %s не найден", id));
        }
    }
}
