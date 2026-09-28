package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage inMemoryFilmStorage;
    private final UserStorage inMemoryUserStorage;

    public Film createFilm(Film film) {
        validationCheck(film);
        return inMemoryFilmStorage.addFilm(film);
    }

    public Collection<Film> findAll() {
        return inMemoryFilmStorage.findAll();
    }

    public Film updateFilm(Film film) {
        validationCheck(film);
        return inMemoryFilmStorage.updateFilm(film);
    }

    public void addLike(Long filmId, Long userId) {
        filmIdCheck(filmId);
        userIdCheck(userId);
        inMemoryFilmStorage.addLike(filmId, userId);
    }

    public void deleteLike(Long filmId, Long userId) {
        filmIdCheck(filmId);
        userIdCheck(userId);
        inMemoryFilmStorage.deleteLike(filmId, userId);
    }

    public Collection<Film> findTopFilm(int count) {
        return inMemoryFilmStorage.findTopFilms(count);
    }

    private void filmIdCheck(Long filmId) {
        if (inMemoryFilmStorage.findById(filmId).isEmpty()) {
            throw new NotFoundException("Фильм с id = " + filmId + " не найден");
        }
    }

    private void userIdCheck(Long userId) {
        if (inMemoryUserStorage.findById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
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
