package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    private Long id;
    @Email(message = "электронная почта должна быть указан и иметь вид: user@domain.tld")
    private String email;
    private String login;
    private String name;

    @Past(message = "дата рождения не может быть в будущем")
    @NotNull
    private LocalDate birthday;
    private final Set<Long> friends = new HashSet<>();

    public void addFriend(Long friendId) {
        friends.add(friendId);
    }

    public void deleteFriend(Long friendId) {
        friends.remove(friendId);
    }
}
