package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {

    User addUser(User user);

    Optional<User> findById(long id);

    User updateUser(User user);

    Collection<User> findAll();

    void addFriend(long userId, long friendId);

    void deleteFriend(long userId, long friendId);

    Collection<User> findAllFriends(long userId);

    Collection<User> findCommonFriend(long userId, long otherId);
}
