package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class Like {
    Long filmId;
    Long userId;
}
