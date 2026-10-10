package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MpaDto {
    @NotNull(message = "id рейтинга должен быть указан")
    Long id;
    String name;
}
