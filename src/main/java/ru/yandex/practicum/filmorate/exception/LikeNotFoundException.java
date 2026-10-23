package ru.yandex.practicum.filmorate.exception;

public class LikeNotFoundException extends RuntimeException {
    public LikeNotFoundException(long filmId, long userId) {
        super("Лайк фильма с id=" + filmId + " от пользователя с id=" + userId + " не найден");
    }
}