package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dao.impl.GenreDbStorage;
import ru.yandex.practicum.filmorate.dao.impl.MPADbStorage;
import ru.yandex.practicum.filmorate.dao.GenreStorage;
import ru.yandex.practicum.filmorate.dao.MPAStorage;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@JdbcTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GenreDbStorageAndMPADbStorageTest {
    private final JdbcTemplate jdbcTemplate;
    private GenreStorage genreStorage;
    private MPAStorage mpaStorage;
//    private final List<Genre> genres = List.of(new Genre(1, "Комедия"), new Genre(2, "Драма"),
//            new Genre(3, "Мультфильм"), new Genre(4, "Триллер"),
//            new Genre(5, "Документальный"), new Genre(6, "Боевик"));
//    private final List<MPA> mpa = List.of(new MPA(1, "G"), new MPA(2, "PG"),
//            new MPA(3, "PG-13"), new MPA(4, "R"), new MPA(5, "NC-17"));
//
//
//    @BeforeEach
//    void create() {
//        genreStorage = new GenreDbStorage(jdbcTemplate);
//        mpaStorage = new MPADbStorage(jdbcTemplate);
//    }
//
//    @Test
//    public void getAndGetByIdGenres() {
//        assertDoesNotThrow(() -> genreStorage.get());
//        assertThat(genreStorage.get())
//                .isNotNull()
//                .usingRecursiveComparison()
//                .isEqualTo(genres);
//
//        assertThat(genreStorage.getById(1).get())
//                .isNotNull()
//                .usingRecursiveComparison()
//                .isEqualTo(genres.get(0));
//
//        assertThat(genreStorage.getById(10))
//                .usingRecursiveComparison()
//                .isEqualTo(Optional.empty());
//    }
//
//    @Test
//    public void getAndGetByIdMPA() {
//        assertDoesNotThrow(() -> mpaStorage.get());
//        assertThat(mpaStorage.get())
//                .isNotNull()
//                .usingRecursiveComparison()
//                .isEqualTo(mpa);
//
//        assertThat(mpaStorage.getById(1).get())
//                .isNotNull()
//                .usingRecursiveComparison()
//                .isEqualTo(mpa.get(0));
//
//        assertThat(mpaStorage.getById(10))
//                .usingRecursiveComparison()
//                .isEqualTo(Optional.empty());
//    }
}
