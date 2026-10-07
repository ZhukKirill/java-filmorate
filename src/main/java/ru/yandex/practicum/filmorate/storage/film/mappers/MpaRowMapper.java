package ru.yandex.practicum.filmorate.storage.film.mappers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.MpaInfo;

import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
@Component
public class MpaRowMapper implements RowMapper<MpaInfo> {

    @Override
    public MpaInfo mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        MpaInfo mpa = new MpaInfo();
        mpa.setId(resultSet.getLong("id"));
        mpa.setName(resultSet.getString("rating"));
        log.info(mpa.toString());
        return mpa;
    }
}
