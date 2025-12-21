# 基于Mahout实现协同过滤推荐算法的电影推荐系统
A film recommendation system based on Mahout to implement collaborative filtering recommendation algorithm 

## 新架构（前后端分离）
- **后端**：`backend/` Spring Boot 2.7.18（Java 8），提供 `/api/movies` 和 `/api/recommendations` JSON 接口。配置见 `backend/README.md` 与 `backend/src/main/resources/application.yml`。
- **前端**：`frontend/` Vue 3 + Vite 单页应用，默认连接 `http://localhost:8080/api`，可通过 `.env` 覆盖 `VITE_API_BASE`。使用说明见 `frontend/README.md`。
- **数据库脚本**：`database/schema.sql` 定义了最小可运行表结构与示例数据，可按需导入 `movie_movies.sql` 和 `movie_movie_preferences.sql` 获取完整数据集。

界面部分截图（旧版参考）：

用户1基于User的推荐结果：

![image](https://github.com/bystc/CollaborativeFilteringMovieRecommender/raw/master/user1.png)
用户1基于Item的推荐结果：

![image](https://github.com/bystc/CollaborativeFilteringMovieRecommender/raw/master/item1.png)
用户1基于Slope one的推荐结果：

![image](https://github.com/bystc/CollaborativeFilteringMovieRecommender/raw/master/slope1.png)

#### 原博客地址：https://blog.csdn.net/bystc/article/details/87904666
