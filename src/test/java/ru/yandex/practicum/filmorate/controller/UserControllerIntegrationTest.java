package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;

import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.Film;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void invalidEmailReturnsBadRequest() throws Exception {
        User invalidUser = new User();
        invalidUser.setLogin("test");
        invalidUser.setEmail("test.com");
        invalidUser.setBirthday(LocalDate.now());

        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("Email должен быть корректным"));
    }

    @Test
    void releaseDateInvalid() throws Exception {
        Film oldFilm = new Film();
        oldFilm.setName("Old Movie");
        oldFilm.setDescription("Too old");
        oldFilm.setReleaseDate(LocalDate.of(1800, 1, 1));
        oldFilm.setDuration(90);

        mockMvc.perform(MockMvcRequestBuilders.post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(oldFilm)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.releaseDate").value(
                        "Дата релиза не может быть раньше 28.12.1895"));
    }


    @Test
    void validUserReturnsCreated() throws Exception {
        User validUser = new User();
        validUser.setLogin("valid-user");
        validUser.setEmail("valid-user@example.com");
        validUser.setBirthday(LocalDate.of(1990, 1, 1));

        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.login").value("valid-user"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("valid-user@example.com"));
    }
}
