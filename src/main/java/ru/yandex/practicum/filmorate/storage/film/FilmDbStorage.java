package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Repository("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbc;
    private final MpaDbStorage mpaStorage;
    private final GenreDbStorage genreStorage;
    private final GenreRowMapper genreMapper;

    private final FilmRowMapper mapper = new FilmRowMapper();

    @Override
    public Film add(Film film) {
        validateRefs(film);
        deduplicateGenres(film);

        String sql = """
                INSERT INTO films (name, description, release_date, duration, mpa_id)
                VALUES (?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setLong(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            film.setId(key.longValue());
        }
        saveGenres(film);
        return film;
    }

    @Override
    public Optional<Film> getById(long id) {
        String sql = """
                SELECT f.*, m.name AS mpa_name
                FROM films f
                JOIN mpa m ON f.mpa_id = m.id
                WHERE f.id = ?
                """;
        List<Film> result = jdbc.query(sql, mapper, id);
        if (result.isEmpty()) {
            return Optional.empty();
        }
        Film film = result.get(0);
        loadGenres(film);
        loadLikes(film);
        return Optional.of(film);
    }

    @Override
    public Film update(Film film) {
        validateRefs(film);

        String sql = """
                UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ?
                WHERE id = ?
                """;
        int rows = jdbc.update(sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        if (rows == 0) {
            return null;
        }
        if (film.getGenres() != null) {
            saveGenres(film);
        }
        return getById(film.getId()).orElse(null);
    }

    @Override
    public void delete(long id) {
        jdbc.update("DELETE FROM films WHERE id = ?", id);
    }

    @Override
    public List<Film> getAll() {
        String sql = """
                SELECT f.*, m.name AS mpa_name
                FROM films f
                JOIN mpa m ON f.mpa_id = m.id
                ORDER BY f.id
                """;
        List<Film> films = jdbc.query(sql, mapper);
        films.forEach(f -> {
            loadGenres(f);
            loadLikes(f);
        });
        return films;
    }

    private void deduplicateGenres(Film film) {
        if (film.getGenres() != null) {
            film.setGenres(new ArrayList<>(new LinkedHashSet<>(film.getGenres())));
        }
    }

    private void saveGenres(Film film) {
        jdbc.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        if (film.getGenres() == null) {
            return;
        }
        for (Genre genre : film.getGenres()) {
            jdbc.update("MERGE INTO film_genres (film_id, genre_id) KEY(film_id, genre_id) VALUES (?, ?)",
                    film.getId(), genre.getId());
        }
    }

    private void loadGenres(Film film) {
        String sql = """
                SELECT g.id, g.name
                FROM film_genres fg
                JOIN genres g ON fg.genre_id = g.id
                WHERE fg.film_id = ?
                ORDER BY g.id
                """;
        List<Genre> genres = jdbc.query(sql, (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getLong("id"));
            genre.setName(rs.getString("name"));
            return genre;
        }, film.getId());
        film.setGenres(genres);
    }

    private void validateRefs(Film film) {
        if (film.getMpa() != null) {
            mpaStorage.getById(film.getMpa().getId());
        }
        if (film.getGenres() != null) {
            film.getGenres().forEach(g -> genreStorage.getById(g.getId()));
        }
    }

    public void addLike(long filmId, long userId) {
        jdbc.update("MERGE INTO likes (film_id, user_id) KEY(film_id, user_id) VALUES (?, ?)",
                filmId, userId);
    }

    public void removeLike(long filmId, long userId) {
        jdbc.update("DELETE FROM likes WHERE film_id = ? AND user_id = ?", filmId, userId);
    }

    private void loadLikes(Film film) {
        Set<Long> likes = new HashSet<>(jdbc.query(
                "SELECT user_id FROM likes WHERE film_id = ?",
                (rs, rowNum) -> rs.getLong("user_id"), film.getId()));
        film.setLikes(likes);
    }

    public List<Film> getPopular(int count) {
        String sql = """
                SELECT f.*, m.name AS mpa_name, COUNT(l.user_id) AS likes_count
                FROM films f
                JOIN mpa m ON f.mpa_id = m.id
                LEFT JOIN likes l ON l.film_id = f.id
                GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name
                ORDER BY likes_count DESC, f.id ASC
                LIMIT ?
                """;
        List<Film> films = jdbc.query(sql, mapper, count);
        films.forEach(f -> {
            loadGenres(f);
            loadLikes(f);
        });
        return films;
    }

}
