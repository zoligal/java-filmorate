package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FriendshipNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
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
        users.sort((u1, u2) -> Long.compare(u1.getId(), u2.getId()));
        return users;
    }

    public User add(User user) {
        return userStorage.add(user);
    }

    public User update(User user) {
        return userStorage.update(user);
    }

    public Optional<User> getById(long id) {
        return userStorage.getById(id);
    }

    public void addFriend(long userId, long friendId) {
        log.debug("Попытка добавить дружбу: userId={}, friendId={}", userId, friendId);
        if (userId == friendId) {
            log.warn("Попытка добавить пользователя в друзья самому себе: userId={}", userId);
            throw new IllegalArgumentException("User cannot be friend with themselves");
        }
        validateUserExists(userId);
        validateUserExists(friendId);

        friendsMap.computeIfAbsent(userId, k -> new HashSet<>()).add(friendId);
        friendsMap.computeIfAbsent(friendId, k -> new HashSet<>()).add(userId);
    }

    public void removeFriend(long userId, long friendId) {
        log.debug("Попытка удалить дружбу: userId={}, friendId={}", userId, friendId);
        if (!hasFriendship(userId, friendId)) {
            log.warn("Попытка удалить несуществующую дружбу: userId={}, friendId={}", userId, friendId);
            throw new FriendshipNotFoundException(userId, friendId);
        }

        Set<Long> userFriends = friendsMap.get(userId);
        Set<Long> friendFriends = friendsMap.get(friendId);

        if (userFriends != null) userFriends.remove(friendId);
        if (friendFriends != null) friendFriends.remove(userId);

        cleanupIfEmpty(userId);
        cleanupIfEmpty(friendId);
        log.trace("Выполнена очистка пустых списков друзей после удаления: userId={}, friendId={}", userId, friendId);
    }

    public List<User> getFriends(long userId) {
        validateUserExists(userId);
        Set<Long> friendIds = friendsMap.getOrDefault(userId, Collections.emptySet());
        return friendIds.stream()
                .map(userStorage::getById)
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
                .map(userStorage::getById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private void validateUserExists(long id) {
        if (userStorage.getById(id).isEmpty()) {
            throw new UserNotFoundException(id);
        }
    }

    private boolean hasFriendship(long userId, long friendId) {
        Set<Long> friends = friendsMap.getOrDefault(userId, Collections.emptySet());
        return friends.contains(friendId);
    }

    private void cleanupIfEmpty(long userId) {
        Set<Long> set = friendsMap.get(userId);
        if (set != null && set.isEmpty()) {
            friendsMap.remove(userId);
        }
    }
}