package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    public User addUser(User user) {
        long id = getNextId();
        log.debug("сгенерирован id");
        user.setId(id);
        log.debug("пользователю присвоен id");
        users.put(id, user);
        log.info("пользователь добавлен");
        return user;
    }

    public Optional<User> findById(long id) {
        if (users.containsKey(id)) {
            return Optional.of(users.get(id));
        }
        return Optional.empty();
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        log.debug("вычислен максимальный id");
        return ++currentMaxId;
    }

    @Override
    public Collection<User> findAll() {
        log.info("пользователи получены");
        return users.values();
    }

    @Override
    public User updateUser(User user) {
        if (user.getId() == null || !users.containsKey(user.getId())) {
            throw new NotFoundException("Пользователь с id = " + user.getId() + " не найден");
        }
        users.put(user.getId(), user);
        log.info("пользователь обновлен");
        return user;
    }

    @Override
    public void addFriend(long userId, long friendId) {
        users.get(userId).addFriend(friendId);
        users.get(friendId).addFriend(userId);
        log.info("друг добавлен");
    }

    @Override
    public void deleteFriend(long userId, long friendId) {
        users.get(userId).deleteFriend(friendId);
        users.get(friendId).deleteFriend(userId);
        log.info("друг удален");
    }

    public Collection<User> findAllFriends(long userId) {
        return users.get(userId).getFriends().stream().map(users::get).toList();
    }

    public Collection<User> findCommonFriend(long userId, long otherId) {
        return users.get(userId).getFriends().stream()
                .filter(id -> users.get(otherId).getFriends().contains(id))
                .map(users::get).toList();
    }
}
