package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
@Validated
public class UserController {

    private Long nextId = 1L;
    private final List<User> users = new ArrayList<>();

    @GetMapping
    public List<User> getAllUsers() {
        log.info("Запрос на получение всех пользователей. Всего: {}", users.size());
        return new ArrayList<>(users);
    }

    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        validateUser(user);

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Имя пустое, использовано имя логина: {}", user.getName());
        }

        user.setId(nextId++);
        users.add(user);
        log.info("Пользователь успешно добавлен. ID: {}, Email: {}", user.getId(), user.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PutMapping("/{id}")
    public User updateUser(@Valid @RequestBody User user, @PathVariable Long userId) {
        if (!userId.equals(user.getId())) {
            log.warn("ID в URL и в теле запроса не совпадают: {} vs {}", userId, user.getId());
            throw new IllegalArgumentException("ID в URL и теле запроса должны совпадать");
        }

        var foundUserOptional = users.stream()
                .filter(currentUser -> currentUser.getId().equals(userId))
                .findFirst();

        if (foundUserOptional.isEmpty()) {
            log.warn("Попытка обновления пользователя с несуществующим ID: {}", userId);
            throw new UserNotFoundException(userId);
        }

        validateUser(user);

        var existingUser = foundUserOptional.get();

        String nameToSet = user.getName();
        if (nameToSet == null || nameToSet.isBlank()) {
            nameToSet = user.getLogin();
            log.debug("При обновлении имя пустое, использовано имя логина: {}", nameToSet);
        }
        existingUser.setName(nameToSet);
        existingUser.setEmail(user.getEmail());
        existingUser.setLogin(user.getLogin());
        existingUser.setBirthday(user.getBirthday());

        log.info("Пользователь обновлён. ID: {}", existingUser.getId());
        return existingUser;
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.error("Некорректный email: {}", user.getEmail());
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.error("Некорректный логин: {}", user.getLogin());
            throw new ValidationException("Логин не может быть пустым или содержать пробелы");
        }
        if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Дата рождения в будущем или отсутствует: {}", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}
