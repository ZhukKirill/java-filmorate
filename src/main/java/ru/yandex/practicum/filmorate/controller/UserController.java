package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User addUser(@RequestBody @Valid User user) {
        log.info("начато добавление пользователя");
        return userService.addUser(user);
    }

    @GetMapping
    public Collection<User> findAll() {
        log.info("начато получение всех пользователей");
        return userService.findAll();
    }

    @PutMapping
    public User updateUser(@RequestBody @Valid User user) {
        log.info("начато обновление данных пользователя");
        return userService.updateUser(user);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable("id") Long userId,
                          @PathVariable Long friendId) {
        log.info("начато добавление пользователя в друзья");
        userService.addFriend(userId, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable("id") Long userId,
                             @PathVariable Long friendId) {
        userService.deleteFriend(userId, friendId);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> findCommonFriend(@PathVariable("id") Long userId,
                                             @PathVariable Long otherId) {
        return userService.findCommonFriend(userId, otherId);
    }

    @GetMapping("/{id}/friends")
    public Collection<User> findAllFriends(@PathVariable("id") Long userId) {
        return userService.findAllFriends(userId);
    }
}
