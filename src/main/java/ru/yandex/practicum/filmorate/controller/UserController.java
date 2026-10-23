package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
@Validated
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAllUsers() {
        log.info("Запрос на получение всех пользователей");
        return userService.getAll();
    }

    @GetMapping("/{id}")
    public User getById(@PathVariable long id) {
        log.info("Запрос на получение пользователя с ID: {}", id);
        return userService.getById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Имя пустое, использовано имя логина: {}", user.getName());
        }

        User created = userService.add(user);
        log.info("Пользователь успешно добавлен. ID: {}, Email: {}", created.getId(), created.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping
    public User updateUser(@RequestBody User user) {
        if (user.getId() == null) {
            log.warn("Попытка обновления пользователя без ID в теле запроса. User={}", user);
            throw new IllegalArgumentException("В теле запроса не указан ID пользователя (поле id) — он обязателен для операции обновления");
        }

        User updated = userService.update(user);
        log.info("Пользователь обновлён. ID: {}, Имя: {}", updated.getId(), updated.getName());
        return updated;
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable long id, @PathVariable long friendId) {
        log.info("Пользователь {} добавляет в друзья {}", id, friendId);
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable long id, @PathVariable long friendId) {
        log.info("Пользователь {} удаляет из друзей {}", id, friendId);
        userService.removeFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable long id) {
        log.info("Запрос списка друзей для пользователя {}", id);
        return userService.getFriends(id);
    }

    @GetMapping("/{id1}/friends/common/{id2}")
    public List<User> getCommonFriends(@PathVariable long id1, @PathVariable long id2) {
        log.info("Запрос общих друзей между {} и {}", id1, id2);
        return userService.getCommonFriends(id1, id2);
    }

}