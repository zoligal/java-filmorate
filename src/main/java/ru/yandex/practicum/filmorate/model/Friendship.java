package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PRIVATE;

@Data
@EqualsAndHashCode(of = {"userId1", "userId2"})
@FieldDefaults(level = PRIVATE, makeFinal = false)
public class Friendship {
    private Long userId1;
    private Long userId2;
    private FriendshipStatus status;
}
