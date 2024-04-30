package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import javax.validation.Valid;
import java.util.Collection;

import static ru.yandex.practicum.filmorate.util.ResponseUtil.respondSuccess;
import static ru.yandex.practicum.filmorate.util.ResponseUtil.respondSuccessList;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping()
    public ResponseEntity<Review> add(@Valid @RequestBody Review review) {
        log.info("Добавление отзыва к фильму");

        Review addedFilm = reviewService.add(review);
        log.info("Отзыв успешно добавлен");

        return respondSuccess(addedFilm);
    }



    @PutMapping()
    public ResponseEntity<Review> update(@Valid @RequestBody Review review) {
        log.info("Обновление отзыва к фильму");

        Review updatedReview = reviewService.update(review);
        log.info("Отзыв успешно обновлен");

        return respondSuccess(updatedReview);
    }

    @GetMapping()
    public ResponseEntity<Collection<Review>> getAll() {
        log.info("Получить все отзывы на фильм");
        log.info("Текущее количество отзывов на фильм: {}", reviewService.getCount());

        return respondSuccessList(reviewService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Review> getById(@PathVariable long id) {
        log.info("Получить отзыв по ID фильма - {}", id);

        return respondSuccess(reviewService.getById(id));
    }

}
