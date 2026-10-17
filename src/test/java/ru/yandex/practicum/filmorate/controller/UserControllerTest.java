package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {

    @Test
    void invalidLoginWithSpace() {
        UserController userController = new UserController();
        User invalidUser = new User();
        invalidUser.setEmail("test@example.com");
        invalidUser.setLogin("ivan ivanov");
        invalidUser.setName("Ivan");
        invalidUser.setBirthday(LocalDate.of(1990, 5, 10));

        assertThrows(ValidationException.class, () -> userController.createUser(invalidUser));
    }

    @Test
    void invalidFutureBirthday() {
        UserController userController = new UserController();
        User invalidUser = new User();
        invalidUser.setEmail("test@example.com");
        invalidUser.setLogin("futureuser");
        invalidUser.setName("Future");
        invalidUser.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class, () -> userController.createUser(invalidUser));
    }

    @Test
    void emptyNameUsesLogin() {
        UserController userController = new UserController();
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("ivanlogin");
        user.setName("");
        user.setBirthday(LocalDate.of(1990, 5, 10));

        var createdUserResponse = userController.createUser(user);

        assertEquals("ivanlogin", createdUserResponse.getBody().getName());
        assertEquals(201, createdUserResponse.getStatusCodeValue());
    }
}
