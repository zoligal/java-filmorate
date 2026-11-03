package ru.yandex.practicum.filmorate.constant;

public final class ApiPathConstants {
    private ApiPathConstants() {}

    public static final String SEGMENT_LIKE = "/{id}/like/{userId}";
    public static final String SEGMENT_FRIENDS_PAIR = "/{id}/friends/{friendId}";
    public static final String SEGMENT_FRIENDS = "/{id}/friends";
    public static final String SEGMENT_COMMON_FRIENDS = "/{id}/friends/common/{otherId}";
}
