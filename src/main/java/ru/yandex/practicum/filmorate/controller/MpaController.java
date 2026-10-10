package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/mpa")
public class MpaController {
    private final FilmService filmService;

    @GetMapping()
    public Collection<MpaDto> findAll() {
        return filmService.findAllMpa();
    }

    @GetMapping("/{id}")
    public MpaDto filndMpaById(@PathVariable Long id) {
        return filmService.findMpaById(id);
    }
}
