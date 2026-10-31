package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
//import ru.yandex.practicum.filmorate.exception.InvalidCountException;
import ru.yandex.practicum.filmorate.exception.LikeNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final ConcurrentMap<Long, Set<Long>> likesMap = new ConcurrentHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(FilmService.class);

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public List<Film> getAll() {
        return filmStorage.getAll();
    }

    public Film add(Film film) {
        return filmStorage.add(film);
    }

    public Film getById(long id) {
        return filmStorage.getById(id)
                .orElseThrow(() -> new FilmNotFoundException(id));
    }

    public void addLike(long filmId, long userId) {
        log.debug("Ставим лайк: filmId={}, userId={}", filmId, userId);

        Film film = filmStorage.getById(filmId)
                .orElseThrow(() -> new FilmNotFoundException(filmId));

        User user = userStorage.getById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с ID " + userId + " не найден"));

        if (film.getLikes().contains(userId)) {
            throw new IllegalArgumentException("Пользователь уже поставил лайк этому фильму");
        }

        film.getLikes().add(userId);
        filmStorage.update(film);

        likesMap.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);

        log.debug("Лайк успешно поставлен: filmId={}, userId={}", filmId, userId);
    }

    /*public Film update(long id, Film film) {
        Film existing = filmStorage.getById(id)
                .orElseThrow(() -> new FilmNotFoundException(id));

        existing.setName(film.getName());
        existing.setDescription(film.getDescription());
        existing.setReleaseDate(film.getReleaseDate());
        existing.setDuration(film.getDuration());

        return filmStorage.update(existing);
    }*/

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

    public void removeLike(long filmId, long userId) {
        log.debug("Запрос на удаление лайка: filmId={}, userId={}", filmId, userId);

        Film film = filmStorage.getById(filmId)
                .orElseThrow(() -> new FilmNotFoundException(filmId));

        boolean wasRemoved = film.getLikes().remove(userId);

        if (!wasRemoved) {
            throw new LikeNotFoundException(filmId, userId);
        }

        filmStorage.update(film);
        log.debug("Лайк успешно удалён: filmId={}, userId={}", filmId, userId);
    }

    public List<Film> getPopular(Integer count) {
        int limit = (count == null || count <= 0) ? 10 : count;

        return filmStorage.getAll().stream()
                .sorted(Comparator.comparing(f -> f.getLikes().size(), Comparator.reverseOrder()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /*public List<Film> getTopNFilms(int count) {
        log.debug("Запрос топ-N фильмов: count={}", count);

        if (count <= 0) {
            log.warn("Некорректный параметр count={}. Должен быть > 0", count);
            throw new InvalidCountException("Параметр count должен быть положительным числом");
        }

        List<Film> top = filmStorage.getAll().stream()
                .sorted((f1, f2) -> Integer.compare(
                        getLikesCount(f2.getId()),
                        getLikesCount(f1.getId())
                ))
                .limit(count)
                .collect(Collectors.toList());

        log.debug("Вернули топ-{} фильмов по лайкам", top.size());
        return top;
    }*/

    public int getLikesCount(long filmId) {
        return likesMap.getOrDefault(filmId, Collections.emptySet()).size();
    }
}