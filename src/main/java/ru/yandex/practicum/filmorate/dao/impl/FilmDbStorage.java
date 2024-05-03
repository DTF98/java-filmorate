package ru.yandex.practicum.filmorate.dao.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.FilmStorage;
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
        try {
            return jdbcTemplate.query("SELECT id, NAME, DESCRIPTION, DURATION, RELEASE_DATE, " +
                    "array_agg(GENRE), MPA, MPA_ID, array_agg(GENRE_ID) FROM films AS f LEFT JOIN (SELECT FILM_ID, " +
                    "GENRE, fg.GENRE_ID FROM film_genres as fg JOIN genres AS g ON g.genre_id = fg.genre_id) AS" +
                    " G ON f.ID = G.film_id LEFT JOIN (SELECT FILM_ID , MPA, fm.MPA_ID FROM FILM_MPA as fm JOIN MPA AS" +
                    " m ON m.mpa_id = fm.mpa_id) AS M ON f.ID = M.film_id GROUP BY id;", this::mapRowToFilm);
        } catch (DataAccessException e) {
            log.error("Ошибка при получении списка всех фильмов", e);
            return null;
        }
    }

    public Optional<Film> getById(Integer id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("SELECT id, NAME, DESCRIPTION, DURATION, RELEASE_DATE, " +
                            "array_agg(GENRE), MPA, MPA_ID, array_agg(GENRE_ID) FROM films AS f LEFT JOIN (SELECT FILM_ID, " +
                            "GENRE, fg.GENRE_ID FROM film_genres as fg JOIN genres AS g ON g.genre_id = fg.genre_id) AS" +
                            " G ON f.ID = G.film_id LEFT JOIN (SELECT FILM_ID , MPA, fm.MPA_ID FROM FILM_MPA as fm JOIN MPA AS" +
                            " m ON m.mpa_id = fm.mpa_i" +
                            "d) AS M ON f.ID = M.film_id where id = ? GROUP BY id;",
                    this::mapRowToFilm, id));
        } catch (Exception e) {
            throw new NotFoundException(String.format("Фильм по id = %s не найден!", id));
        }
    }

    public Film add(Film film) {
        try {
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
            film.setId(filmID);
            updateFilmGenresLinks(film);
            String sqlRating = "insert into film_mpa (film_id, mpa_id) " +
                    "values (?, ?);";
            jdbcTemplate.update(sqlRating, filmID, film.getMpa().getId());
            log.info(String.format("Добавлен фильм: {%s}", filmID));
            return film;
        } catch (DataAccessException e) {
            log.error("Ошибка при добавлении фильма", e);
            return null;
        }
    }

    public Film update(Film film) {
        try {
            String sqlFilms = "UPDATE films SET name = ?, description = ?, release_Date = ?, " +
                    "duration = ? WHERE id = ?;";

            if (jdbcTemplate.update(sqlFilms, film.getName(), film.getDescription(), film.getReleaseDate(),
                    film.getDuration(), film.getId()) > 0) {

                Film sort = updateFilmGenresLinks(film);
                if (film.getMpa().getId() != null) {
                    String sql = "UPDATE film_mpa SET mpa_id = ? WHERE film_id = ?;";
                    jdbcTemplate.update(sql, film.getMpa().getId(), film.getId());
                }
                return sort;
            } else {
                throw new NotFoundException("Фильм не найден!");
            }
        } catch (DataAccessException e) {
            return null;
        }
    }

    public List<Film> getMostPopularFilms(Integer count) {
        if (count > 0) {
            try {
                return jdbcTemplate.query(String.format("SELECT id, NAME, DESCRIPTION, DURATION, RELEASE_DATE," +
                                " array_agg(GENRE), MPA, likes, MPA_ID, array_agg(GENRE_ID) FROM films AS f LEFT JOIN (SELECT FILM_ID," +
                                " GENRE, fg.GENRE_ID FROM film_genres as fg JOIN genres AS g ON g.genre_id = fg.genre_id) AS" +
                                " G ON f.ID = G.film_id LEFT JOIN (SELECT FILM_ID , MPA, fm.MPA_ID FROM FILM_MPA as fm JOIN MPA AS" +
                                " m ON m.mpa_id = fm.mpa_id) AS M ON f.ID = M.film_id LEFT JOIN (SELECT FILM_ID, COUNT(USER_ID) AS" +
                                " likes FROM FILM_LIKES GROUP BY FILM_ID) AS fl ON f.ID = FL.FILM_ID GROUP BY id ORDER BY LIKES DESC" +
                                " LIMIT %s;", count),
                        this::mapRowToFilm);
            } catch (DataAccessException e) {
                log.error("Ошибка при получении списка популярных фильмов", e);
            }
        } else {
            throw new ValidationException("Передан параметр в count <= 0 ");
        }
        return null;
    }

    public Film addLike(Integer filmID, Integer userID) {
        try {
            String sqlLike = "MERGE INTO film_likes KEY(user_id, film_id) VALUES (?,?)";
            if (jdbcTemplate.update(sqlLike, userID, filmID) > 0) {
                return getById(filmID).get();
            } else {
                throw new NotFoundException(String.format("Фильм по id = %s не найден!", filmID));
            }
        } catch (DataAccessException e) {
            log.error("Ошибка при добавления лайка", e);
            return null;
        }
    }

    public Integer deleteLike(Integer filmID, Integer userID) {
        try {
            String sqlFL = String.format("delete from film_likes where film_id = %s and user_id = %s", filmID, userID);
            if (jdbcTemplate.update(sqlFL) > 0) {
                log.info("Удален лайк пользователя id = {} для фильма id = {}", userID, filmID);
                return filmID;
            } else {
                throw new NotFoundException(String.format("Фильм по id = %s не найден!", filmID));
            }
        } catch (DataAccessException e) {
            log.error("Ошибка при удалени лайка", e);
            return null;
        }
    }

    public boolean delete(Integer id) {
        String sqlQuery = "DELETE FROM films WHERE ID= ?";
        deleteFilmGenresLinks(id);
        deleteFilmMPA(id);
        deleteFilmLikesLinks(id);
        return jdbcTemplate.update(sqlQuery, id) > 0;
    }

    public List<Integer> getLikes(Integer filmID) {
        try {
            return jdbcTemplate.query(String.format("select user_id from film_likes where film_id = %s", filmID),
                    new SingleColumnRowMapper<>(Integer.class));
        } catch (DataAccessException e) {
            throw new NotFoundException(String.format("Фильм по id = %s не найден!", filmID));
        }
    }

    private Film updateFilmGenresLinks(Film film) {
        deleteFilmGenresLinks(film.getId());
        String sqlQuery = "INSERT INTO FILM_GENRES (FILM_ID, GENRE_ID) " +
                "VALUES (?, ?)";
        Set<Genre> genres = new TreeSet<>(Comparator.comparing(Genre::getId));
        genres.addAll(film.getGenres());
        film.setGenres(genres);
        jdbcTemplate.batchUpdate(sqlQuery, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Genre genre = new ArrayList<>(genres).get(i);
                ps.setInt(1, film.getId());
                ps.setInt(2, genre.getId());
            }

            @Override
            public int getBatchSize() {
                return genres.size();
            }
        });
        return film;
    }

    private void deleteFilmMPA(Integer id) {
        String sqlQuery = "DELETE FROM FILM_MPA " +
                "WHERE film_id = ?";
        jdbcTemplate.update(sqlQuery, id);
    }

    private void deleteFilmGenresLinks(Integer filmId) {
        String sqlQuery = "DELETE FROM FILM_GENRES " +
                "WHERE FILM_ID = ?";
        jdbcTemplate.update(sqlQuery, filmId);
    }

    private void deleteFilmLikesLinks(Integer filmId) {
        String sqlQuery = "DELETE FROM FILM_LIKES " +
                "WHERE FILM_ID = ?";
        jdbcTemplate.update(sqlQuery, filmId);
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
        for (int i = 0; i < id.size(); i++) {
            newGenres.add(new Genre(id.get(i), name.get(i)));
        }
        return Film.builder()
                .id(resultSet.getInt("id"))
                .name(resultSet.getString("name"))
                .description(resultSet.getString("description"))
                .releaseDate(LocalDate.parse(resultSet.getString("release_date")))
                .duration(resultSet.getInt("duration"))
                .genres(newGenres)
                .mpa(new MPA(resultSet.getInt("mpa_id"), resultSet.getString("mpa")))
                .build();
    }
}
