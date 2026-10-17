package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(of = "id")
public class User {
    private Long id;
    private String name;

    @NotBlank(message = "Электронная почта не может быть пустой")
    @Email(message = "Email должен быть корректным")
    private String email;

    @NotBlank(message = "Логин не может быть пустым")
    @Pattern(regexp = "^\\S+$", message = "Логин не может содержать пробелы")
    private String login;

    @NotNull(message = "Дата рождения должна быть указана")
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;
}
