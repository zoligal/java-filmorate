package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;

class FilmControllerTest {

    @Test
    void emptyNameThrows() {
        FilmController controller = new FilmController();
        Film invalidFilm = new Film();
        invalidFilm.setName("");
        invalidFilm.setDescription("тест");
        invalidFilm.setReleaseDate(LocalDate.now());
        invalidFilm.setDuration(90);

        assertThrows(ValidationException.class, () -> controller.createFilm(invalidFilm));
    }

    @Test
    void oldReleaseDateThrows() {
        FilmController controller = new FilmController();
        Film invalidFilm = new Film();
        invalidFilm.setName("Старый фильм");
        invalidFilm.setDescription("очень старый");
        invalidFilm.setReleaseDate(LocalDate.of(1800, 1, 1));
        invalidFilm.setDuration(60);

        assertThrows(ValidationException.class, () -> controller.createFilm(invalidFilm));
    }

    @Test
    void nonPositiveDurationThrows() {
        FilmController controller = new FilmController();
        Film invalidFilm = new Film();
        invalidFilm.setName("Короткий фильм");
        invalidFilm.setDescription("почти нет");
        invalidFilm.setReleaseDate(LocalDate.now());
        invalidFilm.setDuration(-10);

        assertThrows(ValidationException.class, () -> controller.createFilm(invalidFilm));
    }
}
