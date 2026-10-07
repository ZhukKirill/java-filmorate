package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.enums.Genre;
import ru.yandex.practicum.filmorate.enums.Rating;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.mapper.MpaMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.GenreInfo;
import ru.yandex.practicum.filmorate.model.MpaInfo;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Comparator;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmDbStorage;
    private final UserStorage userDbStorage;

    public FilmDto createFilm(NewFilmRequest newFilm) {
        validationCheck(newFilm);
        Film film = FilmMapper.mapToFilm(newFilm);
        filmDbStorage.addFilm(film);
        return FilmMapper.mapToFilmDto(film);
    }

    public Collection<FilmDto> findAll() {
        return filmDbStorage.findAll().stream().map(FilmMapper::mapToFilmDto).toList();
    }

    public FilmDto findFilmById(Long id) {
        Film film = filmDbStorage.findById(id);
        return FilmMapper.mapToFilmDto(film);
    }

    public FilmDto updateFilm(NewFilmRequest newFilm) {
        validationCheck(newFilm);
        if (newFilm.getId() == null) throw new ValidationException("id должен быть указан");
        Film film = FilmMapper.mapToFilm(newFilm);
        film.setId(newFilm.getId());
        return FilmMapper.mapToFilmDto(filmDbStorage.updateFilm(film));
    }

    public Collection<MpaDto> findAllMpa() {
        return filmDbStorage.findAllMpa().stream().sorted(Comparator.comparingLong(MpaInfo::getId))
                .map(MpaMapper::mapToMpaDto).toList();
    }

    public MpaDto findMpaById(Long id) {
        if (id == null) throw new ValidationException("id должен быть указан");
        MpaInfo map = filmDbStorage.findMpaById(id);
        return MpaMapper.mapToMpaDto(map);
    }

    public Collection<GenreDto> findAllGenres() {
        return filmDbStorage.findAllGenres().stream()
                .sorted(Comparator.comparingLong(GenreInfo::getId))
                .map(GenreMapper::mapToGenreDto)
                .toList();
    }

    public GenreDto findGenreById(Long id) {
        if (id == null) throw new ValidationException("id должен быть указан");
        GenreInfo genreInfo = filmDbStorage.findGenreById(id);
        return GenreMapper.mapToGenreDto(genreInfo);
    }

    public void addLike(Long filmId, Long userId) {
        userIdCheck(userId);
        filmDbStorage.addLike(filmId, userId);
    }

    public void deleteLike(Long filmId, Long userId) {
        userIdCheck(userId);
        filmDbStorage.deleteLike(filmId, userId);
    }

    public Collection<FilmDto> findTopFilm(int count) {
        return filmDbStorage.findTopFilms(count).stream().map(FilmMapper::mapToFilmDto).toList();
    }

    private void userIdCheck(Long userId) {
        userDbStorage.findById(userId);
    }

    private void validationCheck(NewFilmRequest film) {
        if (film.getMpa() != null) Rating.returnRating(film.getMpa().getId());

        if (film.getGenres() != null) {
            for (GenreDto genre: film.getGenres()) {
                Genre.returnGenre(genre.getId());
            }
        }
    }
}
