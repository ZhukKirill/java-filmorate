package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.ToString;
import ru.yandex.practicum.filmorate.config.DurationSecondsDeserializer;
import ru.yandex.practicum.filmorate.config.DurationSecondsSerializer;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;

    @JsonSerialize(using = DurationSecondsSerializer.class)
    @JsonDeserialize(using = DurationSecondsDeserializer.class)
    private Duration duration;

    @ToString.Exclude
    private final Set<Long> likes = new HashSet<>();

    public int getLikesCount() {
        return likes.size();
    }

    public void addLike(Long userId) {
        likes.add(userId);
    }

    public void deleteLike(Long userId) {
        likes.remove(userId);
    }
}
