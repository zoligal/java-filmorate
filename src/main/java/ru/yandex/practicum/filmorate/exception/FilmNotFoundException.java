package ru.yandex.practicum.filmorate.exception;

public class FilmNotFoundException extends RuntimeException {
    private final long id;

    public FilmNotFoundException(long id) {
        super(String.format("Film with id %d not found", id));
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
