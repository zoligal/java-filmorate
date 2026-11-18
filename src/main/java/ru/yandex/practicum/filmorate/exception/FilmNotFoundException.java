package ru.yandex.practicum.filmorate.exception;

public class FilmNotFoundException extends RuntimeException {

    public FilmNotFoundException(long filmId) {
        super(String.format("Фильм с id %d не найден", filmId));
    }

}