package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Repository
@Primary
@RequiredArgsConstructor
public class GenreDbStorage {

    private final JdbcTemplate jdbc;
    private final GenreRowMapper mapper;

    public List<Genre> getAll() {
        return jdbc.query("SELECT id, name FROM genres ORDER BY id", mapper);
    }

    public Genre getById(long id) {
        List<Genre> result = jdbc.query(
                "SELECT id, name FROM genres WHERE id = ?", mapper, id);
        if (result.isEmpty()) {
            throw new NotFoundException(String.format("Жанр с id = %d не найден", id));
        }
        return result.get(0);
    }
}
