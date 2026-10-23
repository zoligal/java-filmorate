package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;
//import ru.yandex.practicum.filmorate.validation.ReleaseDate;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(of = "id")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Film {
    Long id;

    @NotBlank(message = "Название фильма не может быть пустым")
    String name;

    @NotBlank(message = "Описание фильма не может быть пустым")
    @Size(max = 200, message = "Описание не должно превышать 200 символов")
    String description;

    //@NotNull(message = "Необходимо указать дату релиза")
    //@ReleaseDate(message = "Дата релиза не может быть раньше 28.12.1895")
    LocalDate releaseDate;

    //@NotNull(message = "Необходимо указать продолжительность фильма")
    //@Positive(message = "Продолжительность должна быть положительной")
    Integer duration;

    private Set<Long> likes = new HashSet<>();
}
