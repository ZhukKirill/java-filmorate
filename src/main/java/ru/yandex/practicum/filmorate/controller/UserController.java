package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User addUser(@RequestBody User user) {
        log.info("начато добавление пользователя");
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.info("электронная почта не может быть пустой");
            throw new ValidationException("электронная почта не может быть пустой");
        }
        if (!user.getEmail().contains("@")) {
            log.info("электронная почта должна содержать символ @");
            throw new ValidationException("электронная почта должна содержать символ @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.info("логин не может быть пустым и содержать пробелы");
            throw new ValidationException("логин не может быть пустым и содержать пробелы");
        }
        if (user.getBirthday() == null) {
            log.info("дата рождения должна быть указана");
            throw new ValidationException("дата рождения должна быть указана");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.info("дата рождения не может быть в будущем");
            throw new ValidationException("дата рождения не может быть в будущем");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("имя пользователя не было передано");
            user.setName(user.getLogin());
            log.info("имени пользователя присвоено значение логина");
        }
        long id = getNextId();
        log.debug("сгенерирован id");
        user.setId(id);
        log.debug("пользователю присвоен id");
        users.put(id, user);
        log.info("пользователь добавлен");
        return user;
    }

    @GetMapping
    public Collection<User> findAll() {
        log.info("начато получение всех пользователей");
        log.info("пользователи получены");
        return users.values();
    }

    @PutMapping
    public User updateUser(@RequestBody User user) {
        log.info("начато обновление пользователя");
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.info("электронная почта не может быть пустой");
            throw new ValidationException("электронная почта не может быть пустой");
        }
        if (!user.getEmail().contains("@")) {
            log.info("электронная почта должна содержать символ @");
            throw new ValidationException("электронная почта должна содержать символ @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.info("логин не может быть пустым и содержать пробелы");
            throw new ValidationException("логин не может быть пустым и содержать пробелы");
        }
        if (user.getBirthday() == null) {
            log.info("дата рождения должна быть указана");
            throw new ValidationException("дата рождения должна быть указана");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.info("дата рождения не может быть в будущем");
            throw new ValidationException("дата рождения не может быть в будущем");
        }
        if (user.getId() == null || !users.containsKey(user.getId())) {
            log.info("пользователь не найден");
            throw new NotFoundException("Пользователь с id = " + user.getId() + " не найден");
        }
        users.put(user.getId(), user);
        log.info("пользователь обновлен");
        return user;
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
}
