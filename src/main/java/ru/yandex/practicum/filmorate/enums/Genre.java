package ru.yandex.practicum.filmorate.enums;

import lombok.Getter;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

@Getter
public enum Genre {
    COMEDY(1),
    DRAMA(2),
    ANIMATION(3),
    THRILLER(4),
    DOCUMENTARY(5),
    ACTION(6);

    private final long code;

    Genre(long code) {
        this.code = code;
    }

    public static Genre returnGenre(Long id) {
        for (Genre g: values()) {
            if (g.getCode() == id) {
                return g;
            }
        }
        throw new NotFoundException("Жанр с кодом " + id + " не найден");
    }
}