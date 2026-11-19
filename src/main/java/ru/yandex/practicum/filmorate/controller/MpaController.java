package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MpaController {

    private final MpaDbStorage mpaStorage;

    @GetMapping("/mpa")
    public List<Mpa> getAll() {
        return mpaStorage.getAll();
    }

    @GetMapping("/mpa/{id}")
    public Mpa getById(@PathVariable long id) {
        return mpaStorage.getById(id);
    }
}
