package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
public class UserService {

    private final UserStorage userStorage;
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> getAll() {
        return userStorage.getAll();
    }

    public User add(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Пользователь не должен быть равен null");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.add(user);
    }

    public User update(User user) {
        if (user.getId() == null) {
            throw new ValidationException("Для обновления требуется идентификатор пользователя");
        }

        User existing = userStorage.getById(user.getId())
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("Пользователь с идентификатором %d не найден", user.getId())));

        existing.setName(user.getName());
        existing.setLogin(user.getLogin());
        existing.setEmail(user.getEmail());
        existing.setBirthday(user.getBirthday());

        return userStorage.update(existing);
    }

    public User getById(long id) {
        return userStorage.getById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public void addFriend(long userId, long friendId) {
        if (userId == friendId) {
            throw new IllegalArgumentException("Пользователь не может добавить себя в друзья");
        }
        userStorage.getByIdOrFail(userId);
        userStorage.getByIdOrFail(friendId);
        userStorage.addFriend(userId, friendId);
    }

    public void removeFriend(long userId, long friendId) {
        userStorage.getByIdOrFail(userId);
        userStorage.getByIdOrFail(friendId);
        userStorage.removeFriend(userId, friendId);
    }

    public List<User> getFriends(long userId) {
        userStorage.getByIdOrFail(userId);
        return userStorage.getFriends(userId);
    }

    public List<User> getCommonFriends(long userId1, long userId2) {
        userStorage.getByIdOrFail(userId1);
        userStorage.getByIdOrFail(userId2);
        return userStorage.getCommonFriends(userId1, userId2);
    }

    public void deleteUser(long id) {
        userStorage.getByIdOrFail(id);
        userStorage.delete(id);
    }
}
