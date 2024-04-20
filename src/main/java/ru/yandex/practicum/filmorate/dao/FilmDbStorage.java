package ru.yandex.practicum.filmorate.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.impl.FilmStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Film> get() {
        return jdbcTemplate.query("SELECT id, NAME, DESCRIPTION, DURATION, RELEASE_DATE, " +
                "array_agg(GENRE), MPA, MPA_ID, array_agg(GENRE_ID) FROM films AS f JOIN (SELECT FILM_ID, " +
                "GENRE, gf.GENRE_ID FROM genre_film as gf JOIN genres AS g ON g.genre_id = gf.genre_id) AS" +
                " G ON f.ID = G.film_id JOIN (SELECT FILM_ID , MPA, mf.MPA_ID FROM MPA_FILM as mf JOIN MPA AS" +
                " m ON m.mpa_id = mf.mpa_id) AS M ON f.ID = M.film_id GROUP BY id;", this::mapRowToFilm);
    }

    public Film set(Film film) {
        String sqlFilm = "insert into films (name, description, release_date, duration) " +
                "values (?, ?, ?, ?);";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlFilm, new String[]{"id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setInt(4, film.getDuration());
            return stmt;
        }, keyHolder);
        Integer filmID = Objects.requireNonNull(keyHolder.getKey()).intValue();
        for (Genre genre : film.getGenres()) {
            String sqlGenre = "insert into GENRE_FILM (film_id, genre_id) " +
                    "values (?, ?)";
            jdbcTemplate.update(sqlGenre, filmID, genre.getId());
        }
        String sqlRating = "insert into mpa_film (film_id, mpa_id) " +
                "values (?, ?);";
        jdbcTemplate.update(sqlRating, filmID, film.getMpa().getId());
        log.info(String.format("Добавлен фильм: {%s}", filmID));
        return getById(filmID);
    }

    public Film update(Film film) {
        if (contains(film.getId())) {
            try {
                String sqlFilms = "UPDATE films SET name = ?, description = ?, release_Date = ?, " +
                        "duration = ? WHERE id = ?;";
                jdbcTemplate.update(sqlFilms, film.getName(), film.getDescription(), film.getReleaseDate(),
                        film.getDuration(), film.getId());
                return film;
            } catch (EmptyResultDataAccessException e) {
                return null;
            }
        } else {
            throw new NotFoundException("Фильм не найден!");
        }
    }

    public Film getById(Integer id) {
        if (contains(id)) {
            return jdbcTemplate.query(String.format("SELECT id, NAME, DESCRIPTION, DURATION, RELEASE_DATE, " +
                            "array_agg(GENRE), MPA, MPA_ID, array_agg(GENRE_ID) FROM films AS f LEFT JOIN (SELECT FILM_ID, " +
                            "GENRE, gf.GENRE_ID FROM genre_film as gf JOIN genres AS g ON g.genre_id = gf.genre_id) AS" +
                            " G ON f.ID = G.film_id JOIN (SELECT FILM_ID , MPA, mf.MPA_ID FROM MPA_FILM as mf JOIN MPA AS" +
                            " m ON m.mpa_id = mf.mpa_i" +
                            "d) AS M ON f.ID = M.film_id where id = %s GROUP BY id;", id),
                    this::mapRowToFilm).stream().findFirst().orElse(null);
        } else {
            throw new NotFoundException(String.format("Фильм по id = %s не найден!", id));
        }
    }

    public boolean contains(Integer id) {
        Optional<Film> film = Optional.ofNullable(jdbcTemplate.queryForObject(
                String.format("select * from films where id = %s", id),
                this::mapRowToFilmContains));
        return film.isPresent();
    }

    public List<Film> getMostPopularFilms(Integer count) {
        if (count > 0) {
            return jdbcTemplate.query(String.format("SELECT id, NAME, DESCRIPTION, DURATION, RELEASE_DATE," +
                            " array_agg(GENRE), MPA, likes, MPA_ID, array_agg(GENRE_ID) FROM films AS f JOIN (SELECT FILM_ID," +
                            " GENRE, gf.GENRE_ID FROM genre_film as gf JOIN genres AS g ON g.genre_id = gf.genre_id) AS" +
                            " G ON f.ID = G.film_id JOIN (SELECT FILM_ID , MPA, mf.MPA_ID FROM MPA_FILM as mf JOIN MPA AS" +
                            " m ON m.mpa_id = mf.mpa_id) AS M ON f.ID = M.film_id JOIN (SELECT FILM_ID, COUNT(USER_ID) AS" +
                            " likes FROM FILM_LIKES GROUP BY FILM_ID) AS fl ON f.ID = FL.FILM_ID GROUP BY id ORDER BY LIKES DESC" +
                            " LIMIT %s;", count),
                    this::mapRowToFilm);
        } else {
            throw new ValidationException("Передан параметр в count <= 0 ");
        }
    }

    public void setLike(Integer filmID, Integer userID) {
        if (contains(filmID)) {
            String sqlLike = "insert into film_likes (user_id, film_id) values (?, ?)";
            jdbcTemplate.update(sqlLike, userID, filmID);
        } else {
            throw new NotFoundException(String.format("Фильм по id = %s не найден!", filmID));
        }
    }

    public void removeLike(Integer filmID, Integer userID) {
        if (contains(filmID)) {
            if (getLikes(filmID).contains(userID)) {
                String sqlFL = String.format("delete from film_likes where film_id = %s and user_id = %s", filmID, userID);
                jdbcTemplate.update(sqlFL);
            }
        } else {
            throw new ValidationException("Не найден фильм!");
        }
    }

    public List<Integer> getLikes(Integer filmID) {
        return jdbcTemplate.query(String.format("select user_id from film_likes where film_id = %s", filmID),
                new SingleColumnRowMapper<>(Integer.class));
    }

    private Film mapRowToFilm(ResultSet resultSet, int rowNum) throws SQLException {
        Set<Genre> newGenres = new TreeSet<>(Comparator.comparing(Genre::getId));
        List<Integer> id = Arrays.stream(
                resultSet.getString("ARRAY_AGG(GENRE_ID)")
                .replaceAll("[\\[\\]\\\\ ]", "")
                .split(","))
                .filter(elem -> !elem.equals("null"))
                .map(Integer::parseInt)
                .collect(Collectors.toList()
                );
        List<String> name = Arrays.stream(
                resultSet.getString("ARRAY_AGG(GENRE)")
                .replaceAll("[\\[\\]\\\\ ]", "")
                .split(","))
                .filter(elem -> !elem.equals("null"))
                .collect(Collectors.toList()
                );
        for (int i = 0; i < id.size(); i++ ) {
            newGenres.add(new Genre(id.get(i), Optional.ofNullable(name.get(i))));
        }
        return Film.builder()
                .id(resultSet.getInt("id"))
                .name(resultSet.getString("name"))
                .description(resultSet.getString("description"))
                .releaseDate(LocalDate.parse(resultSet.getString("release_date")))
                .duration(resultSet.getInt("duration"))
                .genres(newGenres)
                .mpa(new MPA(resultSet.getInt("mpa_id"), Optional.ofNullable(resultSet.getString("mpa"))))
                .build();
    }

    private Film mapRowToFilmContains(ResultSet resultSet, int rowNum) throws SQLException {
        return Film.builder()
                .id(resultSet.getInt("id"))
                .name(resultSet.getString("name"))
                .description(resultSet.getString("description"))
                .releaseDate(LocalDate.parse(resultSet.getString("release_date")))
                .duration(resultSet.getInt("duration"))
                .build();
    }
}
