package ru.yandex.practicum.filmorate.storage.film.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaInfo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

@Component
public class FilmRowMapper implements RowMapper<Film> {

    @Override
    public  Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = new Film();
        MpaInfo mpa = new MpaInfo();
        film.setId(resultSet.getLong("id"));
        film.setName(resultSet.getString("name"));
        mpa.setId(resultSet.getLong("mpa_id"));
        mpa.setName(resultSet.getString("mpa_name"));
        film.setDescription(resultSet.getString("description"));
        film.setDuration(resultSet.getLong("duration"));
        film.setReleaseDate(LocalDate.parse(resultSet.getString("release_date")));
        film.setMpa(mpa);
        return film;
    }
}
