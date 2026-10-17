package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

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
        Film created = filmService.add(film);
        log.info("Фильм успешно добавлен. ID: {}, Название: {}", created.getId(), created.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public Film updateFilm(@PathVariable long id, @RequestBody Film film) {
        film.setId(id);
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

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable long id, @PathVariable long userId) {
        log.info("Пользователь {} ставит лайк фильму {}", userId, id);
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable long id, @PathVariable long userId) {
        log.info("Пользователь {} убирает лайк с фильма {}", userId, id);
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        log.info("Запрос топ-{} популярных фильмов", count);
        if (count <= 0) {
            throw new IllegalArgumentException("Параметр count должен быть положительным числом");
        }
        return filmService.getTopNFilms(count);
    }

}
