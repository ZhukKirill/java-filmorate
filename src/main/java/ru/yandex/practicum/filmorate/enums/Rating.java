package ru.yandex.practicum.filmorate.enums;

import lombok.Getter;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

@Getter
public enum Rating {
    G(1),
    PG(2),
    PG_13(3),
    R(4),
    NC_17(5);

    private final long code;

    Rating(int code) {
        this.code = code;
    }

    public static Rating returnRating(Long id) {
        for (Rating r: values()) {
            if (r.getCode() == id) {
                return r;
            }
        }
        throw new NotFoundException("Рейтинг с кодом " + id + " не найден");
    }

}

