package ru.yandex.practicum.filmorate.dao.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.FilmStorage;
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

import static ru.yandex.practicum.filmorate.constant.ConstantError.ERROR_ENTITY_FILM;

@Component
@Slf4j
@AllArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    public List<Film> get() {
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
    }

    public Optional<Film> getById(Integer id) {
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
    }

    public List<Film> getMostPopularFilms(Integer count) {
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
    }

    public List<Film> getSearchedFilms(String query, List<String> by) {
        String maskedQuery = "%" + query + "%";
        boolean isTitle = by.contains("title");
        boolean isDirector = by.contains("director");
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
    }

    public List<Film> getSortedLikesListOfDirectorsFilms(Integer directorId) {
        return jdbcTemplate.query(String.format("SELECT id, NAME, DESCRIPTION, DURATION, RELEASE_DATE, " +
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
    }

    public List<Film> getSortedYearListOfDirectorsFilms(Integer directorId) {
        return jdbcTemplate.query(String.format("SELECT id, NAME, DESCRIPTION, DURATION, RELEASE_DATE, " +
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
    }

    public Film add(Film film) {
        String sqlFilm = "insert into films (name, description, release_date, duration) " +
                "values (?, ?, ?, ?);";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        if (jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlFilm, new String[]{"id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setInt(4, film.getDuration());
            return stmt;
        }, keyHolder) > 0) {
            Integer filmID = Objects.requireNonNull(keyHolder.getKey()).intValue();
            film.setId(filmID);
            updateFilmGenresLinks(film);
            updateFilmMPALinks(film);
            updateFilmDirectorsLinks(film);
            log.info("Добавлен фильм по id = {}", filmID);
            return film;
        } else {
            return ERROR_ENTITY_FILM;
        }
    }

    public Film update(Film film) {
        String sqlFilms = "UPDATE films SET name = ?, description = ?, release_Date = ?, " +
                "duration = ? WHERE id = ?;";
        if (jdbcTemplate.update(sqlFilms, film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getId()) > 0) {
            Film sortedByGenre = updateFilmGenresLinks(film);
            updateFilmMPALinks(sortedByGenre);
            log.info("Обновлен фильм по id = {}", film.getId());
            return updateFilmDirectorsLinks(sortedByGenre);
        } else {
            return ERROR_ENTITY_FILM;
        }
    }

    public List<Film> getMostPopularFilmsByGenreId(Integer count, Integer genreId) {
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
    }

    public List<Film> getMostPopularFilmsByYear(Integer count, Integer year) {
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
    }

    public List<Film> getMostPopularFilmsByGenreIdAndYear(Integer count, Integer year, Integer genreId) {
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
    }

    public boolean addLike(Integer filmID, Integer userID) {
        String sqlLike = "MERGE INTO film_likes KEY(user_id, film_id) VALUES (?,?)";
        return jdbcTemplate.update(sqlLike, userID, filmID) > 0;
    }

    public boolean deleteLike(Integer filmID, Integer userID) {
        String sqlFL = "delete from film_likes where film_id = ? and user_id = ?";
        return jdbcTemplate.update(sqlFL, filmID, userID) > 0;
    }

    public boolean delete(Integer id) {
        String sqlQuery = "DELETE FROM films WHERE ID= ?";
        return jdbcTemplate.update(sqlQuery, id) > 0;
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

    private void deleteFilmDirectorsLinks(Integer filmId) {
        String sqlQuery = "DELETE FROM FILM_DIRECTORS " +
                "WHERE FILM_ID = ?";
        jdbcTemplate.update(sqlQuery, filmId);
        log.info("Удалены режисеры у фильма id = {}", filmId);
    }

    public List<Integer> getListOfUsersFilms(Integer id) {
        SqlRowSet userFilms = jdbcTemplate.queryForRowSet("SELECT FILM_ID FROM FILM_LIKES WHERE USER_ID=?", id);
        List<Integer> userFilmIds = new ArrayList<>();
        while (userFilms.next()) {
            userFilmIds.add(userFilms.getInt("FILM_ID"));
        }
        return userFilmIds;
    }

    public Map<Integer, Integer> getFilmIdByPopularity(List<Integer> filmIds) {
        Map<Integer, Integer> filmPopularityMap = new HashMap<>();
        for (Integer id : filmIds) {
            SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("SELECT COUNT(FILM_ID) FROM FILM_LIKES WHERE FILM_ID=?", id);
            while (sqlRowSet.next()) {
                int count = sqlRowSet.getInt("COUNT(FILM_ID)");
                filmPopularityMap.put(id, count);
            }
        }
        return filmPopularityMap;
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
        List<Integer> idGen = Arrays.stream(resultSet.getString("ARRAY_AGG(GENRE_ID)")
                .replaceAll("[\\[\\]\\\\ ]", "")
                .split(","))
                .filter(elem -> !elem.equals("null"))
                .map(Integer::parseInt)
                .collect(Collectors.toList()
                );
        List<String> nameGen = Arrays.stream(resultSet.getString("ARRAY_AGG(GENRE)")
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
        List<Integer> idDir = Arrays.stream(resultSet.getString("ARRAY_AGG(DIRECTOR_ID)")
                .replaceAll("[\\[\\] ]", "")
                .split(","))
                .filter(elem -> !elem.equals("null"))
                .map(Integer::parseInt)
                .collect(Collectors.toList()
                );
        List<String> nameDir = Arrays.stream(resultSet.getString("ARRAY_AGG(DIRECTOR_NAME)")
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