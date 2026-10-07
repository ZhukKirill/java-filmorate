package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.GenreInfo;
import ru.yandex.practicum.filmorate.model.MpaInfo;
import ru.yandex.practicum.filmorate.storage.BaseDbStorage;
import ru.yandex.practicum.filmorate.storage.film.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.film.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.film.mappers.LikeRowMapper;
import ru.yandex.practicum.filmorate.storage.film.mappers.MpaRowMapper;

import java.util.*;

@Repository
public class FilmDbStorage extends BaseDbStorage<Film> implements FilmStorage {

    private static final String INSERT_FILM_QUERY = "INSERT INTO films(name, description, release_date," +
            "duration, rating_id) VALUES (?, ?, ?, ?, ?)";
    private static final String FIND_BY_ID_FILM_QUERY = """
            SELECT f.id, f.name, f.description, f.release_date, f.duration, r.id AS mpa_id, r.rating AS mpa_name
            FROM films f JOIN ratings r ON r.id = f.rating_id WHERE f.id = ?
            """;
    private static final String FIND_ALL_FILMS_QUERY = """
            SELECT f.id, f.name, f.description, f.release_date, f.duration, r.id AS mpa_id, r.rating AS mpa_name
            FROM films f JOIN ratings r ON r.id = f.rating_id
            """;
    private static final String FIND_ALL_MPA_QUERY = "SELECT * FROM ratings ORDER BY id";
    private static final String FIND_MPA_BY_ID_QUERY = "SELECT * FROM ratings WHERE id = ?";
    private static final String FUND_ALL_GENRES_QUERY = "SELECT * FROM genres ORDER BY id";
    private static final String FIND_GENRES_BY_ID_QUERY = "SELECT * FROM genres WHERE id = ?";
    private static final String ADD_FILM_GENRE_QUERY = "INSERT INTO film_genres(film_id, genre_id) VALUES (?, ?)";
    private static final String UPDATE_FILM_QUERY = "UPDATE films SET name = ?, description = ?, release_date = ?," +
            "duration = ?, rating_id = ?  WHERE id = ?";
    private static final String DELETE_FILM_GENRES_QUERY = "DELETE FROM film_genres WHERE film_id = ?";
    private static final String FIND_ALL_FILM_GENRES_QUERY = "SELECT fg.film_id, fg.genre_id, g.genre AS name FROM " +
            "film_genres fg JOIN genres g ON fg.genre_id = g.id";
    private static final String FIND_GENRES_BY_FILM_ID_QUERY = "SELECT fg.film_id, fg.genre_id, g.genre AS name FROM " +
            "film_genres fg JOIN genres g ON fg.genre_id = g.id WHERE fg.film_id = ?";
    private static final String ADD_LIKE_QUERY = "INSERT INTO film_likes(film_id, user_id) VALUES (?, ?)";
    private static final String FIND_LIKE_QUERY = "SELECT * FROM film_likes WHERE film_id = ? AND user_id = ?";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
    private static final String FIND_FILMS_LIKES_QUERY = """
            SELECT f.id, f.name, f.description, f.release_date, f.duration,
            r.id AS mpa_id, r.rating AS mpa_name,
            COALESCE(likes.like_count, 0) AS like_count
            FROM films f
            JOIN ratings r ON r.id = f.rating_id
            LEFT JOIN (
                SELECT film_id, COUNT(*) AS like_count
                FROM film_likes
                GROUP BY film_id
            ) likes ON likes.film_id = f.id
            ORDER BY like_count DESC""";

    private final JdbcTemplate jdbc;
    private final FilmRowMapper filmRowMapper;
    private final GenreRowMapper genreRowMapper;
    private final MpaRowMapper mpaRowMapper;
    private final LikeRowMapper likeRowMapper;

    public FilmDbStorage(JdbcTemplate jdbc, FilmRowMapper filmRowMapper,
                         GenreRowMapper genreRowMapper, MpaRowMapper mpaRowMapper, LikeRowMapper likeRowMapper) {
        super(jdbc);
        this.jdbc = jdbc;
        this.filmRowMapper = filmRowMapper;
        this.genreRowMapper = genreRowMapper;
        this.mpaRowMapper = mpaRowMapper;
        this.likeRowMapper = likeRowMapper;
    }

