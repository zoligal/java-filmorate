package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@Repository
@Primary
@RequiredArgsConstructor
public class MpaDbStorage {

    private final JdbcTemplate jdbc;
    private final MpaRowMapper mapper;

    public List<Mpa> getAll() {
        return jdbc.query("SELECT id, name FROM mpa ORDER BY id", mapper);
    }

    public Mpa getById(long id) {
        List<Mpa> result = jdbc.query(
                "SELECT id, name FROM mpa WHERE id = ?", mapper, id);
        if (result.isEmpty()) {
            throw new NotFoundException(String.format("Рейтинг с id = %d не найден", id));
        }
        return result.get(0);
    }
}
