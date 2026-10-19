package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    Film add(Film film);

    Optional<Film> getById(long id);

    Film update(Film film);

    void delete(long id);

    List<Film> getAll();
}
