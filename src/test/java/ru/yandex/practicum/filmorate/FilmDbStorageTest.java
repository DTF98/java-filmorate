package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.dao.impl.FilmStorage;
import ru.yandex.practicum.filmorate.dao.impl.UserStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmDbStorageTest {
    private final JdbcTemplate jdbcTemplate;
    private FilmStorage filmStorage;
    private UserStorage userStorage;

    @BeforeEach
    void create() {
        filmStorage = new FilmDbStorage(jdbcTemplate);
        userStorage = new UserDbStorage(jdbcTemplate);
    }

    @Test
    public void createAndGetByIdNewFilm() {
        Set<Genre> genres = new TreeSet<>(Comparator.comparing(Genre::getId));
        genres.add(new Genre(1, Optional.of("Комедия")));
        genres.add(new Genre(2, Optional.of("Драма")));
        Film newFilm = new Film(1, "Shakal", "Sakal is back",
                LocalDate.of(2025,12, 12), 140, genres,
                new MPA(3, Optional.of("PG-13")));

        filmStorage.set(newFilm);
        assertThat(filmStorage.getById(1))
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(newFilm);

        assertThrows(NotFoundException.class, () -> filmStorage.getById(10));
    }

    @Test
    void testAddRemoveGetLikes() {
        Set<Genre> genres = new TreeSet<>(Comparator.comparing(Genre::getId));
        genres.add(new Genre(1, Optional.of("Комедия")));
        genres.add(new Genre(2, Optional.of("Драма")));
        Film newFilm = filmStorage.set(new Film(1, "Shakal", "Sakal is back",
                LocalDate.of(2025,12, 12), 140, genres,
                new MPA(3, Optional.of("PG-13"))));
        User stepan = userStorage.set(new User(1, "user@email.ru", "Petuhan", "Stepan",
                LocalDate.of(1990, 1, 1)));
        filmStorage.setLike(1,1);

        List<Integer> likes = filmStorage.getLikes(newFilm.getId());
        assertThat(likes.get(0))
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(stepan.getId());

        filmStorage.removeLike(1, 1);
        List<Integer> likes0 = filmStorage.getLikes(newFilm.getId());
        assertEquals(0, likes0.size());
    }

    @Test
    void testGetPopular() {
        Set<Genre> genres1 = new TreeSet<>(Comparator.comparing(Genre::getId));
        genres1.add(new Genre(1, Optional.of("Комедия")));
        genres1.add(new Genre(2, Optional.of("Драма")));

        Set<Genre> genres2 = new TreeSet<>(Comparator.comparing(Genre::getId));
        genres2.add(new Genre(3, Optional.of("Мультфильм")));
        genres2.add(new Genre(4, Optional.of("Триллер")));

        Set<Genre> genres3 = new TreeSet<>(Comparator.comparing(Genre::getId));
        genres3.add(new Genre(5, Optional.of("Документальный")));
        genres3.add(new Genre(6, Optional.of("Боевик")));

        Film newFilm1 = filmStorage.set(new Film(1, "Shakal1", "Sakal is back1",
                LocalDate.of(2025,12, 11), 140, genres1,
                new MPA(1, Optional.of("G"))));

        Film newFilm2 = filmStorage.set(new Film(2, "Shakal2", "Sakal is back2",
                LocalDate.of(2025,12, 12), 140, genres2,
                new MPA(2, Optional.of("PG"))));

        Film newFilm3 = filmStorage.set(new Film(3, "Shakal3", "Sakal is back3",
                LocalDate.of(2025,12, 13), 140, genres3,
                new MPA(3, Optional.of("PG-13"))));

        User stepan1 = userStorage.set(new User(1, "user@email.ru", "Petuhan", "Stepan1",
                LocalDate.of(1990, 1, 1)));

        User stepan2 = userStorage.set(new User(2, "user@email.ru", "Petuhan", "Stepan2",
                LocalDate.of(1990, 1, 2)));

        User stepan3 = userStorage.set(new User(3, "user@email.ru", "Petuhan", "Stepan3",
                LocalDate.of(1990, 1, 3)));

        User stepan4 = userStorage.set(new User(4, "user@email.ru", "Petuhan", "Stepan4",
                LocalDate.of(1990, 1, 4)));

        User stepan5 = userStorage.set(new User(5, "user@email.ru", "Petuhan", "Stepan5",
                LocalDate.of(1990, 1, 5)));

        User stepan6 = userStorage.set(new User(6, "user@email.ru", "Petuhan", "Stepan6",
                LocalDate.of(1990, 1, 6)));
        filmStorage.setLike(1, 1);
        filmStorage.setLike(1, 2);
        filmStorage.setLike(1, 3);
        filmStorage.setLike(2, 4);
        filmStorage.setLike(2, 5);
        filmStorage.setLike(3, 6);

        List<Film> top = filmStorage.getMostPopularFilms(1000);
        List<Film> test = List.of(newFilm1,newFilm2,newFilm3);
        assertThat(top)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(test);
    }
}
