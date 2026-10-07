CREATE TABLE IF NOT EXISTS ratings (
  id INTEGER PRIMARY KEY,
  rating varchar(20) NOT NULL UNIQUE,
  CONSTRAINT valid_rating CHECK (rating IN ('G', 'PG', 'PG-13', 'R', 'NC-17'))
);

CREATE TABLE IF NOT EXISTS films (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name varchar(255) NOT NULL,
  description TEXT NOT NULL,
  release_date date NOT NULL,
  duration INTEGER NOT NULL,
  rating_id INTEGER NOT NULL REFERENCES ratings(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS genres (
  id INTEGER PRIMARY KEY,
  genre varchar(20) NOT NULL UNIQUE,
  CONSTRAINT valid_genre CHECK (genre IN ('Комедия', 'Драма', 'Мультфильм', 'Триллер', 'Документальный', 'Боевик'))
);

CREATE TABLE IF NOT EXISTS film_genres (
  film_id BIGINT NOT NULL,
  genre_id INTEGER NOT NULL,
  PRIMARY KEY (film_id, genre_id),
  CONSTRAINT fk_film_genres_film FOREIGN KEY (film_id) REFERENCES films(id) ON DELETE CASCADE,
  CONSTRAINT fk_film_genres_genre FOREIGN KEY (genre_id) REFERENCES genres(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  email varchar(50) NOT NULL UNIQUE,
  login varchar(50) NOT NULL UNIQUE,
  name varchar(50) NOT NULL,
  birthday date NOT NULL
);

CREATE TABLE IF NOT EXISTS friendships (
  requester_id BIGINT NOT NULL,
  addressee_id BIGINT NOT NULL,
  status varchar(20) NOT NULL,
  PRIMARY KEY (requester_id, addressee_id),
  CONSTRAINT valid_friendship_status CHECK (status IN ('CONFIRMED', 'PENDING')),
  CONSTRAINT fk_requester FOREIGN KEY (requester_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_addressee FOREIGN KEY (addressee_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS film_likes (
  film_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  PRIMARY KEY (film_id, user_id),
  CONSTRAINT fk_film_likes_film FOREIGN KEY (film_id) REFERENCES films(id) ON DELETE CASCADE,
  CONSTRAINT fk_film_likes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);