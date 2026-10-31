package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FriendshipNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserStorage userStorage;
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final ConcurrentHashMap<Long, Set<Long>> friendsMap = new ConcurrentHashMap<>();

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> getAll() {
        List<User> users = userStorage.getAll();
        users.sort(Comparator.comparingLong(User::getId));
        return users;
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

    /*public User update(User user) {
        return userStorage.update(user);
    }*/

    public User update(User user) {
        if (user.getId() == null) {
            throw new ValidationException("Для обновления требуется идентификатор пользователя");
        }

        Optional<User> existingOpt = userStorage.getById(user.getId());
        if (existingOpt.isEmpty()) {
            throw new UserNotFoundException("Пользователь с идентификатором " + user.getId() + " не найден");
        }
        User existing = existingOpt.get();

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
        userStorage.getById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        userStorage.getById(friendId)
                .orElseThrow(() -> new UserNotFoundException(friendId));

        friendsMap.computeIfAbsent(userId, k -> new HashSet<>()).add(friendId);
        log.debug("Дружба добавлена: {} -> {}", userId, friendId);
    }

    public void removeFriend(long userId, long friendId) {
        log.debug("Попытка удалить дружбу: userId={}, friendId={}", userId, friendId);

        if (!hasFriendship(userId, friendId)) {
            log.warn("Попытка удалить несуществующую дружбу: userId={}, friendId={}. Выбрасываем исключение.", userId, friendId);
            throw new FriendshipNotFoundException(
                    String.format("Дружба между пользователем %d и пользователем %d не найдена", userId, friendId)
            );
        }

        Set<Long> userFriends = friendsMap.get(userId);
        if (userFriends != null) {
            userFriends.remove(friendId);
            if (userFriends.isEmpty()) {
                friendsMap.remove(userId);
            }
        }

        log.trace("Выполнена очистка пустых списков друзей после удаления: userId={}", userId);
    }

    public List<User> getFriends(long userId) {
        validateUserExists(userId);
        Set<Long> friendIds = friendsMap.getOrDefault(userId, Collections.emptySet());
        return friendIds.stream()
                .map(id -> userStorage.getById(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(long userId1, long userId2) {
        log.debug("Запрос общих друзей: userId={}, otherId={}", userId1, userId2);
        validateUserExists(userId1);
        validateUserExists(userId2);

        Set<Long> friends1 = friendsMap.getOrDefault(userId1, Collections.emptySet());
        Set<Long> friends2 = friendsMap.getOrDefault(userId2, Collections.emptySet());

        Set<Long> commonIds = new HashSet<>(friends1);
        commonIds.retainAll(friends2);

        return commonIds.stream()
                .map(id -> userStorage.getById(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private void validateUserExists(long id) {
        if (userStorage.getById(id).isEmpty()) {
            throw new UserNotFoundException("Пользователь с ID " + id + " не найден");
        }
    }

    private boolean hasFriendship(long userId, long friendId) {
        Set<Long> friends = friendsMap.getOrDefault(userId, Collections.emptySet());
        return friends.contains(friendId);
    }
}