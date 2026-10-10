package ru.yandex.practicum.filmorate.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class FilmDto {

    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Long duration;
    private MpaDto mpa;
    private List<GenreDto> genres;
}
