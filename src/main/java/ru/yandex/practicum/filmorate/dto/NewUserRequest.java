package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Data;

import java.time.LocalDate;

@Data
public class NewUserRequest {
    private Long id;
    @NotNull
    @Email(message = "электронная почта должна быть указан и иметь вид: user@domain.tld")
    private String email;
    @NotEmpty(message = "логин не может быть пустым и содержать пробелы")
    private String login;
    private String name;

    @Past(message = "дата рождения не может быть в будущем")
    private LocalDate birthday;
}
