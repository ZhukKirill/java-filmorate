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
        log.info("начато добавление фильма");
        validationCheck(film);
        long id = getNextId();
        log.debug("сгенерирован id");
        film.setId(id);
        log.debug("фильму присвоен id");
        films.put(id, film);
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
        validationCheck(film);
        if (film.getId() == null || !films.containsKey(film.getId())) {
            log.info("фильм не найден");
            throw new NotFoundException("Фильм с id = " + film.getId() + " не найден");
        }
        films.put(film.getId(), film);
        log.info("фильм обновлен");
        return film;
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        log.debug("вычислен максимальный id");
        return ++currentMaxId;
    }

    private void validationCheck(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("название не может быть пустым");
        }
        if (film.getDescription() == null || film.getDescription().length() > 200) {
            throw new ValidationException("длина описания превышает 200 символов");
        }
        if (film.getReleaseDate() == null
                || film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("дата релиза должна быть не раньше 28 декабря 1895 года");
        }
        if (film.getDuration() == null || film.getDuration().toSeconds() <= 0) {
            throw new ValidationException("продолжительность фильма в секундах должна быть больше нуля");
        }
    }
}
