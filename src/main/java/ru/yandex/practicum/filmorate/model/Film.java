package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import ru.yandex.practicum.filmorate.config.DurationSecondsDeserializer;
import ru.yandex.practicum.filmorate.config.DurationSecondsSerializer;

import java.time.Duration;
import java.time.LocalDate;

@Data
public class Film {
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;

    @JsonSerialize(using = DurationSecondsSerializer.class)
    @JsonDeserialize(using = DurationSecondsDeserializer.class)
    private Duration duration;
}
