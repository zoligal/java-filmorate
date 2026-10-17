package ru.yandex.practicum.filmorate.controller;

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
    void invalidEmailReturnsBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserWithInvalidEmail())))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.details.email").exists());
    }

    @Test
    void emptyLoginReturnsBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserWithEmptyLogin())))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.details.login").exists());
    }

    @Test
    void futureBirthdayReturnsBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserWithFutureBirthday())))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.details.birthday").exists());
    }

    @Test
    void validUserReturnsCreated() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidUser())))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.login").value("valid-user"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("valid-user@example.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());
    }
}
