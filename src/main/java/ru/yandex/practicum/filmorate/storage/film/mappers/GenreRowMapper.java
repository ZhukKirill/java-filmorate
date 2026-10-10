package ru.yandex.practicum.filmorate.storage.film.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.GenreInfo;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class GenreRowMapper implements RowMapper<GenreInfo> {

    @Override
    public GenreInfo mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        GenreInfo genre = new GenreInfo();
        genre.setId(resultSet.getLong("id"));
        genre.setName(resultSet.getString("genre"));
        return genre;
    }

}
