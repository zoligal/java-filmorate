package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private static final Logger log = LoggerFactory.getLogger(FilmService.class);

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public List<Film> getAll() {
        return filmStorage.getAll();
    }

    public Film add(Film film) {
        return filmStorage.add(film);
    }

    public Film update(Film film) {
        if (film.getId() == null) {
            throw new ValidationException("Для обновления требуется идентификатор фильма");
        }

        Film existing = filmStorage.getById(film.getId())
                .orElseThrow(() -> new FilmNotFoundException(film.getId()));

        existing.setName(film.getName());
        existing.setDescription(film.getDescription());
        existing.setReleaseDate(film.getReleaseDate());
        existing.setDuration(film.getDuration());

        return filmStorage.update(existing);
    }

    public Film getById(long id) {
        return filmStorage.getById(id)
                .orElseThrow(() -> new FilmNotFoundException(id));
    }

    public void addLike(long filmId, long userId) {
        filmStorage.getById(filmId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Фильм с id = %d не найден", filmId)));
        userStorage.getByIdOrFail(userId);
        filmStorage.addLike(filmId, userId);
        log.debug("Лайк поставлен: filmId={}, userId={}", filmId, userId);
    }

    public void removeLike(long filmId, long userId) {
        filmStorage.getById(filmId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Фильм с id = %d не найден", filmId)));
        userStorage.getByIdOrFail(userId);
        filmStorage.removeLike(filmId, userId);
        log.debug("Лайк удалён: filmId={}, userId={}", filmId, userId);
    }

    public List<Film> getPopular(Integer count) {
        int limit = (count == null || count <= 0) ? 10 : count;
        return filmStorage.getPopular(limit);
    }
}
