package ru.yandex.practicum.filmorate.dao;

import java.util.List;
import java.util.Optional;

public interface Storage<T> {
    T add(T item);

    Optional<T> getById(Integer id);

    List<T> get();

    T update(T item);

    boolean delete(Integer id);
}
