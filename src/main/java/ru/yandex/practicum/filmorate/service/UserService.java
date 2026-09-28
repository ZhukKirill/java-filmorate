package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage inMemoryUserStorage;

    public User addUser(User user) {
        validationCheck(user);
        return inMemoryUserStorage.addUser(user);
    }

    public Collection<User> findAll() {
        return inMemoryUserStorage.findAll();
    }

    public User updateUser(User user) {
        validationCheck(user);
        return inMemoryUserStorage.updateUser(user);
    }

    public void addFriend(Long userId, Long friendId) {
        userIdCheck(userId);
        userIdCheck(friendId);
        if (userId.equals(friendId)) {
            throw new ValidationException("нельзя добавить самого себя в друзья");
        }
        inMemoryUserStorage.addFriend(userId, friendId);
    }

    public void deleteFriend(Long userId, Long friendId) {
        userIdCheck(userId);
        userIdCheck(friendId);
        inMemoryUserStorage.deleteFriend(userId, friendId);
    }

    public Collection<User> findAllFriends(Long userId) {
        userIdCheck(userId);
        return inMemoryUserStorage.findAllFriends(userId);
    }

    public Collection<User> findCommonFriend(Long userId, Long otherId) {
        userIdCheck(userId);
        userIdCheck(otherId);
        return inMemoryUserStorage.findCommonFriend(userId,otherId);
    }

    private void validationCheck(User user) {
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("логин не может быть пустым и содержать пробелы");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("имя пользователя не было передано");
            user.setName(user.getLogin());
            log.info("имени пользователя присвоено значение логина");
        }
    }

    private void userIdCheck(Long userId) {
        if (inMemoryUserStorage.findById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
    }
}
