package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Film addFilm(Film film) {
        Long id = getNextId();
        log.debug("сгенерирован id");
        film.setId(id);
        log.debug("фильму присвоен id");
        films.put(film.getId(), film);
        log.info("фильм добавлен");
        return film;
    }

    @Override
    public Collection<Film> findAll() {
        log.info("фильмы отправлены");
        return films.values();
    }

    @Override
    public Film updateFilm(Film film) {
        if (film.getId() == null || !films.containsKey(film.getId())) {
            throw new NotFoundException("Фильм с id = " + film.getId() + " не найден");
        }
        films.put(film.getId(), film);
        log.info("фильм обновлен");
        return film;
    }

    @Override
    public Optional<Film> findById(Long id) {
        if (films.containsKey(id)) return Optional.of(films.get(id));
        return Optional.empty();
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        films.get(filmId).addLike(userId);
        log.info("лайк пользователя с id = {} добавлен к фильму с id = {}", userId, filmId);
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        films.get(filmId).deleteLike(userId);
        log.info("лайк удален");
    }

    @Override
    public List<Film> findTopFilms(int count) {
        return films.values().stream()
                .sorted(Comparator.comparingInt(Film::getLikesCount).reversed())
                .limit(count).toList();
    }

    private Long getNextId() {
        Long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        log.debug("вычислен максимальный id");
        return ++currentMaxId;
    }
}
