package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

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
        user.setEmail("test.com");
        user.setBirthday(LocalDate.now());
        return user;
    }

    private User createUserWithEmptyLogin() {
        User user = new User();
        user.setLogin("");
        user.setEmail("user@example.com");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    private User createUserWithFutureBirthday() {
        User user = new User();
        user.setLogin("future-user");
        user.setEmail("future-user@example.com");
        user.setBirthday(LocalDate.now().plusDays(1));
        return user;
    }

    @Test
    void invalidEmailReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserWithInvalidEmail())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.email").exists());
    }

    @Test
    void emptyLoginReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserWithEmptyLogin())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.login").exists());
    }

    @Test
    void futureBDayBadRequest() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserWithFutureBirthday())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.birthday").exists());
    }

}
