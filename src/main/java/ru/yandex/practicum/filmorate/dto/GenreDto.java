package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GenreDto {
    @NotNull(message = "id жанра должен быть указан")
    private Long id;
    private String name;
}
