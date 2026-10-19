package ru.yandex.practicum.filmorate.exception;

public class InvalidCountException extends RuntimeException {
    public InvalidCountException(String message) {
        super(message);
    }
}
