package ru.yandex.practicum.filmorate.storage.user.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.enums.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.Friendship;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FriendshipRowMapper implements RowMapper<Friendship> {

    @Override
    public Friendship mapRow(ResultSet rs, int rowNum) throws SQLException {
        Friendship friendship = new Friendship();
        friendship.setRequesterId(rs.getLong("requester_id"));
        friendship.setAddresseeId(rs.getLong("addressee_id"));
        friendship.setStatus(FriendshipStatus.valueOf(rs.getString("status")));
        return friendship;
    }
}
