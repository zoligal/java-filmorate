package ru.yandex.practicum.filmorate.exception;

public class LikeNotFoundException extends RuntimeException {
    public LikeNotFoundException(long filmId, long userId) {
        super(String.format("Лайк фильма с id=%d от пользователя с id=%d не найден", filmId, userId));
    }
}