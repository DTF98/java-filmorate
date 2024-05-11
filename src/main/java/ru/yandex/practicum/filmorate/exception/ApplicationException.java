package ru.yandex.practicum.filmorate.exception;

public class ApplicationException extends RuntimeException {
    public ApplicationException(String msg) {
        super(msg);
    }
}
