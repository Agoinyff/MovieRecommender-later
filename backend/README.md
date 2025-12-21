# Movie Recommender Backend

Spring Boot 2.7.18 + Java 8 implementation that exposes movie search and recommendation APIs.

## Prerequisites
- Java 8+
- Maven 3.8+
- MySQL 5.7+ with the `movie` schema

## Configure
Update `src/main/resources/application.yml` with your database username/password. Import `database/schema.sql` first, then (optionally) the legacy dumps `movie_movies.sql` and `movie_movie_preferences.sql` for the full dataset.

## Run locally
```bash
mvn spring-boot:run
```

APIs are served on `http://localhost:8080/api`:
- `GET /api/movies?query=toy&page=0&size=20`
- `GET /api/recommendations?userId=100&strategy=USER_BASED&size=10`
