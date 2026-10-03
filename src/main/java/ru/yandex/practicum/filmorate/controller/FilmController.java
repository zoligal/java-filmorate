package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/films")
@Slf4j
@Validated
public class FilmController {

    private Long nextId = 1L;
    private final List<Film> films = new ArrayList<>();

    @GetMapping
    public List<Film> getAllFilms() {
        log.info("Запрос на получение всех фильмов. Всего: {}", films.size());
        return new ArrayList<>(films);
    }

    @PostMapping
    public ResponseEntity<Film> createFilm(@Valid @RequestBody Film film) {
        validateFilm(film);
        film.setId(nextId++);
        films.add(film);
        log.info("Фильм успешно добавлен. ID: {}, Название: {}", film.getId(), film.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(film);
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film film) {
        if (film.getId() == null) {
            log.warn("Попытка обновления фильма без ID в теле запроса");
            throw new IllegalArgumentException(
                    "В теле запроса не указан ID фильма (поле id) — он обязателен для операции обновления"
            );
        }

        var foundFilm = films.stream()
                .filter(currentFilm -> currentFilm.getId().equals(film.getId()))
                .findFirst();

        if (foundFilm.isEmpty()) {
            log.warn("Попытка обновления фильма с несуществующим ID: {}", film.getId());
            throw new FilmNotFoundException(film.getId());
        }

        validateFilm(film);

        var existingFilm = foundFilm.get();
        existingFilm.setName(film.getName());
        existingFilm.setDescription(film.getDescription());
        existingFilm.setReleaseDate(film.getReleaseDate());
        existingFilm.setDuration(film.getDuration());

        log.info("Фильм обновлён. ID: {}, Название: {}", existingFilm.getId(), existingFilm.getName());
        return existingFilm;
    }


    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.error("Название фильма пустое");
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.error("Описание длиннее 200 символов: {}", film.getDescription().length());
            throw new ValidationException(
                    String.format("Максимальная длина описания — 200 символов (текущая длина: %d)", film.getDescription().length())
            );
        }
        LocalDate minDate = LocalDate.of(1895, 12, 28);
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(minDate)) {
            log.error("Дата релиза раньше 28.12.1895: {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (film.getDuration() == null || film.getDuration() <= 0) {
            log.error("Продолжительность фильма неположительная: {}", film.getDuration());
            throw new ValidationException(
                    String.format("Продолжительность фильма должна быть положительным числом (текущее значение: %d)", film.getDuration())
            );
        }
    }
}
