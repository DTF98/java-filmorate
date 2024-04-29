package ru.yandex.practicum.filmorate.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collection;

public class ResponseUtil {

    public static <T> ResponseEntity<T> respondSuccess(T body) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(body);
    }

    public static <T> ResponseEntity<Collection<T>> respondSuccessList(Collection<T> body) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(body);
    }

}
