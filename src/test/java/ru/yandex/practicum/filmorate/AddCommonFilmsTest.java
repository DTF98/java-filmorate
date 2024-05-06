//package ru.yandex.practicum.filmorate;
//
//import lombok.RequiredArgsConstructor;
//import org.junit.jupiter.api.BeforeEach;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.annotation.DirtiesContext;
//import ru.yandex.practicum.filmorate.model.*;
//import ru.yandex.practicum.filmorate.service.FilmService;
//import ru.yandex.practicum.filmorate.service.UserService;
//
//import java.time.LocalDate;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//@SpringBootTest
//@AutoConfigureTestDatabase
//@RequiredArgsConstructor(onConstructor_ = @Autowired)
//@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
//public class AddCommonFilmsTest {
//    private final FilmService filmService;
//    private final UserService userService;
//    private final MPA rating = new MPA(1, "G");
//    private final Set<Genre> genres = new HashSet<>();
//    private final Set<Director> directors = new HashSet<>();
//
//    @BeforeEach
//    public void setUp() {
//        directors.add(new Director(1, "K.M."));
//        directors.add(new Director(2, "L.S."));
//        genres.add(new Genre(1, "Комедия"));
//        genres.add(new Genre(2, "Драма"));
//    }
//
//    public void shouldGetCommonFilms(){
//        Film film = filmService.addFilm(new Film(1, "Film", "FilmDescription",
//                LocalDate.of(2000, 1, 1), 100, genres, rating, directors));
//
//        Film film2 = filmService.addFilm(new Film(2, "Film2", "FilmDescription2",
//                LocalDate.of(2000, 1, 1), 100, genres, rating, directors));
//
//        Film film3 = filmService.addFilm(new Film(3, "Film3", "FilmDescription3",
//                LocalDate.of(2000, 1, 1), 100, genres, rating, directors));
//
//        Film film4 = filmService.addFilm(new Film(4, "Film4", "FilmDescription4",
//                LocalDate.of(2000, 1, 1), 100, genres, rating, directors));
//
//        User user = userService.setUser(new User(2, "vladimir-malyshev-2010@mail.ru", "Vl",
//                "Vladimir", LocalDate.of(1997, 9, 11)));
//
//        User user1 = userService.setUser(new User(2, "vladimir-malyshev-2010@mail.ru", "Vl",
//                "Vladimir", LocalDate.of(1997, 9, 11)));
//
//    }
//
//
//}
