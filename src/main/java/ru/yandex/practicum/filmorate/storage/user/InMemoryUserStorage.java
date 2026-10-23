package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.exception.UserAlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import org.springframework.stereotype.Repository;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class InMemoryUserStorage implements UserStorage {

    private final ConcurrentMap<Long, User> users = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);


    @Override
    public Optional<User> getById(long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public User add(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }

        if (user.getId() != null) {
            Long newId = user.getId();
            User newUser = new User();
            newUser.setId(newId);
            newUser.setName(user.getName());
            newUser.setLogin(user.getLogin());
            newUser.setEmail(user.getEmail());
            newUser.setBirthday(user.getBirthday());

            User existing = users.putIfAbsent(newId, newUser);
            if (existing != null) {
                throw new UserAlreadyExistsException(String.format("User with id %d already exists", newId));
            }
            return newUser;
        }

        final int maxAttempts = 1_000;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            long lastId = nextId.get();
            long newId = nextId.incrementAndGet();

            if (newId < lastId) {
                throw new IllegalStateException("ID counter overflowed: no free IDs available");
            }

            User newUser = new User();
            newUser.setId(newId);
            newUser.setName(user.getName());
            newUser.setLogin(user.getLogin());
            newUser.setEmail(user.getEmail());
            newUser.setBirthday(user.getBirthday());

            User existing = users.putIfAbsent(newId, newUser);
            if (existing == null) {
                return newUser;
            }
        }

        throw new IllegalStateException(String.format(
                "Failed to generate a unique ID after %d attempts. The ID space may be exhausted or the storage is nearly full.",
                maxAttempts
        ));
    }

    @Override
    public User update(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User and its id must not be null");
        }
        if (!users.containsKey(user.getId())) {
            throw new UserNotFoundException(user.getId());
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public void delete(long id) {
        users.remove(id);
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }
}