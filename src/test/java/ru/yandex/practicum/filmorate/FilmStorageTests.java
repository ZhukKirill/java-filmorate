package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.GenreInfo;
import ru.yandex.practicum.filmorate.model.MpaInfo;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.film.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.film.mappers.LikeRowMapper;
import ru.yandex.practicum.filmorate.storage.film.mappers.MpaRowMapper;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.user.mappers.FriendshipRowMapper;
import ru.yandex.practicum.filmorate.storage.user.mappers.UserRowMapper;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({
        FilmDbStorage.class,
        UserDbStorage.class,
        FilmRowMapper.class,
        GenreRowMapper.class,
        MpaRowMapper.class,
        LikeRowMapper.class,
        UserRowMapper.class,
        FriendshipRowMapper.class
})
class FilmStorageTests {
    private final FilmStorage filmDbStorage;
    private final UserDbStorage userDbStorage;

    @Test
    void findAllReturnsEmptyListWhenNoFilms() {
        assertThat(filmDbStorage.findAll()).isEmpty();
    }

    @Test
    void shouldAddFilmWithGenres() {
        Film film = new Film();
        film.setName("New film");
        film.setDescription("description");
        film.setReleaseDate(LocalDate.of(1999, 6, 30));
        film.setDuration(120L);

        MpaInfo mpa = new MpaInfo();
        mpa.setId(1L);
        film.setMpa(mpa);

        GenreInfo genre = new GenreInfo();
        genre.setId(1L);
        film.setGenres(List.of(genre, genre)); // проверка на фильтр дубликатов

        Film saved = filmDbStorage.addFilm(film);

        Film found = filmDbStorage.findById(saved.getId());
        assertThat(found.getName()).isEqualTo("New film");
        assertThat(found.getMpa().getId()).isEqualTo(1L);
        assertThat(found.getMpa().getName()).isEqualTo("G");
        assertThat(found.getGenres()).extracting(GenreInfo::getId)
                .containsExactly(1L);
        assertThat(found.getGenres()).extracting(GenreInfo::getName)
                .containsExactly("Комедия");
    }

    @Test
    void shouldAddFilmWithoutGenres() {
        Film film = new Film();
        film.setName("New film");
        film.setDescription("description");
        film.setReleaseDate(LocalDate.of(1999, 6, 30));
        film.setDuration(120L);

        MpaInfo mpa = new MpaInfo();
        mpa.setId(1L);
        film.setMpa(mpa);
        film.setGenres(null);

        Film saved = filmDbStorage.addFilm(film);
        Film found = filmDbStorage.findById(saved.getId());

        assertThat(found.getGenres()).isEmpty();
    }

    @Test
    void shouldAddFilmWithSeveralGenres() {
        Film film = new Film();
        film.setName("New film");
        film.setDescription("description");
        film.setReleaseDate(LocalDate.of(1999, 6, 30));
        film.setDuration(120L);

        MpaInfo mpa = new MpaInfo();
        mpa.setId(1L);
        film.setMpa(mpa);

        GenreInfo comedy = new GenreInfo();
        comedy.setId(1L);
        GenreInfo drama = new GenreInfo();
        drama.setId(2L);
        film.setGenres(List.of(comedy, drama));

        Film saved = filmDbStorage.addFilm(film);
        Film found = filmDbStorage.findById(saved.getId());

        assertThat(found.getGenres())
                .extracting(GenreInfo::getId)
                .containsExactly(1L, 2L);

        assertThat(found.getGenres())
                .extracting(GenreInfo::getName)
                .containsExactly("Комедия", "Драма");
    }

