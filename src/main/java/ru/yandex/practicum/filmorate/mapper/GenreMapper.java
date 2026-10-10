package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.model.GenreInfo;

public class GenreMapper {

    public static GenreDto mapToGenreDto(GenreInfo genreInfo) {
        GenreDto genreDto = new GenreDto();
        genreDto.setId(genreInfo.getId());
        genreDto.setName(genreInfo.getName());
        return genreDto;
    }

    public static GenreInfo mapToGenreInfo(GenreDto genreDto) {
        GenreInfo genreInfo = new GenreInfo();
        genreInfo.setId(genreDto.getId());
        genreInfo.setName(genreDto.getName());
        return genreInfo;
    }
}
