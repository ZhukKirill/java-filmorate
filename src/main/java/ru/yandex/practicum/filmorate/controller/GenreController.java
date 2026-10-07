package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/genres")
public class GenreController {
    private final FilmService filmService;

    @GetMapping
    public Collection<GenreDto> findAllGenres() {
        return filmService.findAllGenres();
    }

    @GetMapping("/{id}")
    public GenreDto findGenreByID(@PathVariable Long id) {
        return filmService.findGenreById(id);
    }

}
