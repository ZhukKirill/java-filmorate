package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.model.Film;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FilmMapper {

    public static Film mapToFilm(NewFilmRequest newFilm) {
        Film film = new Film();
        film.setName(newFilm.getName());
        if (newFilm.getMpa() != null)
        film.setMpa(MpaMapper.mapToMpaInfo(newFilm.getMpa()));

        film.setDescription(newFilm.getDescription());
        film.setDuration(newFilm.getDuration());
        film.setReleaseDate(newFilm.getReleaseDate());
        if (newFilm.getGenres() != null)
        film.setGenres(newFilm.getGenres().stream().map(GenreMapper::mapToGenreInfo).toList());
        return film;
    }

    public static FilmDto mapToFilmDto(Film film) {
        FilmDto filmDto = new FilmDto();
        filmDto.setId(film.getId());
        filmDto.setName(film.getName());
        if (film.getMpa() != null)
        filmDto.setMpa(MpaMapper.mapToMpaDto(film.getMpa()));

        filmDto.setDescription(film.getDescription());
        filmDto.setDuration(film.getDuration());
        filmDto.setReleaseDate(film.getReleaseDate());
        if (film.getGenres() != null)
        filmDto.setGenres(film.getGenres().stream().map(GenreMapper::mapToGenreDto).toList());
        return filmDto;
    }
}
