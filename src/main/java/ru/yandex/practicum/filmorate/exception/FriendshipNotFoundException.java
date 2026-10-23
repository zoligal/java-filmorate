package ru.yandex.practicum.filmorate.exception;

public class FriendshipNotFoundException extends RuntimeException {
    public FriendshipNotFoundException(long userId, long friendId) {
        super(String.format("Friendship between user %d and %d does not exist", userId, friendId));

    }
}