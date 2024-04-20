package ru.yandex.practicum.filmorate.dao.impl;

import java.util.List;

public interface Storage<T> {
    T set(T item);

    T getById(Integer id);

    List<T> get();

    T update(T item);

    boolean contains(Integer id);
}
