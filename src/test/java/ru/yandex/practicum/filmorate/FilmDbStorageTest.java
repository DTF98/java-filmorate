package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dao.FilmStorage;
import ru.yandex.practicum.filmorate.dao.UserStorage;

@JdbcTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmDbStorageTest {
    private final JdbcTemplate jdbcTemplate;
    private FilmStorage filmStorage;
    private UserStorage userStorage;

//    @BeforeEach
//    void create() {
//        filmStorage = new FilmDbStorage(jdbcTemplate);
//        userStorage = new UserDbStorage(jdbcTemplate);
//    }

//    @Test
//    public void createAndGetByIdNewFilm() {
//        Set<Genre> genres = new TreeSet<>(Comparator.comparing(Genre::getId));
//        genres.add(new Genre(1, "Комедия"));
//        genres.add(new Genre(2, "Драма"));
//        Film newFilm = new Film(1, "Shakal", "Sakal is back",
//                LocalDate.of(2025,12, 12), 140, genres,
//                new MPA(3, "PG-13"));
//
//        Film saved = filmStorage.add(newFilm);
//        assertThat(filmStorage.getById(saved.getId()).get())
//                .isNotNull()
//                .usingRecursiveComparison()
//                .isEqualTo(saved);
//
//        assertThrows(NotFoundException.class, () -> filmStorage.getById(10));
//    }
//
//    @Test
//    void testAddRemoveGetLikes() {
//        Set<Genre> genres = new TreeSet<>(Comparator.comparing(Genre::getId));
//        genres.add(new Genre(1, "Комедия"));
//        genres.add(new Genre(2, "Драма"));
//        Film newFilm = filmStorage.add(new Film(1, "Shakal", "Sakal is back",
//                LocalDate.of(2025,12, 12), 140, genres,
//                new MPA(3, "PG-13")));
//        User stepan = userStorage.add(new User(1, "user@email.ru", "Petuhan", "Stepan",
//                LocalDate.of(1990, 1, 1)));
//        filmStorage.addLike(newFilm.getId(),stepan.getId());
//
//        List<Integer> likes = filmStorage.getLikes(newFilm.getId());
//        assertThat(likes.get(0))
//                .isNotNull()
//                .usingRecursiveComparison()
//                .isEqualTo(stepan.getId());
//
//        filmStorage.removeLike(newFilm.getId(), stepan.getId());
//        List<Integer> likes0 = filmStorage.getLikes(newFilm.getId());
//        assertEquals(0, likes0.size());
//    }
//
//    @Test
//    void testGetPopular() {
//        Set<Genre> genres1 = new TreeSet<>(Comparator.comparing(Genre::getId));
//        genres1.add(new Genre(1, "Комедия"));
//        genres1.add(new Genre(2, "Драма"));
//
//        Set<Genre> genres2 = new TreeSet<>(Comparator.comparing(Genre::getId));
//        genres2.add(new Genre(3, "Мультфильм"));
//        genres2.add(new Genre(4, "Триллер"));
//
//        Set<Genre> genres3 = new TreeSet<>(Comparator.comparing(Genre::getId));
//        genres3.add(new Genre(5, "Документальный"));
//        genres3.add(new Genre(6, "Боевик"));
//
//        Film newFilm1 = filmStorage.add(new Film(1, "Shakal1", "Sakal is back1",
//                LocalDate.of(2025,12, 11), 140, genres1,
//                new MPA(1, "G")));
//
//        Film newFilm2 = filmStorage.add(new Film(2, "Shakal2", "Sakal is back2",
//                LocalDate.of(2025,12, 12), 140, genres2,
//                new MPA(2, "PG")));
//
//        Film newFilm3 = filmStorage.add(new Film(3, "Shakal3", "Sakal is back3",
//                LocalDate.of(2025,12, 13), 140, genres3,
//                new MPA(3, "PG-13")));
//
//        User stepan1 = userStorage.add(new User(1, "user@email.ru", "Petuhan", "Stepan1",
//                LocalDate.of(1990, 1, 1)));
//
//        User stepan2 = userStorage.add(new User(2, "user@email.ru", "Petuhan", "Stepan2",
//                LocalDate.of(1990, 1, 2)));
//
//        User stepan3 = userStorage.add(new User(3, "user@email.ru", "Petuhan", "Stepan3",
//                LocalDate.of(1990, 1, 3)));
//
//        User stepan4 = userStorage.add(new User(4, "user@email.ru", "Petuhan", "Stepan4",
//                LocalDate.of(1990, 1, 4)));
//
//        User stepan5 = userStorage.add(new User(5, "user@email.ru", "Petuhan", "Stepan5",
//                LocalDate.of(1990, 1, 5)));
//
//        User stepan6 = userStorage.add(new User(6, "user@email.ru", "Petuhan", "Stepan6",
//                LocalDate.of(1990, 1, 6)));
//        filmStorage.addLike(newFilm1.getId(), stepan1.getId());
//        filmStorage.addLike(newFilm1.getId(), stepan2.getId());
//        filmStorage.addLike(newFilm1.getId(), stepan3.getId());
//        filmStorage.addLike(newFilm2.getId(), stepan4.getId());
//        filmStorage.addLike(newFilm2.getId(), stepan5.getId());
//        filmStorage.addLike(newFilm3.getId(), stepan6.getId());
//
//        List<Film> top = filmStorage.getMostPopularFilms(1000);
//        List<Film> test = List.of(newFilm1,newFilm2,newFilm3);
//        assertThat(top)
//                .isNotNull()
//                .usingRecursiveComparison()
//                .isEqualTo(test);
//    }
}
