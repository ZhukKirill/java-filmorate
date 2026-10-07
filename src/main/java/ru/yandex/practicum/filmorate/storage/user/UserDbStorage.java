package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.enums.FriendshipStatus;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.storage.BaseDbStorage;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.mappers.FriendshipRowMapper;
import ru.yandex.practicum.filmorate.storage.user.mappers.UserRowMapper;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class UserDbStorage extends BaseDbStorage<User> implements UserStorage {
    private final JdbcTemplate jdbc;
    private final UserRowMapper userRowMapper;
    private final FriendshipRowMapper friendshipRowMapper;

    private static final String INSERT_USER_QUERY = "INSERT INTO users(email, login, name, " +
            "birthday) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_USER_QUERY = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ?" +
            " WHERE id = ?";
    private static final String FIND_ALL_USERS_QUERY = "SELECT * FROM users";
    private static final String ADD_FRIEND_QUERY = "INSERT INTO friendships(requester_id, addressee_id, status) " +
            "VALUES (?, ?, ?)";
    private static final String UPDATE_FRIENDSHIP_QUERY = "UPDATE friendships SET status = ? " +
            "WHERE requester_id = ? AND addressee_id = ?";
    private static final String FIND_ALL_FRIENDS_QUERY = "SELECT us.id, us.email, us.login, us.name, us.birthday " +
            "FROM users us JOIN friendships f1 ON f1.addressee_id = us.id " +
            "WHERE f1.requester_id = ? " +
            "UNION " +
            "SELECT us.id, us.email, us.login, us.name, us.birthday " +
            "FROM users us JOIN friendships f2 ON f2.requester_id = us.id " +
            "WHERE f2.addressee_id = ? AND f2.status = 'CONFIRMED'";
    private static final String FIND_USER_BY_ID_QUERY = "SELECT * FROM users WHERE id = ?";
    private static final String FIND_USER_BY_EMAIL_OR_LOGIN_QUERY = "SELECT * FROM users WHERE " +
            "LOWER(email) = LOWER(?) OR LOWER(login) = LOWER(?)";
    private static final String FIND_FRIENDSHIP_BY_USERS_ID_QUERY = "SELECT * FROM friendships " +
            "WHERE requester_id = ? AND addressee_id =? OR requester_id = ? AND addressee_id =?";
    private static final String UPDATE_FRIENDSHIPS_STATUS_TO_CONFIRMED = "UPDATE friendships SET status = ? " +
            "WHERE requester_id = ? AND addressee_id = ?";
    private static final String DELETE_FRIENDSHIP_ROW_QUERY = "DELETE FROM friendships " +
            "WHERE requester_id = ? AND addressee_id = ?";
    private static final String FIND_COMMON_FRIEND_QUERY = "(SELECT us.id, us.email, us.login, us.name, us.birthday " +
            "FROM users us JOIN friendships f1 ON f1.addressee_id = us.id " +
            "WHERE f1.requester_id = ? " +
            "UNION " +
            "SELECT us.id, us.email, us.login, us.name, us.birthday " +
            "FROM users us JOIN friendships f2 ON f2.requester_id = us.id " +
            "WHERE f2.addressee_id = ? AND f2.status = 'CONFIRMED') " +
            "INTERSECT " +
            "(SELECT us.id, us.email, us.login, us.name, us.birthday " +
            "FROM users us JOIN friendships f1 ON f1.addressee_id = us.id " +
            "WHERE f1.requester_id = ? " +
            "UNION " +
            "SELECT us.id, us.email, us.login, us.name, us.birthday " +
            "FROM users us JOIN friendships f2 ON f2.requester_id = us.id " +
            "WHERE f2.addressee_id = ? AND f2.status = 'CONFIRMED')";

    public UserDbStorage(JdbcTemplate jdbc, JdbcTemplate jdbc1, UserRowMapper userRowMapper, FriendshipRowMapper friendshipRowMapper) {
        super(jdbc);
        this.jdbc = jdbc1;
        this.userRowMapper = userRowMapper;
        this.friendshipRowMapper = friendshipRowMapper;
    }

    public User addUser(User user) {
        List<User> userList = findMany(FIND_USER_BY_EMAIL_OR_LOGIN_QUERY,
                userRowMapper, user.getEmail(), user.getLogin());

        if (userList.stream().anyMatch(u -> u.getEmail().equals(user.getEmail())))
            throw new ValidationException("этот емаил уже используется другим пользователем");
        if (userList.stream().anyMatch(u -> u.getLogin().equals(user.getLogin())))
            throw new ValidationException("этот логин уже используется другим пользователем");

        Long id = insertAndReturnId(INSERT_USER_QUERY, user.getEmail(), user.getLogin(), user.getName(),
                user.getBirthday());
        user.setId(id);
        return user;
    }

    @Override
    public User findById(Long id) {
        Optional<User> user = findOne(FIND_USER_BY_ID_QUERY, userRowMapper, id);
        if (user.isEmpty()) {
            throw new NotFoundException("пользователь с id " + id + " не найден");
        }
        return user.get();
    }

    @Override
    public User updateUser(User newUser) {
        List<User> userList = findMany(FIND_USER_BY_EMAIL_OR_LOGIN_QUERY,
                userRowMapper, newUser.getEmail(), newUser.getLogin());

        User oldUser = findById(newUser.getId());
        if (!oldUser.getEmail().equalsIgnoreCase(newUser.getEmail()))
            if (userList.stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(newUser.getEmail())))
                throw new ValidationException("этот емаил уже используется другим пользователем");
        if (!oldUser.getLogin().equalsIgnoreCase(newUser.getLogin()))
            if (userList.stream().anyMatch(u -> u.getLogin().equalsIgnoreCase(newUser.getLogin())))
                throw new ValidationException("этот логин уже используется другим пользователем");

        insert(UPDATE_USER_QUERY, newUser.getEmail(), newUser.getLogin(),
                newUser.getName(), newUser.getBirthday(), newUser.getId());

        return newUser;
    }

    @Override
    public Collection<User> findAll() {
        return jdbc.query(FIND_ALL_USERS_QUERY, userRowMapper);
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        findById(userId);
        findById(friendId);
        try {
            Friendship friendship = jdbc.queryForObject(FIND_FRIENDSHIP_BY_USERS_ID_QUERY, friendshipRowMapper,
                    userId, friendId, friendId, userId);
            if (FriendshipStatus.PENDING == friendship.getStatus() && userId.equals(friendship.getAddresseeId()))
                jdbc.update(UPDATE_FRIENDSHIPS_STATUS_TO_CONFIRMED, FriendshipStatus.CONFIRMED.name(), friendId, userId);
        } catch (EmptyResultDataAccessException e) {
            insert(ADD_FRIEND_QUERY, userId, friendId, FriendshipStatus.PENDING.name());
        }
    }

    @Override
    public Collection<User> findAllFriends(Long userId) {
        findById(userId);
        return jdbc.query(FIND_ALL_FRIENDS_QUERY, userRowMapper, userId, userId);
    }


    @Override
    public void deleteFriend(Long userId, Long friendId) {
        findById(userId);
        findById(friendId);
        try {
            Friendship friendship = jdbc.queryForObject(FIND_FRIENDSHIP_BY_USERS_ID_QUERY, friendshipRowMapper,
                    userId, friendId, friendId, userId);
            if (FriendshipStatus.PENDING == friendship.getStatus() && userId.equals(friendship.getRequesterId()))
                jdbc.update(DELETE_FRIENDSHIP_ROW_QUERY, userId, friendId);
            if (FriendshipStatus.CONFIRMED == friendship.getStatus()) {
                if (userId.equals(friendship.getRequesterId())) {
                    jdbc.update(DELETE_FRIENDSHIP_ROW_QUERY, userId, friendId);
                    jdbc.update(ADD_FRIEND_QUERY, friendId, userId, FriendshipStatus.PENDING.name());
                } else jdbc.update(UPDATE_FRIENDSHIP_QUERY, FriendshipStatus.PENDING.name(), friendId, userId);
            }
        } catch (EmptyResultDataAccessException ignored) {
        }
    }

    @Override
    public Collection<User> findCommonFriend(Long userId, Long otherId) {
        return jdbc.query(FIND_COMMON_FRIEND_QUERY, userRowMapper, userId, userId, otherId, otherId);
    }

}
