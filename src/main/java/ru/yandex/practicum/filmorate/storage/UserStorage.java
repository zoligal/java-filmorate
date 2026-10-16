package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    User add(User user);

    Optional<User> getById(long id);

    User update(User user);

    void delete(long id);

    List<User> getAll();
}

