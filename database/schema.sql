-- Schema for the Spring Boot backend. Import the legacy dataset with movie_movies.sql and movie_movie_preferences.sql after creating the schema.
CREATE DATABASE IF NOT EXISTS movie CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE movie;

DROP TABLE IF EXISTS movie_preferences;
DROP TABLE IF EXISTS movies;

CREATE TABLE movies (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    published_year VARCHAR(4),
    type VARCHAR(100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE movie_preferences (
    userID INT NOT NULL,
    movieID INT NOT NULL,
    preference INT NOT NULL DEFAULT 0,
    timestamp INT NOT NULL DEFAULT 0,
    PRIMARY KEY (userID, movieID),
    KEY idx_movie_preferences_user (userID),
    KEY idx_movie_preferences_movie (movieID),
    CONSTRAINT fk_movie_preferences_movie FOREIGN KEY (movieID) REFERENCES movies (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Minimal seed data that matches the Vue demo. Replace with your own imports or use the full dumps for production usage.
INSERT INTO movies (id, name, published_year, type) VALUES
  (1, 'Toy Story', '1995', 'Animation, Children''s, Comedy'),
  (2, 'Jumanji', '1995', 'Adventure, Children''s, Fantasy'),
  (3, 'Grumpier Old Men', '1995', 'Comedy, Romance'),
  (4, 'Waiting to Exhale', '1995', 'Comedy, Drama'),
  (5, 'Father of the Bride Part II', '1995', 'Comedy');

INSERT INTO movie_preferences (userID, movieID, preference, timestamp) VALUES
  (100, 1, 5, 963983415),
  (100, 2, 3, 963983489),
  (101, 1, 4, 963983654),
  (101, 3, 2, 963983808),
  (102, 2, 4, 963983915),
  (102, 4, 5, 963984012);
