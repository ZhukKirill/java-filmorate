package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.GenreInfo;
import ru.yandex.practicum.filmorate.model.MpaInfo;

import java.util.Collection;

public interface FilmStorage {
    Film addFilm(Film film);

    Collection<Film> findAll();

    Film updateFilm(Film film);

    Film findById(Long id);

    void addLike(Long filmId, Long userId);

    void deleteLike(Long filmId, Long userId);

    Collection<Film> findTopFilms(int count);

    Collection<MpaInfo> findAllMpa();

    MpaInfo findMpaById(Long id);

    Collection<GenreInfo> findAllGenres();

    GenreInfo findGenreById(Long id);
}
