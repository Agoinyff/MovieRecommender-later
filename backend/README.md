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

## 接口文档

### 电影检索 `GET /api/movies`
- **描述**：按名称关键字分页搜索电影。如果未提供 `query`，则返回全部电影的分页列表。
- **请求参数**：
  - `query` (可选，string)：模糊匹配的电影名称关键字。
  - `page` (可选，int，默认 `0`)：分页页码，从 0 开始。
  - `size` (可选，int，默认 `20`)：每页数量。
- **响应示例**：
```json
{
  "content": [
    {
      "id": 1,
      "name": "Toy Story",
      "publishedYear": "1995",
      "genres": "Animation|Adventure|Comedy"
    }
  ],
  "pageable": { "pageNumber": 0, "pageSize": 20 },
  "totalElements": 1,
  "last": true,
  "totalPages": 1
}
```

### 电影详情 `GET /api/movies/{id}`
- **描述**：按 ID 获取单部电影信息。
- **路径参数**：`id` (long)：电影主键。
- **响应**：`200 OK` 返回 `MovieDto`，未找到时返回 `404 Not Found`。

### 推荐列表 `GET /api/recommendations`
- **描述**：为指定用户生成推荐结果。
- **请求参数**（均放在 query string）：
  - `userId` (必填，long)：目标用户 ID。
  - `size` (可选，int，默认 `10`，范围 `1-50`)：返回的推荐条数。
  - `strategy` (必填，enum)：推荐算法，取值 `USER_BASED`（用户相似度）、`ITEM_BASED`（物品相似度）、`SLOPE_ONE`（Slope One 差值）。
- **响应示例**：
```json
[
  {
    "movieId": 357,
    "name": "Star Wars",
    "publishedYear": "1977",
    "genres": "Action|Sci-Fi",
    "score": 4.86
  }
]
```
