package ru.yandex.practicum.filmorate.exception;

public class FriendshipNotFoundException extends RuntimeException {
    public FriendshipNotFoundException(long userId, long friendId) {
        super(String.format("Дружба между пользователем %d и пользователем %d не существует", userId, friendId));

    }
}
