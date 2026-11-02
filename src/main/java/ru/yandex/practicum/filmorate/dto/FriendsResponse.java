package ru.yandex.practicum.filmorate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;
import ru.yandex.practicum.filmorate.model.User;

@Data
@AllArgsConstructor
public class FriendsResponse {
    private List<User> friends;
    private int friendCount;
}
