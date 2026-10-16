package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {

    @Test
    void invalidLoginWithSpace() {
        UserService userService = Mockito.mock(UserService.class);
        UserController userController = new UserController(userService);

        User invalidUser = new User();
        invalidUser.setEmail("test@example.com");
        invalidUser.setLogin("ivan ivanov");
        invalidUser.setName("Ivan");
        invalidUser.setBirthday(LocalDate.of(1990, 5, 10));

        assertThrows(ValidationException.class, () -> userController.createUser(invalidUser));

        Mockito.verify(userService, Mockito.never()).add(Mockito.any());
    }


    @Test
    void invalidFutureBirthday() {
        UserService userService = Mockito.mock(UserService.class);
        UserController userController = new UserController(userService);

        User invalidUser = new User();
        invalidUser.setEmail("test@example.com");
        invalidUser.setLogin("future_user");
        invalidUser.setName("Future");
        invalidUser.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class, () -> userController.createUser(invalidUser));
    }

    @Test
    void emptyNameUsesLogin() {
        UserService userService = Mockito.mock(UserService.class);
        UserController userController = new UserController(userService);

        User mockCreatedUser = new User();
        mockCreatedUser.setId(1L);
        mockCreatedUser.setLogin("ivan_login");
        mockCreatedUser.setName("ivan_login");
        mockCreatedUser.setEmail("test@example.com");
        mockCreatedUser.setBirthday(LocalDate.of(1990, 5, 10));

        Mockito.when(userService.add(Mockito.any(User.class))).thenReturn(mockCreatedUser);

        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("ivan_login");
        user.setName("");
        user.setBirthday(LocalDate.of(1990, 5, 10));

        var createdUserResponse = userController.createUser(user);

        var body = createdUserResponse.getBody();
        assertNotNull(body, "Тело ответа не должно быть null");
        assertEquals("ivan_login", body.getName());
        assertEquals(201, createdUserResponse.getStatusCode().value());
    }
}