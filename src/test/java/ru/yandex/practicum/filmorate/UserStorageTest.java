package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.user.mappers.FriendshipRowMapper;
import ru.yandex.practicum.filmorate.storage.user.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({
        UserDbStorage.class,
        UserRowMapper.class,
        FriendshipRowMapper.class
})

public class UserStorageTest {
    private static final ParameterizedTypeReference<Map<String, String>> MAP_TYPE =
            new ParameterizedTypeReference<>() {
            };
    private static final ParameterizedTypeReference<Collection<User>> COLLECTION_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final UserDbStorage userDbStorage;

    @Test
    void shouldAddUser() {
        User user = createUser("email@mail.ru", "login");
        User saved = userDbStorage.addUser(user);

        User found = userDbStorage.findById(saved.getId());
        assertThat(found.getEmail()).isEqualTo("email@mail.ru");
        assertThat(found.getLogin()).isEqualTo("login");
        assertThat(found.getName()).isEqualTo("name");
    }

    private User createUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName("name");
        user.setBirthday(LocalDate.now());
        return user;
    }

    @Test
    void shouldNotAddUserWithDuplicateEmail() {
        userDbStorage.addUser(createUser("email@mail.ru", "login"));

        assertThatThrownBy(() -> userDbStorage.addUser(createUser("email@mail.ru", "other")))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldNotAddUserWithDuplicateLogin() {
        userDbStorage.addUser(createUser("email@mail.ru", "login"));

        assertThatThrownBy(() -> userDbStorage.addUser(createUser("other@mail.ru", "login")))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldReturnAddedUsers() {
        userDbStorage.addUser(createUser("mail1@mail.ru", "login1"));
        userDbStorage.addUser(createUser("mail2@mail.ru", "login2"));

        assertThat(userDbStorage.findAll())
                .extracting(User::getLogin)
                .containsExactlyInAnyOrder("login1", "login2");
    }

    @Test
    void shouldReturnEmptyListWhenNoUsers() {
        assertThat(userDbStorage.findAll()).isEmpty();
    }

    @Test
    void findByIdThrowsWhenUserDoesNotExist() {
        assertThatThrownBy(() -> userDbStorage.findById(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldUpdateUser() {
        User saved = userDbStorage.addUser(createUser("email@mail.ru", "login"));
        saved.setEmail("new@mail.ru");
        saved.setLogin("newLogin");
        saved.setName("new name");
        saved.setBirthday(LocalDate.of(2000, 1, 1));

        userDbStorage.updateUser(saved);
        User found = userDbStorage.findById(saved.getId());

        assertThat(found.getEmail()).isEqualTo(saved.getEmail());
        assertThat(found.getLogin()).isEqualTo(saved.getLogin());
        assertThat(found.getName()).isEqualTo(saved.getName());
        assertThat(found.getBirthday()).isEqualTo(saved.getBirthday());
    }

    @Test
    void shouldNotUpdateToAnotherUsersEmail() {
        userDbStorage.addUser(createUser("taken@mail.ru", "login1"));
        User saved = userDbStorage.addUser(createUser("email@mail.ru", "login2"));
        saved.setEmail("TAKEN@mail.ru");

        assertThatThrownBy(() -> userDbStorage.updateUser(saved))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldNotUpdateToAnotherUsersLogin() {
        userDbStorage.addUser(createUser("one@mail.ru", "taken"));
        User saved = userDbStorage.addUser(createUser("two@mail.ru", "login"));
        saved.setLogin("taken");

        assertThatThrownBy(() -> userDbStorage.updateUser(saved))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldNotUpdateUnknownUser() {
        User user = createUser("email@mail.ru", "login");
        user.setId(999L);

        assertThatThrownBy(() -> userDbStorage.updateUser(user))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldAddOutgoingFriendRequest() {
        User user = userDbStorage.addUser(createUser("one@mail.ru", "login1"));
        User friend = userDbStorage.addUser(createUser("two@mail.ru", "login2"));

        userDbStorage.addFriend(user.getId(), friend.getId());

        assertThat(userDbStorage.findAllFriends(user.getId()))
                .extracting(User::getId)
                .containsExactly(friend.getId());
        assertThat(userDbStorage.findAllFriends(friend.getId())).isEmpty();
    }

    @Test
    void shouldNotAddFriendWhenUserDoesNotExist() {
        User user = userDbStorage.addUser(createUser("one@mail.ru", "login1"));

        assertThatThrownBy(() -> userDbStorage.addFriend(user.getId(), 999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void repeatedFriendRequestDoesNotDuplicateFriend() {
        User user = userDbStorage.addUser(createUser("one@mail.ru", "login1"));
        User friend = userDbStorage.addUser(createUser("two@mail.ru", "login2"));

        userDbStorage.addFriend(user.getId(), friend.getId());
        userDbStorage.addFriend(user.getId(), friend.getId());

        assertThat(userDbStorage.findAllFriends(user.getId()))
                .extracting(User::getId)
                .containsExactly(friend.getId());
        assertThat(userDbStorage.findAllFriends(friend.getId())).isEmpty();
    }

    @Test
    void findAllFriendsThrowsWhenUserDoesNotExist() {
        assertThatThrownBy(() -> userDbStorage.findAllFriends(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldDeletePendingRequest() {
        User user = userDbStorage.addUser(createUser("one@mail.ru", "login1"));
        User friend = userDbStorage.addUser(createUser("two@mail.ru", "login2"));
        userDbStorage.addFriend(user.getId(), friend.getId());

        userDbStorage.deleteFriend(user.getId(), friend.getId());

        assertThat(userDbStorage.findAllFriends(user.getId())).isEmpty();
        assertThat(userDbStorage.findAllFriends(friend.getId())).isEmpty();
    }

    @Test
    void shouldReturnPendingRequestWhenConfirmedFriendDeletes() {
        User user = userDbStorage.addUser(createUser("one@mail.ru", "login1"));
        User friend = userDbStorage.addUser(createUser("two@mail.ru", "login2"));
        userDbStorage.addFriend(user.getId(), friend.getId());
        userDbStorage.addFriend(friend.getId(), user.getId());

        userDbStorage.deleteFriend(user.getId(), friend.getId());

        assertThat(userDbStorage.findAllFriends(user.getId())).isEmpty();
        assertThat(userDbStorage.findAllFriends(friend.getId()))
                .extracting(User::getId)
                .containsExactly(user.getId());
    }

    @Test
    void deleteMissingFriendshipDoesNotThrow() {
        User user = userDbStorage.addUser(createUser("one@mail.ru", "login1"));
        User friend = userDbStorage.addUser(createUser("two@mail.ru", "login2"));

        assertThatCode(() -> userDbStorage.deleteFriend(user.getId(), friend.getId()))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldNotDeleteFriendWhenUserDoesNotExist() {
        User user = userDbStorage.addUser(createUser("one@mail.ru", "login1"));

        assertThatThrownBy(() -> userDbStorage.deleteFriend(user.getId(), 999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldReturnCommonFriend() {
        User user = userDbStorage.addUser(createUser("one@mail.ru", "login1"));
        User other = userDbStorage.addUser(createUser("two@mail.ru", "login2"));
        User common = userDbStorage.addUser(createUser("three@mail.ru", "login3"));

        userDbStorage.addFriend(user.getId(), common.getId());
        userDbStorage.addFriend(other.getId(), common.getId());

        assertThat(userDbStorage.findCommonFriend(user.getId(), other.getId()))
                .extracting(User::getId)
                .containsExactly(common.getId());
    }

    @Test
    void shouldReturnEmptyListWhenNoCommonFriends() {
        User user = userDbStorage.addUser(createUser("one@mail.ru", "login1"));
        User other = userDbStorage.addUser(createUser("two@mail.ru", "login2"));
        User onlyFirst = userDbStorage.addUser(createUser("three@mail.ru", "login3"));
        userDbStorage.addFriend(user.getId(), onlyFirst.getId());

        assertThat(userDbStorage.findCommonFriend(user.getId(), other.getId())).isEmpty();
    }
}