    @Test
    void shouldNotAddFilmWithUnknownMpa() {
        Film film = new Film();
        film.setName("New film");
        film.setDescription("description");
        film.setReleaseDate(LocalDate.of(1999, 6, 30));
        film.setDuration(120L);

        MpaInfo mpa = new MpaInfo();
        mpa.setId(99L);
        film.setMpa(mpa);

        assertThatThrownBy(() -> filmDbStorage.addFilm(film))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldFindAllFilmsWithTheirGenres() {
        Film withGenres = createFilm("first", 1L);
        GenreInfo comedy = new GenreInfo();
        comedy.setId(1L);
        withGenres.setGenres(List.of(comedy));
        filmDbStorage.addFilm(withGenres);

        Film withoutGenre = createFilm("second", 2L);
        withoutGenre.setGenres(null);
        filmDbStorage.addFilm(withoutGenre);

        Collection<Film> films = filmDbStorage.findAll();

        assertThat(films).extracting(Film::getName)
                .containsExactlyInAnyOrder("first", "second");
        assertThat(films).filteredOn(f -> f.getName().equals("first"))
                .flatExtracting(Film::getGenres)
                .extracting(GenreInfo::getId)
                .containsExactly(1L);
        assertThat(films).filteredOn(f -> f.getName().equals("second"))
                .flatExtracting(Film::getGenres)
                .isEmpty();
    }


    @Test
    void shouldUpdateFilmAndReplaceGenres() {
        Film saved = createFilm("first", 4L);
        GenreInfo comedy = new GenreInfo();
        comedy.setId(1L);
        GenreInfo drama = new GenreInfo();
        drama.setId(2L);
        saved.setGenres(List.of(drama, comedy));
        filmDbStorage.addFilm(saved);

        saved.setName("Updated");
        saved.setDescription("new description");
        saved.setReleaseDate(LocalDate.of(2001, 1, 1));
        saved.setDuration(90L);

        GenreInfo thriller = new GenreInfo();
        thriller.setId(4L);
        saved.setGenres(List.of(thriller));

        filmDbStorage.updateFilm(saved);
        Film found = filmDbStorage.findById(saved.getId());

        assertThat(found.getName()).isEqualTo("Updated");
        assertThat(found.getDescription()).isEqualTo("new description");
        assertThat(found.getDuration()).isEqualTo(90L);
        assertThat(found.getGenres())
                .extracting(GenreInfo::getId)
                .containsExactly(4L);
    }

    @Test
    void shouldClearGenresWhenUpdateHasNullGenres() {
        Film saved = createFilm("saved", 5L);
        GenreInfo comedy = new GenreInfo();
        comedy.setId(1L);
        GenreInfo drama = new GenreInfo();
        drama.setId(2L);
        saved.setGenres(List.of(drama, comedy));
        filmDbStorage.addFilm(saved);

        saved.setGenres(null);
        filmDbStorage.updateFilm(saved);

        assertThat(filmDbStorage.findById(saved.getId()).getGenres()).isEmpty();
    }

    @Test
    void shouldNotUpdateUnknownFilm() {
        Film film = createFilm("invalid", 3L);
        film.setId(999L);

        assertThatThrownBy(() -> filmDbStorage.updateFilm(film))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldNotFindUnknownFilm() {
        Film film = createFilm("invalid", 3L);
        film.setId(999L);

        assertThatThrownBy(() -> filmDbStorage.findById(film.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldReturnAllMpa() {
        assertThat(filmDbStorage.findAllMpa()).extracting(MpaInfo::getId).containsExactly(1L, 2L, 3L, 4L, 5L);
        assertThat(filmDbStorage.findAllMpa()).extracting(MpaInfo::getName)
                .containsExactly("G", "PG", "PG-13", "R", "NC-17");
    }

    @Test
    void shouldReturnMpaById() {
        assertThat(filmDbStorage.findMpaById(1L)).extracting(MpaInfo::getId).isEqualTo(1L);
        assertThat(filmDbStorage.findMpaById(1L)).extracting(MpaInfo::getName).isEqualTo("G");

        assertThat(filmDbStorage.findMpaById(5L)).extracting(MpaInfo::getId).isEqualTo(5L);
        assertThat(filmDbStorage.findMpaById(5L)).extracting(MpaInfo::getName).isEqualTo("NC-17");
    }

    @Test
    void shouldNotFindUnknownMpa() {
        assertThatThrownBy(() -> filmDbStorage.findMpaById(6L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldReturnAllGenres() {
        assertThat(filmDbStorage.findAllGenres()).extracting(GenreInfo::getId)
                .containsExactly(1L, 2L, 3L, 4L, 5L, 6L);
        assertThat(filmDbStorage.findAllGenres()).extracting(GenreInfo::getName)
                .containsExactly("Комедия", "Драма", "Мультфильм", "Триллер", "Документальный", "Боевик");
    }

    @Test
    void shouldReturnGenreById() {
        assertThat(filmDbStorage.findGenreById(1L)).extracting(GenreInfo::getId).isEqualTo(1L);
        assertThat(filmDbStorage.findGenreById(1L)).extracting(GenreInfo::getName).isEqualTo("Комедия");

        assertThat(filmDbStorage.findGenreById(6L)).extracting(GenreInfo::getId).isEqualTo(6L);
        assertThat(filmDbStorage.findGenreById(6L)).extracting(GenreInfo::getName).isEqualTo("Боевик");
    }

    @Test
    void shouldNotFindUnknownGenre() {
        assertThatThrownBy(() -> filmDbStorage.findGenreById(10L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void addLikeAffectsPopularOrder() {
        Film first = createFilm("first", 1L);
        filmDbStorage.addFilm(first);
        Film second = createFilm("second", 2L);
        filmDbStorage.addFilm(second);
        Film third = createFilm("third", 3L);
        filmDbStorage.addFilm(third);

        User user1 = createUser("email_1@mail.ru", "login_1");
        userDbStorage.addUser(user1);
        User user2 = createUser("email_2@mail.ru", "login_2");
        userDbStorage.addUser(user2);

        filmDbStorage.addLike(second.getId(), user1.getId());
        filmDbStorage.addLike(second.getId(), user2.getId());
        filmDbStorage.addLike(first.getId(), user1.getId());

        assertThat(filmDbStorage.findTopFilms(10))
                .extracting(Film::getId)
                .containsExactly(second.getId(), first.getId(), third.getId());
    }

    @Test
    void repeatedLikeDoesNotChangePopularOrder() {
        Film first = createFilm("first", 1L);
        filmDbStorage.addFilm(first);
        Film second = createFilm("second", 2L);
        filmDbStorage.addFilm(second);
        Film third = createFilm("third", 3L);
        filmDbStorage.addFilm(third);

        User user1 = createUser("email_1@mail.ru", "login_1");
        userDbStorage.addUser(user1);
        User user2 = createUser("email_2@mail.ru", "login_2");
        userDbStorage.addUser(user2);


        filmDbStorage.addLike(first.getId(), user1.getId());
        filmDbStorage.addLike(first.getId(), user1.getId());
        filmDbStorage.addLike(first.getId(), user1.getId());

        filmDbStorage.addLike(second.getId(), user1.getId());
        filmDbStorage.addLike(second.getId(), user2.getId());

        assertThat(filmDbStorage.findTopFilms(3))
                .extracting(Film::getId)
                .containsExactly(second.getId(), first.getId(), third.getId());
    }

    @Test
    void deleteLikeChangesPopularOrder() {
        Film first = createFilm("first", 1L);
        filmDbStorage.addFilm(first);
        Film second = createFilm("second", 2L);
        filmDbStorage.addFilm(second);

        User user1 = createUser("email_4@mail.ru", "login_4");
        userDbStorage.addUser(user1);
        User user2 = createUser("email_6@mail.ru", "login_6");
        userDbStorage.addUser(user2);

        filmDbStorage.addLike(second.getId(), user1.getId());
        filmDbStorage.addLike(second.getId(), user2.getId());
        filmDbStorage.addLike(first.getId(), user1.getId());

        filmDbStorage.deleteLike(second.getId(), user2.getId());
        filmDbStorage.deleteLike(second.getId(), user1.getId());

        assertThat(filmDbStorage.findTopFilms(2))
                .extracting(Film::getId)
                .containsExactly(first.getId(), second.getId());
    }

    private Film createFilm(String name, Long mpaId) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("description");
        film.setReleaseDate(LocalDate.of(1999, 6, 30));
        film.setDuration(120L);
        MpaInfo mpa = new MpaInfo();
        mpa.setId(mpaId);
        film.setMpa(mpa);
        return film;
    }

    private User createUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName("name");
        user.setBirthday(LocalDate.now());
        return user;
    }

}
