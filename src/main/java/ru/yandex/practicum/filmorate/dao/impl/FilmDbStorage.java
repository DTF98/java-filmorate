package ru.yandex.practicum.filmorate.dao.impl;

import lombok.AllArgsConstructor;
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
import ru.yandex.practicum.filmorate.model.Director;
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
@AllArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    public List<Film> get() {
        try {
            return jdbcTemplate.query("SELECT id, NAME, DESCRIPTION, DURATION, RELEASE_DATE, array_agg(GENRE), MPA, " +
                    "MPA_ID, array_agg(GENRE_ID), array_agg(DIRECTOR_ID), array_agg(DIRECTOR_NAME) " +
                    "FROM films AS f " +
                    "LEFT JOIN (SELECT FILM_ID, GENRE, fg.GENRE_ID FROM film_genres as fg " +
                    "JOIN genres AS g ON g.genre_id = fg.genre_id) AS G ON f.ID = G.film_id " +
                    "LEFT JOIN (SELECT FILM_ID , MPA, fm.MPA_ID FROM FILM_MPA as fm " +
                    "JOIN MPA AS m ON m.mpa_id = fm.mpa_id) AS M ON f.ID = M.film_id " +
                    "LEFT JOIN (SELECT FILM_ID, DIRECTOR_NAME, fd.DIRECTOR_ID FROM FILM_DIRECTORS AS fd " +
                    "JOIN DIRECTORS AS d ON d.director_id = fd.director_id) AS D ON f.ID = D.FILM_ID " +
                    "GROUP BY id;", this::mapRowToFilm);
        } catch (DataAccessException e) {
            log.error("Ошибка при получении списка всех фильмов", e);
            return null;
        }
    }

    public Optional<Film> getById(Integer id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("SELECT id, NAME, DESCRIPTION, DURATION, RELEASE_DATE, " +
                    "array_agg(GENRE), MPA, MPA_ID, array_agg(GENRE_ID), array_agg(DIRECTOR_ID), array_agg(DIRECTOR_NAME) " +
                    "FROM films AS f " +
                    "LEFT JOIN (SELECT FILM_ID, GENRE, fg.GENRE_ID FROM film_genres as fg " +
                    "JOIN genres AS g ON g.genre_id = fg.genre_id) AS G ON f.ID = G.film_id " +
                    "LEFT JOIN (SELECT FILM_ID , MPA, fm.MPA_ID FROM FILM_MPA as fm " +
                    "JOIN MPA AS m ON m.mpa_id = fm.mpa_id) AS M ON f.ID = M.film_id " +
                    "LEFT JOIN (SELECT FILM_ID, DIRECTOR_NAME, fd.DIRECTOR_ID FROM FILM_DIRECTORS AS fd " +
                    "JOIN DIRECTORS AS d ON d.director_id = fd.director_id) AS D ON f.ID = D.FILM_ID " +
                    "where id = ? " +
                    "GROUP BY id;", this::mapRowToFilm, id));
        } catch (DataAccessException e) {
            throw new NotFoundException(String.format("Фильм по id = %s не найден!", id));
        }
    }

    public List<Film> getMostPopularFilms(Integer count) {
        if (count > 0) {
            try {
                return jdbcTemplate.query(String.format("SELECT id, NAME, DESCRIPTION, DURATION, RELEASE_DATE, " +
                                "array_agg(GENRE), MPA, MPA_ID, array_agg(GENRE_ID), array_agg(DIRECTOR_ID), array_agg(DIRECTOR_NAME) " +
                                "FROM films AS f " +
                                "LEFT JOIN (SELECT FILM_ID, GENRE, fg.GENRE_ID FROM film_genres as fg " +
                                "JOIN genres AS g ON g.genre_id = fg.genre_id) AS G ON f.ID = G.film_id " +
                                "LEFT JOIN (SELECT FILM_ID , MPA, fm.MPA_ID FROM FILM_MPA as fm " +
                                "JOIN MPA AS m ON m.mpa_id = fm.mpa_id) AS M ON f.ID = M.film_id " +
                                "LEFT JOIN (SELECT FILM_ID, COUNT(USER_ID) AS likes " +
                                "FROM FILM_LIKES GROUP BY FILM_ID) AS fl ON f.ID = FL.FILM_ID " +
                                "LEFT JOIN (SELECT FILM_ID, DIRECTOR_NAME, fd.DIRECTOR_ID FROM FILM_DIRECTORS AS fd " +
                                "JOIN DIRECTORS AS d ON d.director_id = fd.director_id) AS D ON f.ID = D.FILM_ID " +
                                "GROUP BY id " +
                                "ORDER BY LIKES DESC" +
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

    public List<Film> getSearchedFilms(String query, List<String> by) {
        String maskedQuery = "%" + query + "%";
        boolean isTitle = by.contains("title");
        boolean isDirector = by.contains("director");
        try {
            String sqlSearch = "SELECT id, NAME, DESCRIPTION, DURATION, RELEASE_DATE, " +
                    "array_agg(GENRE), MPA, MPA_ID, array_agg(GENRE_ID), array_agg(DIRECTOR_ID), " +
                    "array_agg(DIRECTOR_NAME) " +
                    "FROM films AS f " +
                    "LEFT JOIN (SELECT FILM_ID, GENRE, fg.GENRE_ID FROM film_genres as fg " +
                    "LEFT JOIN genres AS g ON g.genre_id = fg.genre_id) AS G ON f.ID = G.film_id " +
                    "LEFT JOIN (SELECT FILM_ID , MPA, fm.MPA_ID FROM FILM_MPA as fm " +
                    "JOIN MPA AS m ON m.mpa_id = fm.mpa_id) AS M ON f.ID = M.film_id " +
                    "LEFT JOIN (SELECT FILM_ID, COUNT(USER_ID) AS likes " +
                    "FROM FILM_LIKES GROUP BY FILM_ID) AS fl ON f.ID = FL.FILM_ID " +
                    "LEFT JOIN (SELECT d.DIRECTOR_ID, d.DIRECTOR_NAME, fd.FILM_ID " +
                    "FROM FILM_DIRECTORS as fd " +
                    "LEFT JOIN DIRECTORS AS d ON d.DIRECTOR_ID = fd.DIRECTOR_ID) AS D ON f.ID = D.FILM_ID " +
                    "WHERE CASEWHEN(?, NAME ilike (?), false) OR CASEWHEN(?, D.DIRECTOR_NAME ilike (?), false) " +
                    "GROUP BY id " +
                    "ORDER BY LIKES DESC;";
            return jdbcTemplate.query(connection -> {
                PreparedStatement preparedStatement = connection.prepareStatement(sqlSearch);
                preparedStatement.setBoolean(1, isTitle);
                preparedStatement.setString(2, maskedQuery);
                preparedStatement.setBoolean(3, isDirector);
                preparedStatement.setString(4, maskedQuery);
                return preparedStatement;
            }, this::mapRowToFilm);
        } catch (DataAccessException e) {
            log.error("Ошибка при поиске фильмов", e);
        }
        return new ArrayList<>();
    }

    public List<Film> getSortedLikesListOfDirectorsFilms(Integer directorId) {
        try {
            List<Film> like = jdbcTemplate.query(String.format("SELECT id, NAME, DESCRIPTION, DURATION, RELEASE_DATE, " +
                            "array_agg(GENRE), MPA, MPA_ID, array_agg(GENRE_ID), array_agg(DIRECTOR_ID), array_agg(DIRECTOR_NAME) " +
                            "FROM films AS f " +
                            "LEFT JOIN (SELECT FILM_ID, GENRE, fg.GENRE_ID FROM film_genres as fg " +
                            "JOIN genres AS g ON g.genre_id = fg.genre_id) AS G ON f.ID = G.film_id " +
                            "LEFT JOIN (SELECT FILM_ID , MPA, fm.MPA_ID FROM FILM_MPA as fm " +
                            "JOIN MPA AS m ON m.mpa_id = fm.mpa_id) AS M ON f.ID = M.film_id " +
                            "LEFT JOIN (SELECT FILM_ID, COUNT(USER_ID) AS likes " +
                            "FROM FILM_LIKES GROUP BY FILM_ID) AS fl ON f.ID = FL.FILM_ID " +
                            "RIGHT JOIN (SELECT FILM_ID, DIRECTOR_NAME, fd.DIRECTOR_ID FROM FILM_DIRECTORS AS fd " +
                            "JOIN DIRECTORS AS d ON d.director_id = fd.director_id WHERE fd.DIRECTOR_ID = %s) AS D ON f.ID = D.FILM_ID " +
                            "GROUP BY id " +
                            "ORDER BY LIKES DESC;", directorId),
                    this::mapRowToFilm);
            if (like.isEmpty()) {
                throw new NotFoundException("Режисер или его фильмы не найдены");
            } else {
                log.info("Получен список фильмов режиссера id = {} отсортированных по лайкам", directorId);
                return like;
            }
        } catch (DataAccessException e) {
            log.error("Ошибка при получении списка фильмов режиссера id = {} отсортированных по колличеству лайков", directorId);
        }
        return null;
    }

    public List<Film> getSortedYearListOfDirectorsFilms(Integer directorId) {
        try {
            List<Film> year = jdbcTemplate.query(String.format("SELECT id, NAME, DESCRIPTION, DURATION, RELEASE_DATE, " +
                            "array_agg(GENRE), MPA, MPA_ID, array_agg(GENRE_ID), array_agg(DIRECTOR_ID), array_agg(DIRECTOR_NAME) " +
                            "FROM films AS f " +
                            "LEFT JOIN (SELECT FILM_ID, GENRE, fg.GENRE_ID FROM film_genres as fg " +
                            "JOIN genres AS g ON g.genre_id = fg.genre_id) AS G ON f.ID = G.film_id " +
                            "LEFT JOIN (SELECT FILM_ID , MPA, fm.MPA_ID FROM FILM_MPA as fm " +
                            "JOIN MPA AS m ON m.mpa_id = fm.mpa_id) AS M ON f.ID = M.film_id " +
                            "LEFT JOIN (SELECT FILM_ID, COUNT(USER_ID) AS likes " +
                            "FROM FILM_LIKES GROUP BY FILM_ID) AS fl ON f.ID = FL.FILM_ID " +
                            "RIGHT JOIN (SELECT FILM_ID, DIRECTOR_NAME, fd.DIRECTOR_ID FROM FILM_DIRECTORS AS fd " +
                            "JOIN DIRECTORS AS d ON d.director_id = fd.director_id WHERE fd.DIRECTOR_ID = %s) AS D ON f.ID = D.FILM_ID " +
                            "GROUP BY id " +
                            "ORDER BY RELEASE_DATE ASC;", directorId),
                    this::mapRowToFilm);
            if (year.isEmpty()) {
                throw new NotFoundException("Режисер или его фильмы не найдены");
            } else {
                log.info("Получен список фильмов режиссера id = {} отсортированных по году", directorId);
                return year;
            }
        } catch (DataAccessException e) {
            log.error("Ошибка при получении списка фильмов режиссера id = {} отсортированных по году", directorId);
        }
        return null;
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
            updateFilmMPALinks(film);
            updateFilmDirectorsLinks(film);
            log.info("Добавлен фильм по id = {}", filmID);
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
                Film sortedByGenre = updateFilmGenresLinks(film);
                updateFilmMPALinks(sortedByGenre);
                log.info("Обновлен фильм по id = {}", film.getId());
                return updateFilmDirectorsLinks(sortedByGenre);
            } else {
                throw new NotFoundException("Фильм не найден!");
            }
        } catch (DataAccessException e) {
            return null;
        }
    }

    public List<Film> getMostPopularFilmsByGenreId(Integer count, Integer genreId) {
        if (count > 0) {
            try {
                return jdbcTemplate.query(String.format("SELECT id, NAME, DESCRIPTION, DURATION, RELEASE_DATE," +
                                " array_agg(GENRE), MPA, likes, MPA_ID, array_agg(GENRE_ID), array_agg(DIRECTOR_ID), array_agg(DIRECTOR_NAME)" +
                                " FROM films AS f LEFT JOIN (SELECT FILM_ID," +
                                " GENRE, fg.GENRE_ID FROM film_genres as fg JOIN genres AS g ON g.genre_id = fg.genre_id) AS" +
                                " G ON f.ID = G.film_id LEFT JOIN (SELECT FILM_ID , MPA, fm.MPA_ID FROM FILM_MPA as fm JOIN MPA AS" +
                                " m ON m.mpa_id = fm.mpa_id) AS M ON f.ID = M.film_id LEFT JOIN (SELECT FILM_ID, COUNT(USER_ID) AS" +
                                " likes FROM FILM_LIKES GROUP BY FILM_ID) AS fl ON f.ID = FL.FILM_ID" +
                                " LEFT JOIN (SELECT FILM_ID, DIRECTOR_NAME, fd.DIRECTOR_ID FROM FILM_DIRECTORS AS fd" +
                                " JOIN DIRECTORS AS d ON d.director_id = fd.director_id) AS D ON f.ID = D.FILM_ID" +
                                " RIGHT JOIN (SELECT FILM_ID FROM FILM_GENRES WHERE GENRE_ID = %s) AS fi ON f.ID = fi.FILM_ID" +
                                " GROUP BY id ORDER BY LIKES DESC" +
                                " LIMIT %s;", genreId, count),
                        this::mapRowToFilm);
            } catch (DataAccessException e) {
                log.error("Ошибка при получении списка популярных фильмов", e);
            }
        } else {
            throw new ValidationException("Передан параметр в count <= 0 ");
        }
        return null;
    }

    public List<Film> getMostPopularFilmsByYear(Integer count, Integer year) {
        if (count > 0) {
            try {
                return jdbcTemplate.query(String.format("SELECT id, NAME, DESCRIPTION, DURATION, RELEASE_DATE," +
                                " array_agg(GENRE), MPA, likes, MPA_ID, array_agg(GENRE_ID), array_agg(DIRECTOR_ID), array_agg(DIRECTOR_NAME)" +
                                " FROM films AS f LEFT JOIN (SELECT FILM_ID," +
                                " GENRE, fg.GENRE_ID FROM film_genres as fg JOIN genres AS g ON g.genre_id = fg.genre_id) AS" +
                                " G ON f.ID = G.film_id LEFT JOIN (SELECT FILM_ID , MPA, fm.MPA_ID FROM FILM_MPA as fm JOIN MPA AS" +
                                " m ON m.mpa_id = fm.mpa_id) AS M ON f.ID = M.film_id LEFT JOIN (SELECT FILM_ID, COUNT(USER_ID) AS" +
                                " likes FROM FILM_LIKES GROUP BY FILM_ID) AS fl ON f.ID = FL.FILM_ID" +
                                " LEFT JOIN (SELECT FILM_ID, DIRECTOR_NAME, fd.DIRECTOR_ID FROM FILM_DIRECTORS AS fd" +
                                " JOIN DIRECTORS AS d ON d.director_id = fd.director_id) AS D ON f.ID = D.FILM_ID" +
                                " WHERE FORMATDATETIME(RELEASE_DATE ,'yyyy') = %s" +
                                " GROUP BY id" +
                                " ORDER BY LIKES DESC" +
                                " LIMIT %s;", year, count),
                        this::mapRowToFilm);
            } catch (DataAccessException e) {
                log.error("Ошибка при получении списка популярных фильмов", e);
            }
        } else {
            throw new ValidationException("Передан параметр в count <= 0 ");
        }
        return null;
    }

    public List<Film> getMostPopularFilmsByGenreIdAndYear(Integer count, Integer year, Integer genreId) {
        if (count > 0) {
            try {
                return jdbcTemplate.query(String.format("SELECT id, NAME, DESCRIPTION, DURATION, RELEASE_DATE," +
                                " array_agg(GENRE), MPA, likes, MPA_ID, array_agg(GENRE_ID), array_agg(DIRECTOR_ID), array_agg(DIRECTOR_NAME)" +
                                " FROM films AS f LEFT JOIN (SELECT FILM_ID," +
                                " GENRE, fg.GENRE_ID FROM film_genres as fg JOIN genres AS g ON g.genre_id = fg.genre_id) AS" +
                                " G ON f.ID = G.film_id LEFT JOIN (SELECT FILM_ID , MPA, fm.MPA_ID FROM FILM_MPA as fm JOIN MPA AS" +
                                " m ON m.mpa_id = fm.mpa_id) AS M ON f.ID = M.film_id LEFT JOIN (SELECT FILM_ID, COUNT(USER_ID) AS" +
                                " likes FROM FILM_LIKES GROUP BY FILM_ID) AS fl ON f.ID = FL.FILM_ID" +
                                " RIGHT JOIN (SELECT FILM_ID FROM FILM_GENRES WHERE GENRE_ID = %s) AS fi ON f.ID = fi.FILM_ID" +
                                " LEFT JOIN (SELECT FILM_ID, DIRECTOR_NAME, fd.DIRECTOR_ID FROM FILM_DIRECTORS AS fd" +
                                " JOIN DIRECTORS AS d ON d.director_id = fd.director_id) AS D ON f.ID = D.FILM_ID" +
                                " WHERE FORMATDATETIME(RELEASE_DATE ,'yyyy') = %s" +
                                " GROUP BY id" +
                                " ORDER BY LIKES DESC" +
                                " LIMIT %s;", genreId, year, count),
                        this::mapRowToFilm);
            } catch (DataAccessException e) {
                log.error("Ошибка при получении списка популярных фильмов", e);
                return new ArrayList<>();
            }
        } else {
            throw new ValidationException("Передан параметр в count <= 0 ");
        }
    }

    public Film addLike(Integer filmID, Integer userID) {
        try {
            String sqlLike = "MERGE INTO film_likes KEY(user_id, film_id) VALUES (?,?)";
            if (jdbcTemplate.update(sqlLike, userID, filmID) > 0) {
                Optional<Film> check = getById(filmID);
                if (check.isPresent()) {
                    log.info("Добавлен лайк фильму id = {}, пользователем id = {}", filmID, userID);
                    return check.get();
                }
            } else {
                throw new NotFoundException(String.format("Фильм по id = %s не найден!", filmID));
            }
        } catch (DataAccessException e) {
            log.error("Ошибка при добавления лайка", e);
        }
        return null;
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
        log.info("Удаление фильма по id = {}", id);
        String sqlQuery = "DELETE FROM films WHERE ID= ?";
        deleteFilmGenresLinks(id);
        deleteFilmMPALinks(id);
        deleteFilmLikesLinks(id);
        return jdbcTemplate.update(sqlQuery, id) > 0;
    }

    public List<Integer> getLikes(Integer filmID) {
        try {
            List<Integer> likes = jdbcTemplate.query(String.format("select user_id from film_likes where film_id = %s", filmID),
                    new SingleColumnRowMapper<>(Integer.class));
            log.info("Получены лайки фильма id = {}", filmID);
            return likes;
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
                ps.setLong(1, film.getId());
                ps.setLong(2, genre.getId());
            }

            @Override
            public int getBatchSize() {
                return genres.size();
            }
        });
        log.info("Обновлёны жанры фильма id = {}", film.getId());
        return film;
    }

    private Film updateFilmDirectorsLinks(Film film) {
        deleteFilmDirectorsLinks(film.getId());
        String sqlQuery = "INSERT INTO FILM_DIRECTORS (FILM_ID, DIRECTOR_ID) " +
                "VALUES (?, ?)";
        Set<Director> directors = new TreeSet<>(Comparator.comparing(Director::getId));
        directors.addAll(film.getDirectors());
        film.setDirectors(directors);
        jdbcTemplate.batchUpdate(sqlQuery, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Director director = new ArrayList<>(directors).get(i);
                ps.setLong(1, film.getId());
                ps.setLong(2, director.getId());
            }

            @Override
            public int getBatchSize() {
                return directors.size();
            }
        });
        log.info("Обновлёны режисеры фильма id = {}", film.getId());
        return film;
    }

    private void updateFilmMPALinks(Film film) {
        if (film.getMpa().getId() != null) {
            deleteFilmMPALinks(film.getId());
            String sql = "insert into film_mpa (film_id, mpa_id) values (?, ?);";
            jdbcTemplate.update(sql, film.getId(), film.getMpa().getId());
            log.info("Обновлён рейтинг фильма id = {}", film.getId());
        }
    }

    private void deleteFilmMPALinks(Integer filmId) {
        String sqlQuery = "DELETE FROM FILM_MPA " +
                "WHERE film_id = ?";
        jdbcTemplate.update(sqlQuery, filmId);
        log.info("Удален рейтинг у фильма id = {}", filmId);
    }

    private void deleteFilmGenresLinks(Integer filmId) {
        String sqlQuery = "DELETE FROM FILM_GENRES " +
                "WHERE FILM_ID = ?";
        jdbcTemplate.update(sqlQuery, filmId);
        log.info("Удалены жанры у фильма id = {}", filmId);
    }

    private void deleteFilmLikesLinks(Integer filmId) {
        String sqlQuery = "DELETE FROM FILM_LIKES " +
                "WHERE FILM_ID = ?";
        jdbcTemplate.update(sqlQuery, filmId);
        log.info("Удалены лайки у фильма id = {}", filmId);
    }

    private void deleteFilmDirectorsLinks(Integer filmId) {
        String sqlQuery = "DELETE FROM FILM_DIRECTORS " +
                "WHERE FILM_ID = ?";
        jdbcTemplate.update(sqlQuery, filmId);
        log.info("Удалены жанры у фильма id = {}", filmId);
    }

    private Film mapRowToFilm(ResultSet resultSet, int rowNum) throws SQLException {
        return Film.builder()
                .id(resultSet.getInt("id"))
                .name(resultSet.getString("name"))
                .description(resultSet.getString("description"))
                .releaseDate(LocalDate.parse(resultSet.getString("release_date")))
                .duration(resultSet.getInt("duration"))
                .genres(mapRowToSetGenres(resultSet))
                .directors(mapRowToSetDirectors((resultSet)))
                .mpa(new MPA(resultSet.getInt("mpa_id"), resultSet.getString("mpa")))
                .build();
    }

    private Set<Genre> mapRowToSetGenres(ResultSet resultSet) throws SQLException {
        Set<Genre> newGenres = new TreeSet<>(Comparator.comparing(Genre::getId));
        List<Integer> idGen = Arrays.stream(
                resultSet.getString("ARRAY_AGG(GENRE_ID)")
                .replaceAll("[\\[\\]\\\\ ]", "")
                .split(","))
                .filter(elem -> !elem.equals("null"))
                .map(Integer::parseInt)
                .collect(Collectors.toList()
                );
        List<String> nameGen = Arrays.stream(
                resultSet.getString("ARRAY_AGG(GENRE)")
                .replaceAll("[\\[\\]\\\\ ]", "")
                .split(","))
                .filter(elem -> !elem.equals("null"))
                .collect(Collectors.toList()
                );
        for (int i = 0; i < idGen.size(); i++) {
            newGenres.add(new Genre(idGen.get(i), nameGen.get(i)));
        }
        return newGenres;
    }

    private Set<Director> mapRowToSetDirectors(ResultSet resultSet) throws SQLException {
        Set<Director> newDirectors = new TreeSet<>(Comparator.comparing(Director::getId));
        List<Integer> idDir = Arrays.stream(
                resultSet.getString("ARRAY_AGG(DIRECTOR_ID)")
                .replaceAll("[\\[\\] ]", "")
                .split(","))
                .filter(elem -> !elem.equals("null"))
                .map(Integer::parseInt)
                .collect(Collectors.toList()
                );
        List<String> nameDir = Arrays.stream(
                resultSet.getString("ARRAY_AGG(DIRECTOR_NAME)")
                .replaceAll("[\\[\\]]", "")
                .split(","))
                .filter(elem -> !elem.equals("null"))
                .collect(Collectors.toList()
                );
        for (int i = 0; i < idDir.size(); i++) {
            newDirectors.add(new Director(idDir.get(i), nameDir.get(i)));
        }
        return newDirectors;
    }
}