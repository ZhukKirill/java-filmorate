package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Film addFilm(Film film);

    Collection<Film> findAll();

    Film updateFilm(Film film);

    Optional<Film> findById(long id);

    void addLike(long filmId, long userId);

    void deleteLike(long filmId, long userId);

    Collection<Film> findTopFilms(int count);
}
