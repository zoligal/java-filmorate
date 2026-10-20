package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final ConcurrentHashMap<Long, Film> films = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @Override
    public Optional<Film> getById(long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public Film add(Film film) {
        if (film == null) {
            throw new IllegalArgumentException("Фильм не должен быть равен null");
        }
        long id = nextId.getAndIncrement();
        film.setId(id);
        films.put(id, film);
        return film;
    }
    //
    @Override
    public Film update(Film film) {
        if (film == null || film.getId() == null) {
            throw new IllegalArgumentException("Фильм и его ID не должны быть null");
        }
        Long filmId = film.getId();

        if (!films.containsKey(filmId)) {
            throw new FilmNotFoundException(filmId);
        }
        films.put(filmId, film);

        return films.get(filmId);
    }


    @Override
    public List<Film> getAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public void delete(long id) {
        films.remove(id);
    }
}
