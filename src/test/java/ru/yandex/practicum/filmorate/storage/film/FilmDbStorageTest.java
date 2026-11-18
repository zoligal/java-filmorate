package ru.yandex.practicum.filmorate.storage.film;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaRowMapper;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, MpaDbStorage.class, GenreDbStorage.class,
        MpaRowMapper.class, GenreRowMapper.class, UserDbStorage.class})
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    FilmDbStorageTest(@Autowired FilmDbStorage filmStorage,
                      @Autowired UserDbStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    private Film makeFilm() {
        Film film = new Film();
        film.setName("Кино");
        film.setDescription("Тестовое описание");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Mpa mpa = new Mpa();
        mpa.setId(1L);
        film.setMpa(mpa);

        Genre genre = new Genre();
        genre.setId(1L);
        film.setGenres(List.of(genre));

        return film;
    }

    private User makeUser(String login) {
        User user = new User();
        user.setEmail(login + "@example.com");
        user.setLogin(login);
        user.setName(login);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    @Test
    void addShouldSaveAndAssignId() {
        Film film = filmStorage.add(makeFilm());

        assertThat(film.getId()).isNotNull().isPositive();
    }

    @Test
    void getByIdShouldReturnEmptyForMissingFilm() {
        assertThat(filmStorage.getById(9999)).isEmpty();
    }

    @Test
    void getByIdShouldReturnFilmWithMpaAndGenres() {
        Film saved = filmStorage.add(makeFilm());

        Optional<Film> found = filmStorage.getById(saved.getId());

        assertThat(found).isPresent();
        Film film = found.get();
        assertThat(film.getMpa()).isNotNull();
        assertThat(film.getMpa().getId()).isEqualTo(1L);
        assertThat(film.getMpa().getName()).isNotBlank();
        assertThat(film.getGenres()).extracting(Genre::getId).containsExactly(1L);
    }

    @Test
    void updateShouldChangeFieldsAndGenres() {
        Film saved = filmStorage.add(makeFilm());
        saved.setName("Новое название");
        saved.setDuration(90);

        Genre newGenre = new Genre();
        newGenre.setId(2L);
        saved.setGenres(List.of(newGenre));

        Film updated = filmStorage.update(saved);
        Optional<Film> reloaded = filmStorage.getById(saved.getId());

        assertThat(updated).isNotNull();
        assertThat(reloaded)
                .isPresent()
                .hasValueSatisfying(f -> {
                    assertThat(f.getName()).isEqualTo("Новое название");
                    assertThat(f.getDuration()).isEqualTo(90);
                    assertThat(f.getGenres()).extracting(Genre::getId).containsExactly(2L);
                });
    }

    @Test
    void deleteShouldRemoveFilm() {
        Film saved = filmStorage.add(makeFilm());

        filmStorage.delete(saved.getId());

        assertThat(filmStorage.getById(saved.getId())).isEmpty();
    }

    @Test
    void getAllShouldReturnAllFilms() {
        filmStorage.add(makeFilm());
        Film second = makeFilm();
        second.setName("Второй фильм");
        filmStorage.add(second);

        assertThat(filmStorage.getAll()).hasSize(2);
    }

    @Test
    void addLikeShouldNotDuplicate() {
        Film film = filmStorage.add(makeFilm());
        User user = userStorage.add(makeUser("vasya"));

        filmStorage.addLike(film.getId(), user.getId());
        filmStorage.addLike(film.getId(), user.getId());

        assertThat(filmStorage.getById(film.getId()).orElseThrow().getLikes())
                .containsExactly(user.getId());
    }

    @Test
    void removeLikeShouldRemoveLike() {
        Film film = filmStorage.add(makeFilm());
        User u1 = userStorage.add(makeUser("vasya"));
        User u2 = userStorage.add(makeUser("petya"));

        filmStorage.addLike(film.getId(), u1.getId());
        filmStorage.addLike(film.getId(), u2.getId());

        filmStorage.removeLike(film.getId(), u1.getId());
        filmStorage.removeLike(film.getId(), u1.getId());

        assertThat(filmStorage.getById(film.getId()).orElseThrow().getLikes())
                .containsExactly(u2.getId());
    }

    @Test
    void getPopularShouldSortByLikesDesc() {
        Film f1 = filmStorage.add(makeFilm());
        Film f2 = makeFilm();
        f2.setName("Второй");
        Film saved2 = filmStorage.add(f2);
        Film f3 = makeFilm();
        f3.setName("Третий");
        Film saved3 = filmStorage.add(f3);

        User u1 = userStorage.add(makeUser("vasya"));
        User u2 = userStorage.add(makeUser("petya"));
        User u3 = userStorage.add(makeUser("sasha"));

        filmStorage.addLike(saved2.getId(), u1.getId());
        filmStorage.addLike(saved2.getId(), u2.getId());
        filmStorage.addLike(saved2.getId(), u3.getId());
        filmStorage.addLike(saved3.getId(), u1.getId());
        filmStorage.addLike(saved3.getId(), u2.getId());

        assertThat(filmStorage.getPopular(3))
                .extracting(Film::getId)
                .containsExactly(saved2.getId(), saved3.getId(), f1.getId());
    }
}
