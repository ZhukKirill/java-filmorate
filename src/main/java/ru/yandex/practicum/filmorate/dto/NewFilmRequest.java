package ru.yandex.practicum.filmorate.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class NewFilmRequest {
    private Long id;

    @NotBlank(message = "название не может быть пустым")
    private String name;

    @NotNull(message = "описание не может быть пустым")
    @Size(max = 200, message = "длина описания превышает 200 символов")
    private String description;

    @NotNull(message = "дата релиза должна быть указана")
    private LocalDate releaseDate;

    @NotNull(message = "продолжительность должна быть указана")
    @Positive(message = "продолжительность фильма в секундах должна быть больше нуля")
    private Long duration;

    @Valid
    @NotNull(message = "рейтинг должен быть указан")
    private MpaDto mpa;
    @Valid
    private List<GenreDto> genres;

    @JsonIgnore
    @AssertTrue(message = "дата релиза должна быть не раньше 28 декабря 1895 года")
    private boolean isReleaseDateValid() {
        return releaseDate == null || !releaseDate.isBefore(LocalDate.of(1895, 12, 28));
    }
}
