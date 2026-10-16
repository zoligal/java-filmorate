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
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/films")
@Slf4j
@Validated
public class FilmController {

    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public List<Film> getAllFilms() {
        log.info("Запрос на получение всех фильмов");
        return filmService.getAll();
    }

    @PostMapping
    public ResponseEntity<Film> createFilm(@Valid @RequestBody Film film) {
        validateFilm(film);

        Film created = filmService.add(film);
        log.info("Фильм успешно добавлен. ID: {}, Название: {}", created.getId(), created.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film film) {
        if (film.getId() == null) {
            log.warn("Попытка обновления фильма без ID в теле запроса");
            throw new IllegalArgumentException("В теле запроса не указан ID фильма (поле id) — он обязателен для операции обновления");
        }

        validateFilm(film);

        Film updated = filmService.update(film);
        log.info("Фильм обновлён. ID: {}, Название: {}", updated.getId(), updated.getName());
        return updated;
    }

    @GetMapping("/{id}")
    public Film getById(@PathVariable long id) {
        log.info("Запрос на получение фильма с ID: {}", id);
        return filmService.getById(id)
                .orElseThrow(() -> new FilmNotFoundException(id));
    }

    @PutMapping("/{id}/likes/{userId}")
    public void addLike(@PathVariable long id, @PathVariable long userId) {
        log.info("Пользователь {} ставит лайк фильму {}", userId, id);
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/likes/{userId}")
    public void removeLike(@PathVariable long id, @PathVariable long userId) {
        log.info("Пользователь {} убирает лайк с фильма {}", userId, id);
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        log.info("Запрос топ-{} популярных фильмов", count);
        if (count <= 0) {
            throw new ValidationException("Параметр count должен быть положительным числом");
        }
        return filmService.getTopNFilms(count);
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
