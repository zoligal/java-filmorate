package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GenreController {

    private final GenreDbStorage genreStorage;

    @GetMapping("/genres")
    public List<Genre> getAll() {
        return genreStorage.getAll();
    }

    @GetMapping("/genres/{id}")
    public Genre getById(@PathVariable long id) {
        return genreStorage.getById(id);
    }
}
