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

import ru.yandex.practicum.filmorate.model.Film;

@SpringBootTest
@AutoConfigureMockMvc
class FilmControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Film createInvalidNameFilm() {
        Film film = new Film();
        film.setName("");
        film.setDescription("Test description");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(90);
        return film;
    }

    private Film createOldReleaseDateFilm() {
        Film film = new Film();
        film.setName("Old Movie");
        film.setDescription("Too old movie");
        film.setReleaseDate(LocalDate.of(1800, 1, 1));
        film.setDuration(90);
        return film;
    }

    private Film createNonPositiveDurationFilm() {
        Film film = new Film();
        film.setName("Short Movie");
        film.setDescription("Too short");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(-10);
        return film;
    }

    private Film createValidFilm() {
        Film film = new Film();
        film.setName("Valid Movie");
        film.setDescription("Good description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        return film;
    }

    @Test
    void invalidNameReturnsBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createInvalidNameFilm())))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.details.name").exists());
    }

    @Test
    void oldReleaseDateReturnsBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOldReleaseDateFilm())))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.details.releaseDate").exists());
    }

    @Test
    void nonPositiveDurationReturnsBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createNonPositiveDurationFilm())))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.details.duration").exists());
    }

    @Test
    void validFilmReturnsCreated() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidFilm())))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Valid Movie"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());
    }
}