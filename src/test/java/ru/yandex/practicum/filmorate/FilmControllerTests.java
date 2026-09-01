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
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class FilmControllerTests {

    private static final ParameterizedTypeReference<Map<String, String>> MAP_TYPE =
            new ParameterizedTypeReference<>() {
            };
    private static final ParameterizedTypeReference<Collection<Film>> COLLECTION_TYPE =
            new ParameterizedTypeReference<>() {
            };

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void post_ShouldAddFilmSuccessfully() {
        Map<String, Object> filmRequest = Map.of(
                "id", 1,
                "name", "Test Film",
                "description", "Valid description",
                "releaseDate", "2021-01-01",
                "duration", 91
        );

        ResponseEntity<Film> response = restTemplate.postForEntity("/films", filmRequest, Film.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Test Film");
    }

    @Test
    void post_ShouldReturnBadRequestWhenNameIsBlank() {
        Map<String, Object> invalidFilm = Map.of(
                "id", 104,
                "name", "   ",
                "description", "Desc",
                "releaseDate", "2000-01-01",
                "duration", 90
        );

        ResponseEntity<Map<String, String>> response = restTemplate.exchange(
                "/films",
                HttpMethod.POST,
                new HttpEntity<>(invalidFilm),
                MAP_TYPE
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("error")).isEqualTo("название не может быть пустым");
    }

    @Test
    void post_ShouldReturnBadRequestWithDescriptionLengthMore200Symbols() {
        Map<String, Object> invalidFilm = Map.of(
                "id", 9,
                "name", "NotBlank",
                "description", "D".repeat(201),
                "releaseDate", "2000-01-01",
                "duration", 90
        );
        ResponseEntity<Map<String, String>> response = restTemplate.exchange(
                "/films",
                HttpMethod.POST,
                new HttpEntity<>(invalidFilm),
                MAP_TYPE
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("error")).isEqualTo("длина описания превышает 200 символов");
    }

    @Test
    void post_ShouldReturnBadRequestWithInvalidReleaseDate() {
        Map<String, Object> invalidFilm = Map.of(
                "id", 15,
                "name", "NotBlank",
                "description", "D",
                "releaseDate", "1895-12-27",
                "duration", 90
        );
        ResponseEntity<Map<String, String>> response = restTemplate.exchange(
                "/films",
                HttpMethod.POST,
                new HttpEntity<>(invalidFilm),
                MAP_TYPE
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("error"))
                .isEqualTo("дата релиза должна быть не раньше 28 декабря 1895 года");
    }

    @Test
    void post_ShouldReturnBadRequestWithInvalidDuration() {
        Map<String, Object> film = Map.of(
                "id", 10,
                "name", "NotBlank",
                "description", "D",
                "releaseDate", "1895-12-28",
                "duration", -150
        );
        ResponseEntity<Map<String, String>> response = restTemplate.exchange(
                "/films",
                HttpMethod.POST,
                new HttpEntity<>(film),
                MAP_TYPE
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("error"))
                .isEqualTo("продолжительность фильма в секундах должна быть больше нуля");
    }

    @Test
    void get_ShouldReturnListWithFilms() {
        Map<String, Object> film1 = Map.of(
                "id", 75,
                "name", "NotBlank",
                "description", "D",
                "releaseDate", "1895-12-28",
                "duration", 150
        );
        ResponseEntity<Map<String, String>> response1 = restTemplate.exchange(
                "/films",
                HttpMethod.POST,
                new HttpEntity<>(film1),
                MAP_TYPE
        );

        Map<String, Object> film2 = Map.of(
                "id", 3,
                "name", "Name",
                "description", "D",
                "releaseDate", "1895-12-28",
                "duration", 140
        );
        ResponseEntity<Map<String, String>> response2 = restTemplate.exchange(
                "/films",
                HttpMethod.POST,
                new HttpEntity<>(film2),
                MAP_TYPE
        );

        ResponseEntity<Collection<Film>> getResponse = restTemplate.exchange(
                "/films",
                HttpMethod.GET,
                null,
                COLLECTION_TYPE
        );
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertTrue(getResponse.getBody().toString()
                .contains("Film(id=75, name=NotBlank, description=D, releaseDate=1895-12-28, duration=PT2M30S)")
        );
        assertTrue(getResponse.getBody().toString()
                .contains("Film(id=3, name=Name, description=D, releaseDate=1895-12-28, duration=PT2M20S)")
        );
    }

    @Test
    void put_shouldReturnUpdatedFilmSuccessfully() {
        Map<String, Object> film1 = Map.of(
                "id", 2,
                "name", "NotBlank",
                "description", "D",
                "releaseDate", "1895-12-28",
                "duration", 150
        );
        ResponseEntity<Film> response1 = restTemplate.exchange(
                "/films",
                HttpMethod.POST,
                new HttpEntity<>(film1),
                Film.class
        );

        Map<String, Object> invalidFilm2 = Map.of(
                "id", 2,
                "name", "Name",
                "description", "D",
                "releaseDate", "1895-12-28",
                "duration", 140
        );
        ResponseEntity<Film> putResponse = restTemplate.exchange(
                "/films",
                HttpMethod.PUT,
                new HttpEntity<>(invalidFilm2),
                Film.class
        );
        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(putResponse.getBody().getName()).isEqualTo("Name");

    }

    @Test
    void put_ShouldReturnBadRequestWithInvalidName() {
        Map<String, Object> film1 = Map.of(
                "id", 2,
                "name", "NotBlank",
                "description", "D",
                "releaseDate", "1895-12-28",
                "duration", 150
        );
        ResponseEntity<Film> response1 = restTemplate.exchange(
                "/films",
                HttpMethod.POST,
                new HttpEntity<>(film1),
                Film.class
        );

        Map<String, Object> invalidFilm2 = Map.of(
                "id", 2,
                "name", " ",
                "description", "D",
                "releaseDate", "1895-12-28",
                "duration", 140
        );
        ResponseEntity<Map<String, String>> putResponse = restTemplate.exchange(
                "/films",
                HttpMethod.PUT,
                new HttpEntity<>(invalidFilm2),
                MAP_TYPE
        );

        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(putResponse.getBody().get("error")).isEqualTo("название не может быть пустым");
    }

    @Test
    void put_ShouldReturnBadRequestWithInvalidDescription() {
        Map<String, Object> film1 = Map.of(
                "id", 2,
                "name", "NotBlank",
                "description", "D",
                "releaseDate", "1895-12-28",
                "duration", 150
        );
        ResponseEntity<Film> response1 = restTemplate.exchange(
                "/films",
                HttpMethod.POST,
                new HttpEntity<>(film1),
                Film.class
        );

        Map<String, Object> invalidFilm2 = Map.of(
                "id", 2,
                "name", "Name",
                "description", "D".repeat(201),
                "releaseDate", "1895-12-28",
                "duration", 140
        );
        ResponseEntity<Map<String, String>> putResponse = restTemplate.exchange(
                "/films",
                HttpMethod.PUT,
                new HttpEntity<>(invalidFilm2),
                MAP_TYPE
        );

        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(putResponse.getBody().get("error")).isEqualTo("длина описания превышает 200 символов");
    }

    @Test
    void put_ShouldReturnBadRequestWithInvalidReleaseDate() {
        Map<String, Object> film1 = Map.of(
                "id", 2,
                "name", "NotBlank",
                "description", "D",
                "releaseDate", "1895-12-28",
                "duration", 150
        );
        ResponseEntity<Film> response1 = restTemplate.exchange(
                "/films",
                HttpMethod.POST,
                new HttpEntity<>(film1),
                Film.class
        );

        Map<String, Object> invalidFilm2 = Map.of(
                "id", 2,
                "name", "Name",
                "description", "D",
                "releaseDate", "1895-12-27",
                "duration", 140
        );
        ResponseEntity<Map<String, String>> putResponse = restTemplate.exchange(
                "/films",
                HttpMethod.PUT,
                new HttpEntity<>(invalidFilm2),
                MAP_TYPE
        );

        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(putResponse.getBody().get("error"))
                .isEqualTo("дата релиза должна быть не раньше 28 декабря 1895 года");
    }

    @Test
    void put_ShouldReturnBadRequestWithInvalidDuration() {
        Map<String, Object> film1 = Map.of(
                "id", 2,
                "name", "NotBlank",
                "description", "D",
                "releaseDate", "1895-12-28",
                "duration", 150
        );
        ResponseEntity<Film> response1 = restTemplate.exchange(
                "/films",
                HttpMethod.POST,
                new HttpEntity<>(film1),
                Film.class
        );

        Map<String, Object> invalidFilm2 = Map.of(
                "id", 2,
                "name", "Name",
                "description", "D",
                "releaseDate", "1895-12-28",
                "duration", -8
        );
        ResponseEntity<Map<String, String>> putResponse = restTemplate.exchange(
                "/films",
                HttpMethod.PUT,
                new HttpEntity<>(invalidFilm2),
                MAP_TYPE
        );

        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(putResponse.getBody().get("error"))
                .isEqualTo("продолжительность фильма в секундах должна быть больше нуля");
    }

    @Test
    void put_ShouldReturnBadRequestWithInvalidId() {
        Map<String, Object> film1 = Map.of(
                "id", 2,
                "name", "NotBlank",
                "description", "D",
                "releaseDate", "1895-12-28",
                "duration", 150
        );
        ResponseEntity<Film> response1 = restTemplate.exchange(
                "/films",
                HttpMethod.POST,
                new HttpEntity<>(film1),
                Film.class
        );

        Map<String, Object> invalidFilm2 = Map.of(
                "id", 5,
                "name", "Name",
                "description", "D",
                "releaseDate", "1895-12-28",
                "duration", 8
        );
        ResponseEntity<Map<String, String>> putResponse = restTemplate.exchange(
                "/films",
                HttpMethod.PUT,
                new HttpEntity<>(invalidFilm2),
                MAP_TYPE
        );

        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(putResponse.getBody().get("error"))
                .isEqualTo("Фильм с id = " + 5 + " не найден");
    }
}
