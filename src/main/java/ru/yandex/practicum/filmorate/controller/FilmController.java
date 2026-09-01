package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film addFilm(@RequestBody Film film) {
        log.info("начато добавление фильм");
        if (films.containsKey(film.getId())) {
            log.info("фильм с таким id уже добавлен");
            throw new ValidationException("фильм с таким id уже добавлен");
        }
        if (film.getName() == null || film.getName().isBlank()) {
            log.info("название не может быть пустым");
            throw new ValidationException("название не может быть пустым");
        }
        if (film.getDescription() == null || film.getDescription().length() > 200) {
            log.info("длина описания превышает 200 символов");
            throw new ValidationException("длина описания превышает 200 символов");
        }
        if (film.getReleaseDate() == null
                || film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.info("дата релиза должна быть не раньше 28 декабря 1895 года");
            throw new ValidationException("дата релиза должна быть не раньше 28 декабря 1895 года");
        }
        if (film.getDuration() == null || film.getDuration().toSeconds() <= 0) {
            log.info("продолжительность фильма в секундах должна быть больше нуля");
            throw new ValidationException("продолжительность фильма в секундах должна быть больше нуля");
        }
//        if (film.getId() == null) {
//            throw new ValidationException("id должен быть указан");
//        }
        films.put(film.getId(), film);
        log.info("фильм добавлен");
        return film;
    }

    @GetMapping
    public Collection<Film> findAll() {
        log.info("начато получение всех фильмы");
        log.info("фильмы отправлены");
        return films.values();
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film film) {
        log.info("начато обновление фильма");
        if (film.getName() == null || film.getName().isBlank()) {
            log.info("название не может быть пустым");
            throw new ValidationException("название не может быть пустым");
        }
        if (film.getDescription() == null || film.getDescription().length() > 200) {
            log.info("длина описания превышает 200 символов");
            throw new ValidationException("длина описания превышает 200 символов");
        }
        if (film.getReleaseDate() == null
                || film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.info("дата релиза должна быть не раньше 28 декабря 1895 года");
            throw new ValidationException("дата релиза должна быть не раньше 28 декабря 1895 года");
        }
        if (film.getDuration() == null || film.getDuration().toSeconds() <= 0) {
            log.info("продолжительность фильма в секундах должна быть больше нуля");
            throw new ValidationException("продолжительность фильма в секундах должна быть больше нуля");
        }
        if (film.getId() == null || !films.containsKey(film.getId())) {
            log.info("фильм не найден");
            throw new NotFoundException("Фильм с id = " + film.getId() + " не найден");
        }
        films.put(film.getId(), film);
        log.info("фильм обновлен");
        return film;
    }
}
