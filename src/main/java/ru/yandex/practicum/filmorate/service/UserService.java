package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userDbStorage;

    public UserDto addUser(NewUserRequest newUser) {
        nameCheck(newUser);
        User user = UserMapper.mapToUser(newUser);
        userDbStorage.addUser(user);
        return UserMapper.mapToUserDto(user);
    }

    public Collection<UserDto> findAll() {
        return userDbStorage.findAll().stream().map(UserMapper::mapToUserDto).toList();
    }

    public UserDto updateUser(NewUserRequest newUser) {
        nameCheck(newUser);
        log.info(newUser.toString());
        User user = UserMapper.mapToUser(newUser);
        userDbStorage.updateUser(user);
        return UserMapper.mapToUserDto(user);
    }

    public void addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("нельзя добавить самого себя в друзья");
        }
        userDbStorage.addFriend(userId, friendId);
    }

    public Collection<UserDto> findAllFriends(Long userId) {
        return userDbStorage.findAllFriends(userId).stream().map(UserMapper::mapToUserDto).toList();
    }

    public void deleteFriend(Long userId, Long friendId) {
        userDbStorage.deleteFriend(userId, friendId);
    }

    public Collection<UserDto> findCommonFriend(Long userId, Long otherId) {
        return userDbStorage.findCommonFriend(userId,otherId).stream().map(UserMapper::mapToUserDto).toList();
    }

    private void nameCheck(NewUserRequest user) {
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("имя пользователя не было передано");
            user.setName(user.getLogin());
            log.info("имени пользователя присвоено значение логина");
        }
    }
}
