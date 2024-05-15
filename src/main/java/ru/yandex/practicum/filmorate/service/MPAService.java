package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.impl.MPADbStorage;
import ru.yandex.practicum.filmorate.exception.CreateEntityException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MPA;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class MPAService {
    private final MPADbStorage mpaStorage;

    public List<MPA> get() {
        log.info("Получение списка всех рейтингов");
        return mpaStorage.get();
    }

    public MPA getByID(Integer id) throws NotFoundException {
        MPA mpa = mpaStorage.getById(id).orElseThrow(() ->
                new NotFoundException(String.format("Не найден mpa по id = %s", id)));
        log.info("Получен рейтинг по id = {}", id);
        return mpa;
    }

    public MPA add(MPA mpa) {
        MPA added = mpaStorage.add(mpa);
        if (added.getId() == null) {
            throw new CreateEntityException(String.format("Рейтинг не добавлен! %s", mpa));
        }
        log.info("Добавлен рейтинг {}", added);
        return added;
    }

    public MPA update(MPA mpa) {
        getByID(mpa.getId());
        MPA updated = mpaStorage.update(mpa);
        log.info("Обновлен рейтинг по id = {}", mpa.getId());
        return updated;
    }

    public MPA delete(Integer id) {
        MPA deleted = getByID(id);
        mpaStorage.delete(id);
        log.info("Удален рейтинг по id = {}", id);
        return deleted;
    }
}
