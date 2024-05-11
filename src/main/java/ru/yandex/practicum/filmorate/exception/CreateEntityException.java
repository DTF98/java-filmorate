package ru.yandex.practicum.filmorate.exception;

public class CreateEntityException extends RuntimeException {
    public CreateEntityException(String msg) {
        super(msg);
    }
}
