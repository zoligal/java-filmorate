package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.constant.ApiPathConstants;

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

    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        User created = userService.add(user);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public User getById(@PathVariable long id) {
        return userService.getById(id);
    }

    @PutMapping
    public ResponseEntity<User> updateUser(@Valid @RequestBody User user) {
        log.info("Update called with id={}", user.getId());
        User updated = userService.update(user);
        return ResponseEntity.ok(updated);
    }

    @PutMapping(ApiPathConstants.SEGMENT_FRIENDS_PAIR)
    public ResponseEntity<Void> addFriend(@PathVariable long id, @PathVariable long friendId) {
        log.info("Пользователь {} добавляет в друзья {}", id, friendId);
        userService.addFriend(id, friendId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(ApiPathConstants.SEGMENT_FRIENDS_PAIR)
    public ResponseEntity<Void> removeFriend(@PathVariable long id, @PathVariable long friendId) {
        log.info("Пользователь {} удаляет из друзей {}", id, friendId);
        userService.removeFriend(id, friendId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable long id) {
        log.info("Удаление пользователя с ID: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(ApiPathConstants.SEGMENT_FRIENDS)
    public List<User> getFriends(@PathVariable long id) {
        return userService.getFriends(id);
    }

    @GetMapping(ApiPathConstants.SEGMENT_COMMON_FRIENDS)
    public List<User> getCommonFriends(@PathVariable long id, @PathVariable long otherId) {
        return userService.getCommonFriends(id, otherId);
    }
}
