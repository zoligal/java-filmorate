package ru.yandex.practicum.filmorate.exception;

public class FilmNotFoundException extends RuntimeException {
    private final long id;

    public FilmNotFoundException(long id) {
        super(String.format("Фильм с id %d не найден", id));
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
