package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;

import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    private User testUser1;
    private User testUser2;

    @BeforeEach
    void setUp() {
        System.out.println("--- Начало подготовки теста ---");

        User u1Template = new User();
        u1Template.setLogin("test-user-1");
        u1Template.setEmail("u1@test.com");
        u1Template.setBirthday(LocalDate.of(1990, 1, 1));

        testUser1 = userService.add(u1Template);

        if (testUser1 == null || testUser1.getId() == null) {
            throw new IllegalStateException("CRITICAL ERROR: UserService.add returned null or ID is null. Check your InMemoryUserStorage implementation.");
        }
        System.out.println("Пользователь 1 создан: ID=" + testUser1.getId());

        User u2Template = new User();
        u2Template.setLogin("test-user-2");
        u2Template.setEmail("u2@test.com");
        u2Template.setBirthday(LocalDate.of(1990, 2, 2));

        testUser2 = userService.add(u2Template);

        if (testUser2 == null || testUser2.getId() == null) {
            throw new IllegalStateException("CRITICAL ERROR: UserService.add returned null for second user.");
        }
        System.out.println("Пользователь 2 создан: ID=" + testUser2.getId());

        userService.addFriend(testUser1.getId(), testUser2.getId());
        System.out.println("Дружба между " + testUser1.getId() + " и " + testUser2.getId() + " установлена.");
        System.out.println("--- Подготовка теста завершена ---");
    }

    @Test
    void deleteFriendshipAndTryAgainReturns404() throws Exception {
        long id1 = testUser1.getId();
        long id2 = testUser2.getId();

        System.out.println("Тест: Удаляем дружбу между " + id1 + " и " + id2);

        mockMvc.perform(MockMvcRequestBuilders.delete("/users/" + id1 + "/friends/" + id2)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNoContent())
                .andReturn();

        System.out.println("Дружба удалена. Теперь пробуем удалить несуществующую.");

        mockMvc.perform(MockMvcRequestBuilders.delete("/users/" + id1 + "/friends/" + id2)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Not Found"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists());

        System.out.println("Тест пройден: получили 404 при повторном удалении.");
    }
}

/*package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;

import ru.yandex.practicum.filmorate.model.User;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private User createUserWithInvalidEmail() {
        User user = new User();
        user.setLogin("test");
        user.setEmail("test.com"); // некорректный email
        user.setBirthday(LocalDate.now());
        return user;
    }

    private User createUserWithEmptyLogin() {
        User user = new User();
        user.setLogin(""); // пустой логин
        user.setEmail("user@example.com");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    private User createUserWithFutureBirthday() {
        User user = new User();
        user.setLogin("future-user");
        user.setEmail("future-user@example.com");
        user.setBirthday(LocalDate.now().plusDays(1)); // будущая дата
        return user;
    }

    private User createValidUser() {
        User user = new User();
        user.setLogin("valid-user");
        user.setEmail("valid-user@example.com");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    @Test
    void emptyLoginReturnsBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserWithEmptyLogin())))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.details.login").exists());
    }

}*/