    @Override
    public Film addFilm(Film film) {
        long id = insertAndReturnId(
                INSERT_FILM_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId());
        film.setId(id);
        if (film.getGenres() != null) {
            List<Long> genresId = film.getGenres().stream().map(GenreInfo::getId).distinct().toList();
            for (Long genreId : genresId) insert(ADD_FILM_GENRE_QUERY, film.getId(), genreId);
        }
        return film;
    }

    @Override
    public Collection<Film> findAll() {
        List<Film> films = findMany(FIND_ALL_FILMS_QUERY, filmRowMapper);
        Map<Long, Film> filmMap = new HashMap<>();
        for (Film film : films) {
            film.setGenres(new ArrayList<>());
            filmMap.put(film.getId(), film);
        }

        jdbc.query(FIND_ALL_FILM_GENRES_QUERY,
                (resultSet, rowNum) -> {
                    Film film = filmMap.get(resultSet.getLong("film_id"));
                    if (film != null) {
                        GenreInfo genre = new GenreInfo();
                        genre.setId(resultSet.getLong("genre_id"));
                        genre.setName(resultSet.getString("name"));
                        film.getGenres().add(genre);
                        return film;
                    }
                    return null;
                }
        );
        return films;
    }

    @Override
    public Film updateFilm(Film newFilm) {
        findById(newFilm.getId());
        update(UPDATE_FILM_QUERY, newFilm.getName(), newFilm.getDescription(), newFilm.getReleaseDate(),
                newFilm.getDuration(), newFilm.getMpa().getId(), newFilm.getId());
        jdbc.update(DELETE_FILM_GENRES_QUERY, newFilm.getId());
        if (newFilm.getGenres() != null) {
            List<Long> genresId = newFilm.getGenres().stream().map(GenreInfo::getId).distinct().toList();
            for (Long genreId : genresId) insert(ADD_FILM_GENRE_QUERY, newFilm.getId(), genreId);
        }
        return newFilm;
    }

    @Override
    public Film findById(Long id) {
        Film film = findOne(FIND_BY_ID_FILM_QUERY, filmRowMapper, id)
                .orElseThrow(() -> new NotFoundException("Фильм " +
                        "с идентификатором " + id + " не найден"));
        film.setGenres(new ArrayList<>());
        jdbc.query(FIND_GENRES_BY_FILM_ID_QUERY,
                (resultSet, rowNum) -> {
                    GenreInfo genre = new GenreInfo();
                    genre.setId(resultSet.getLong("genre_id"));
                    genre.setName(resultSet.getString("name"));
                    film.getGenres().add(genre);
                    return film;
                },
                id
        );
        return film;
    }

    @Override
    public Collection<MpaInfo> findAllMpa() {
        return jdbc.query(FIND_ALL_MPA_QUERY, mpaRowMapper);
    }

    @Override
    public MpaInfo findMpaById(Long id) {
        try {
            return jdbc.queryForObject(FIND_MPA_BY_ID_QUERY, mpaRowMapper, id);

        } catch (EmptyResultDataAccessException ignored) {
            throw new NotFoundException("рейтинга с таким id нет");
        }
    }

    @Override
    public Collection<GenreInfo> findAllGenres() {
        return jdbc.query(FUND_ALL_GENRES_QUERY, genreRowMapper);
    }

    @Override
    public GenreInfo findGenreById(Long id) {
        try {
            return jdbc.queryForObject(FIND_GENRES_BY_ID_QUERY, genreRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("жанра с таким id нет");
        }
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        try {
            jdbc.queryForObject(FIND_LIKE_QUERY, likeRowMapper, filmId, userId);
        } catch (EmptyResultDataAccessException e) {
            jdbc.update(ADD_LIKE_QUERY, filmId, userId);
        }
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        try {
            jdbc.queryForObject(FIND_LIKE_QUERY, likeRowMapper, filmId, userId);
            jdbc.update(DELETE_LIKE_QUERY, filmId, userId);
        } catch (EmptyResultDataAccessException ignored) {
        }
    }

    @Override
    public Collection<Film> findTopFilms(int count) {
        return jdbc.query(FIND_FILMS_LIKES_QUERY, filmRowMapper);
    }

}
