package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class UserControllerTest {
    private static final ParameterizedTypeReference<Map<String, String>> MAP_TYPE =
            new ParameterizedTypeReference<>() {
            };
    private static final ParameterizedTypeReference<Collection<User>> COLLECTION_TYPE =
            new ParameterizedTypeReference<>() {
            };

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void post_ShouldAddUserSuccessfully() {
        Map<String, Object> invalidUser = Map.of(
                "name", "Name",
                "email", "email@",
                "login", "Login",
                "birthday", "2000-01-01"
        );

        ResponseEntity<User> response = restTemplate.exchange(
                "/users",
                HttpMethod.POST,
                new HttpEntity<>(invalidUser),
                User.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getName()).isEqualTo("Name");
    }

    @Test
    void post_ShouldReturnBadRequestWithInvalideEmail() {
        Map<String, Object> invalidUser1 = Map.of(
                "name", "Name",
                "email", " ",
                "login", "Login",
                "birthday", "2000-01-01"
        );

        ResponseEntity<Map<String, String>> response1 = restTemplate.exchange(
                "/users",
                HttpMethod.POST,
                new HttpEntity<>(invalidUser1),
                MAP_TYPE
        );

        Map<String, Object> invalidUser2 = Map.of(
                "name", "Name",
                "email", "email",
                "login", "Login",
                "birthday", "2000-01-01"
        );

        ResponseEntity<Map<String, String>> response2 = restTemplate.exchange(
                "/users",
                HttpMethod.POST,
                new HttpEntity<>(invalidUser2),
                MAP_TYPE
        );
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response1.getBody().get("error")).isEqualTo("электронная почта не может быть пустой");
        assertThat(response2.getBody().get("error")).isEqualTo("электронная почта должна содержать символ @");
    }

    @Test
    void post_ShouldReturnBadRequestWithInvalideLogin() {
        Map<String, Object> invalidUser = Map.of(
                "name", "Name",
                "email", "email@",
                "login", " ",
                "birthday", "2000-01-01"
        );

        ResponseEntity<Map<String, String>> response = restTemplate.exchange(
                "/users",
                HttpMethod.POST,
                new HttpEntity<>(invalidUser),
                MAP_TYPE
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("error")).isEqualTo("логин не может быть пустым и содержать пробелы");
    }

    @Test
    void post_ShouldReturnBadRequestWithInvalideBirthday() {
        Map<String, Object> invalidUser = Map.of(
                "name", "Name",
                "email", "email@",
                "login", "Login",
                "birthday", "2027-01-01"
        );

        ResponseEntity<Map<String, String>> response = restTemplate.exchange(
                "/users",
                HttpMethod.POST,
                new HttpEntity<>(invalidUser),
                MAP_TYPE
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("error")).isEqualTo("дата рождения не может быть в будущем");
    }

    @Test
    void get_ShouldReturnListWithUsers() {
        Map<String, Object> invalidUser1 = Map.of(
                "name", "Not",
                "email", "email@",
                "login", "Logiin",
                "birthday", "2001-01-01"
        );

        ResponseEntity<User> response1 = restTemplate.exchange(
                "/users",
                HttpMethod.POST,
                new HttpEntity<>(invalidUser1),
                User.class
        );

        Map<String, Object> invalidUser2 = Map.of(
                "name", "Name",
                "email", "email@",
                "login", "Login",
                "birthday", "2000-01-01"
        );

        ResponseEntity<User> response2 = restTemplate.exchange(
                "/users",
                HttpMethod.POST,
                new HttpEntity<>(invalidUser2),
                User.class
        );

        ResponseEntity<Collection<User>> getResponse = restTemplate.exchange(
                "/users",
                HttpMethod.GET,
                null,
                COLLECTION_TYPE
        );

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertTrue(getResponse.getBody().toString()
                .contains("User(id=" + response1.getBody().getId()
                        + ", email=email@, login=Logiin, name=Not, birthday=2001-01-01)"));
        assertTrue(getResponse.getBody().toString()
                .contains("User(id=" + response2.getBody().getId()
                        + ", email=email@, login=Login, name=Name, birthday=2000-01-01)"));
    }

    @Test
    void put_ShouldUpdateUserSuccessfully() {
        Map<String, Object> user1 = Map.of(
                "name", "Name",
                "email", "email@",
                "login", "Login",
                "birthday", "2021-01-01"
        );

        ResponseEntity<User> response1 = restTemplate.exchange(
                "/users",
                HttpMethod.POST,
                new HttpEntity<>(user1),
                User.class
        );

        Map<String, Object> user2 = Map.of(
                "id", response1.getBody().getId(),
                "name", "Dima",
                "email", "email@",
                "login", "Login",
                "birthday", "2000-01-01"
        );

        ResponseEntity<User> response2 = restTemplate.exchange(
                "/users",
                HttpMethod.PUT,
                new HttpEntity<>(user2),
                User.class
        );

        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response2.getBody().getName()).isEqualTo("Dima");
    }

    @Test
    void put_ShouldReturnBadRequestWithInvalideEmail() {
        Map<String, Object> user1 = Map.of(
                "name", "Name",
                "email", "email@",
                "login", "Login",
                "birthday", "2000-01-01"
        );

        ResponseEntity<User> response1 = restTemplate.exchange(
                "/users",
                HttpMethod.POST,
                new HttpEntity<>(user1),
                User.class
        );

        Map<String, Object> user2 = Map.of(
                "id", response1.getBody().getId(),
                "name", "Dasha",
                "email", "email",
                "login", "Login",
                "birthday", "2000-01-01"
        );

        ResponseEntity<Map<String, String>> response2 = restTemplate.exchange(
                "/users",
                HttpMethod.PUT,
                new HttpEntity<>(user2),
                MAP_TYPE
        );

        Map<String, Object> user3 = Map.of(
                "id", response1.getBody().getId(),
                "name", "Peter",
                "email", " ",
                "login", "Login",
                "birthday", "2000-01-01"
        );

        ResponseEntity<Map<String, String>> response3 = restTemplate.exchange(
                "/users",
                HttpMethod.PUT,
                new HttpEntity<>(user3),
                MAP_TYPE
        );
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response3.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response3.getBody().get("error")).isEqualTo("электронная почта не может быть пустой");
        assertThat(response2.getBody().get("error")).isEqualTo("электронная почта должна содержать символ @");
    }

    @Test
    void put_ShouldReturnBadRequestWithInvalideLogin() {
        Map<String, Object> user1 = Map.of(
                "name", "Name",
                "email", "email@",
                "login", "Login",
                "birthday", "2000-01-01"
        );

        ResponseEntity<User> response1 = restTemplate.exchange(
                "/users",
                HttpMethod.POST,
                new HttpEntity<>(user1),
                User.class
        );

        Map<String, Object> user2 = Map.of(
                "id", response1.getBody().getId(),
                "name", "Dasha",
                "email", "emaill@",
                "login", "",
                "birthday", "2000-01-01"
        );

        ResponseEntity<Map<String, String>> response2 = restTemplate.exchange(
                "/users",
                HttpMethod.PUT,
                new HttpEntity<>(user2),
                MAP_TYPE
        );

        Map<String, Object> user3 = Map.of(
                "id", response1.getBody().getId(),
                "name", "Peter",
                "email", "eemail@",
                "login", "Lo gi n",
                "birthday", "2000-02-01"
        );

        ResponseEntity<Map<String, String>> response3 = restTemplate.exchange(
                "/users",
                HttpMethod.PUT,
                new HttpEntity<>(user3),
                MAP_TYPE
        );
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response3.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response3.getBody().get("error"))
                .isEqualTo("логин не может быть пустым и содержать пробелы");
        assertThat(response2.getBody().get("error"))
                .isEqualTo("логин не может быть пустым и содержать пробелы");
    }

    @Test
    void put_ShouldReturnBadRequestWithInvalideBirthday() {
        Map<String, Object> user1 = Map.of(
                "name", "Name",
                "email", "email@",
                "login", "Login",
                "birthday", "2000-01-01"
        );

        ResponseEntity<User> response1 = restTemplate.exchange(
                "/users",
                HttpMethod.POST,
                new HttpEntity<>(user1),
                User.class
        );

        Map<String, Object> user2 = Map.of(
                "id", response1.getBody().getId(),
                "name", "Dasha",
                "email", "emaill@",
                "login", "Login",
                "birthday", "2028-01-01"
        );

        ResponseEntity<Map<String, String>> response2 = restTemplate.exchange(
                "/users",
                HttpMethod.PUT,
                new HttpEntity<>(user2),
                MAP_TYPE
        );

        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response2.getBody().get("error"))
                .isEqualTo("дата рождения не может быть в будущем");
    }

    @Test
    void put_ShouldReturnBadRequestWithInvalidId() {
        Map<String, Object> user1 = Map.of(
                "name", "Alena",
                "email", "email@",
                "login", "Login",
                "birthday", "2000-01-01"
        );
        ResponseEntity<User> response1 = restTemplate.exchange(
                "/users",
                HttpMethod.POST,
                new HttpEntity<>(user1),
                User.class
        );

        Map<String, Object> user2 = Map.of(
                "id", response1.getBody().getId() + 10,
                "name", "Dasha",
                "email", "emaill@",
                "login", "Login",
                "birthday", "2002-01-01"
        );
        ResponseEntity<Map<String, String>> response2 = restTemplate.exchange(
                "/users",
                HttpMethod.PUT,
                new HttpEntity<>(user2),
                MAP_TYPE
        );

        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response2.getBody().get("error"))
                .isEqualTo("Пользователь с id = " + (response1.getBody().getId() + 10) + " не найден");
    }
}
