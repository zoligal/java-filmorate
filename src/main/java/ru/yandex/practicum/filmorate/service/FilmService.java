package ru.yandex.practicum.filmorate.service;

import jakarta.validation.ValidationException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.LikeNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.exception.InvalidCountException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final ConcurrentMap<Long, Set<Long>> likesMap = new ConcurrentHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(FilmService.class);


    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public List<Film> getAll() {
        return filmStorage.getAll();
    }

    public Film add(Film film) {
        return filmStorage.add(film);
    }

    public Film update(Film film) {
        if (film.getId() == null) {
            throw new IllegalArgumentException("ID фильма не может быть null при обновлении");
        }

        if (!filmStorage.getById(film.getId()).isPresent()) {
            throw new FilmNotFoundException(film.getId());
        }
        return filmStorage.update(film);
    }

    public Film getById(long id) {
        return filmStorage.getById(id)
                .orElseThrow(() -> new FilmNotFoundException(id));
    }

    public void addLike(long filmId, long userId) {
        log.debug("Ставим лайк: filmId={}, userId={}", filmId, userId);

        validateFilmExists(filmId);

        Film film = filmStorage.getById(filmId)
                .orElseThrow(() -> new FilmNotFoundException(filmId));

        if (film.getLikes().contains(userId)) {
            throw new ValidationException("Пользователь уже поставил лайк этому фильму");
        }

        film.getLikes().add(userId);
        filmStorage.update(film);

        likesMap.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);

        log.debug("Лайк успешно поставлен: filmId={}, userId={}", filmId, userId);
    }

    public void removeLike(long filmId, long userId) {
        log.debug("Запрос на удаление лайка: filmId={}, userId={}", filmId, userId);

        validateFilmExists(filmId);
        Film film = filmStorage.getById(filmId).get();
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

    public List<Film> getTopNFilms(int count) {
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
    }

    public int getLikesCount(long filmId) {
        return likesMap.getOrDefault(filmId, Collections.emptySet()).size();
    }

    private void validateFilmExists(long id) {
        if (filmStorage.getById(id).isEmpty()) {
            throw new FilmNotFoundException(id);
        }
    }

}
