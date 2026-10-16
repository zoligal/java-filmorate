package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;

class FilmControllerTest {

    @Test
    void emptyNameThrows() {
        FilmService filmService = Mockito.mock(FilmService.class);
        FilmController controller = new FilmController(filmService);

        Film mockFilm = new Film();
        mockFilm.setId(1L);
        mockFilm.setName("Mocked Film");
        mockFilm.setDescription("test");
        mockFilm.setReleaseDate(LocalDate.now());
        mockFilm.setDuration(90);
        Mockito.when(filmService.add(Mockito.any(Film.class))).thenReturn(mockFilm);

        Film invalidFilm = new Film();
        invalidFilm.setName("");
        invalidFilm.setDescription("тест");
        invalidFilm.setReleaseDate(LocalDate.now());
        invalidFilm.setDuration(90);

        assertThrows(ValidationException.class, () -> controller.createFilm(invalidFilm));
    }

    @Test
    void oldReleaseDateThrows() {
        FilmService filmService = Mockito.mock(FilmService.class);
        FilmController controller = new FilmController(filmService);

        Film mockFilm = new Film();
        mockFilm.setId(2L);
        mockFilm.setName("Old Film");
        mockFilm.setDescription("very old");
        mockFilm.setReleaseDate(LocalDate.of(1895, 12, 28));
        mockFilm.setDuration(60);
        Mockito.when(filmService.add(Mockito.any(Film.class))).thenReturn(mockFilm);

        Film invalidFilm = new Film();
        invalidFilm.setName("Старый фильм");
        invalidFilm.setDescription("очень старый");
        invalidFilm.setReleaseDate(LocalDate.of(1800, 1, 1));
        invalidFilm.setDuration(60);

        assertThrows(ValidationException.class, () -> controller.createFilm(invalidFilm));
    }

    @Test
    void nonPositiveDurationThrows() {
        FilmService filmService = Mockito.mock(FilmService.class);
        FilmController controller = new FilmController(filmService);

        Film mockFilm = new Film();
        mockFilm.setId(3L);
        mockFilm.setName("Short Film");
        mockFilm.setDescription("almost nothing");
        mockFilm.setReleaseDate(LocalDate.now());
        mockFilm.setDuration(10);
        Mockito.when(filmService.add(Mockito.any(Film.class))).thenReturn(mockFilm);

        Film invalidFilm = new Film();
        invalidFilm.setName("Короткий фильм");
        invalidFilm.setDescription("почти нет");
        invalidFilm.setReleaseDate(LocalDate.now());
        invalidFilm.setDuration(-10);

        assertThrows(ValidationException.class, () -> controller.createFilm(invalidFilm));
    }
}
