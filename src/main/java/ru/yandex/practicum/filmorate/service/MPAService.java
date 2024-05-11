package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.impl.MPADbStorage;
import ru.yandex.practicum.filmorate.exception.CreateEntityException;
import ru.yandex.practicum.filmorate.exception.ApplicationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MPA;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class MPAService {
    private final MPADbStorage mpaStorage;

    public List<MPA> get() {
        try {
            log.info("Получение списка всех рейтингов");
            return mpaStorage.get();
        } catch (DataAccessException e) {
            log.error("Ошибка при получении всех рейтингов", e);
        }
        throw new ApplicationException("Ошибка при получении всех рейтингов");
    }

    public MPA getByID(Integer id) throws NotFoundException {
        try {
            Optional<MPA> mpa = mpaStorage.getById(id);
            if (mpa.isPresent()) {
                log.info("Получение рейтинга по id = {}", id);
                return mpa.get();
            }
        } catch (DataAccessException e) {
            log.error("Ошибка при получении рейтинга по id = {}", id, e);
            throw new NotFoundException(String.format("Рейтинг по id = %s не найден", id));
        }
        throw new ApplicationException(String.format("Ошибка при получении рейтинга по id = %s", id));
    }

    public MPA add(MPA mpa) {
        try {
            MPA added = mpaStorage.add(mpa);
            if (added.getId() == 0) {
                throw new CreateEntityException(String.format("Рейтинг не добавлен! %s", mpa));
            }
            return added;
        } catch (DataAccessException e) {
            log.error("Ошибка при добавлении рейтинга {}", mpa, e);
        }
        throw new ApplicationException(String.format("Ошибка при добавлении рейтинга %s", mpa));
    }

    public MPA update(MPA mpa) {
        try {
            MPA updated = mpaStorage.update(mpa);
            if (updated.getId() == 0) {
                throw new NotFoundException("Рейтинг не найден!");
            }
            return updated;
        } catch (DataAccessException e) {
            log.error("Ошибка при обновлении рейтинга {}", mpa, e);
        }
        throw new ApplicationException(String.format("Ошибка при обновлении рейтинга %s", mpa));
    }

    public boolean delete(Integer id) {
        try {
            if (mpaStorage.isExistMpaById(id)) {
                if (mpaStorage.delete(id)) {
                    log.info("Удален рейтинг по id = {}", id);
                    return true;
                }
            } else {
                throw new NotFoundException(String.format("Рейтинг по id = %s не найден", id));
            }
        } catch (DataAccessException e) {
            log.error("Ошибка при удалении рейтинга по id = {}", id, e);
        }
        throw new ApplicationException(String.format("Ошибка при удалении рейтинга по id = %s", id));
    }
}
