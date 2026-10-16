package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.practicum.filmorate.exception.ValidationException;


import java.util.List;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
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
        return filmStorage.update(film);
    }

    public Optional<Film> getById(long id) {
        return filmStorage.getById(id);
    }

    public void addLike(long filmId, long userId) {
        log.debug("Ставим лайк: filmId={}, userId={}", filmId, userId);

        validateFilmExists(filmId);

        Set<Long> likes = likesMap.computeIfAbsent(filmId, k -> new HashSet<>());

        boolean isNewLike = likes.add(userId);

        if (isNewLike) {
            log.debug("Лайк успешно поставлен: filmId={}, userId={}", filmId, userId);
        } else {
            log.debug("Пользователь уже поставил лайк этому фильму: filmId={}, userId={}", filmId, userId);
        }
    }

    public void removeLike(long filmId, long userId) {
        log.debug("Запрос на удаление лайка: filmId={}, userId={}", filmId, userId);

        validateFilmExists(filmId);

        Set<Long> usersWhoLiked = likesMap.get(filmId);
        if (usersWhoLiked == null || !usersWhoLiked.contains(userId)) {
            log.debug("Лайк не найден для удаления: filmId={}, userId={}. Лайка не было.", filmId, userId);
            return;
        }

        usersWhoLiked.remove(userId);
        log.debug("Лайк удалён: filmId={}, userId={}", filmId, userId);

        if (usersWhoLiked.isEmpty()) {
            log.debug("У фильма больше нет лайков, удаляем запись из likesMap: filmId={}", filmId);
            likesMap.remove(filmId);
        }
    }

    public List<Film> getTop10Films() {
        return filmStorage.getAll().stream()
                .sorted((f1, f2) -> {
                    int likes1 = getLikesCount(f1.getId());
                    int likes2 = getLikesCount(f2.getId());
                    return Integer.compare(likes2, likes1);
                })
                .limit(10)
                .collect(Collectors.toList());
    }

    public List<Film> getTopNFilms(int count) {
        log.debug("Запрос топ-N фильмов: count={}", count);

        if (count <= 0) {
            log.warn("Некорректный параметр count={}. Должен быть > 0", count);
            throw new ValidationException("Параметр count должен быть положительным числом");
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
