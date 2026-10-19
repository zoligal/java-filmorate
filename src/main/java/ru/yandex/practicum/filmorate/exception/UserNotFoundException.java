package ru.yandex.practicum.filmorate.exception;

public class UserNotFoundException extends RuntimeException {
    private final long id;

    public UserNotFoundException(long id) {
        super(String.format("Пользователь с id %d не найден", id));
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
