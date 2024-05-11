package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Director;

public interface DirectorStorage extends Storage<Director> {

    boolean isExistDirectorById(Integer id);
}
