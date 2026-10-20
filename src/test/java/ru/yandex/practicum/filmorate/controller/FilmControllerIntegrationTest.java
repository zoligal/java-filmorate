package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

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
        // По ТЗ Filmorate минимальная дата — 1895-12-28
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
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createInvalidNameFilm())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.name").exists());
    }

    @Test
    void oldReleaseDateReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOldReleaseDateFilm())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.releaseDate").exists());
    }

    @Test
    void nonPosDurBadRequest() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createNonPositiveDurationFilm())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.duration").exists());
    }

    @Test
    void validFilmReturnsCreated() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidFilm())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Valid Movie"))
                .andExpect(jsonPath("$.id").exists());
    }

}
