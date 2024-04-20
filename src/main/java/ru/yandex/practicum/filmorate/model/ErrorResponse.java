package ru.yandex.practicum.filmorate.model;

import lombok.Getter;

@Getter
public class ErrorResponse {
    String description;
    public ErrorResponse(String description) {
        this.description = description;
    }
}
