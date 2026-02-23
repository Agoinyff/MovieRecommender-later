# 电影推荐系统后端 (Movie Recommender Backend)

![Java](https://img.shields.io/badge/Java-8-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-green)
![MyBatis](https://img.shields.io/badge/MyBatis-2.3.1-blue)
![Mahout](https://img.shields.io/badge/Mahout-0.13.0-red)
![License](https://img.shields.io/badge/License-MIT-yellow)

基于 **Apache Mahout** 协同过滤算法的电影推荐系统后端服务，提供电影检索、个性化推荐、评分管理、性能监控等 RESTful API。

---

## 📑 目录

- [项目简介](#-项目简介)
- [核心功能](#-核心功能)
- [技术栈](#-技术栈)
- [系统架构](#-系统架构)
- [环境要求](#-环境要求)
- [快速开始](#-快速开始)
- [配置说明](#-配置说明)
- [API 文档](#-api-文档)
- [推荐算法](#-推荐算法)
- [性能优化](#-性能优化)
- [项目结构](#-项目结构)
- [FAQ](#-faq)

---

## 🎬 项目简介

这是一个基于 **协同过滤算法** 的电影推荐系统后端实现，采用前后端分离架构。系统集成了 Apache Mahout 机器学习库，支持多种推荐策略：

- **用户协同过滤** (User-Based Collaborative Filtering)
- **物品协同过滤** (Item-Based Collaborative Filtering)  
- **Slope One 算法** (基于评分差值的预测)

同时集成 **TMDb (The Movie Database) API**，自动获取高质量电影海报，提供完整的电影元数据展示。

---

## ✨ 核心功能

### 1. 电影检索与管理
- ✅ 分页查询电影列表
- ✅ 关键词模糊搜索
- ✅ 电影详情查询
- ✅ 电影海报自动获取（集成 TMDb API）

### 2. 个性化推荐
- ✅ 三种协同过滤算法可选
- ✅ 基于用户历史评分的推荐
- ✅ 推荐结果缓存（30 分钟有效期）
- ✅ 批量查询优化（避免 N+1 问题）

### 3. 评分系统
- ✅ 用户电影评分记录
- ✅ 评分查询与修改
- ✅ 评分统计（支持 Streaming ResultHandler）

### 4. 性能监控
- ✅ 系统健康检查
- ✅ 缓存统计（命中率、大小、驱逐次数）
- ✅ 内存使用监控
- ✅ 推荐性能指标记录

### 5. 数据模型管理
- ✅ 异步数据模型预热
- ✅ 定时自动重建（可配置）
- ✅ 流式加载大规模评分数据
- ✅ 模型状态查询

### 6. 海报管理
- ✅ 批量更新电影海报（后台异步）
- ✅ 单个电影海报更新
- ✅ 快速更新前 N 部电影
- ✅ 自动限流（避免 API 超限）

---

## 🛠 技术栈

| 分类 | 技术 | 版本 | 说明 |
|------|------|------|------|
| **核心框架** | Spring Boot | 2.7.18 | 企业级应用框架 |
| **语言** | Java | 8 | JDK 1.8 |
| **持久层** | MyBatis | 2.3.1 | SQL 映射框架 |
| **数据库** | MySQL | 5.7+ | 关系型数据库 |
| **推荐引擎** | Apache Mahout | 0.13.0 | 机器学习库 |
| **缓存** | Caffeine | - | 高性能本地缓存 |
| **API 文档** | SpringDoc OpenAPI | 1.7.0 | Swagger 3.0 |
| **外部 API** | TMDb API | v3 | 电影元数据服务 |
| **构建工具** | Maven | 3.8+ | 项目管理 |

---

## 🏗 系统架构

### 分层架构

```
┌─────────────────────────────────────────────────────┐
│                   Controller Layer                  │
│  (MovieController, RecommendationController, etc.)  │
└─────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────┐
│                    Service Layer                     │
│  (MovieService, RecommendationService, etc.)        │
└─────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────┐
│                  Persistence Layer                   │
│        (MyBatis Mapper + MySQL Database)            │
└─────────────────────────────────────────────────────┘
```

### 推荐引擎架构

```
┌──────────────┐
│ User Request │
└──────┬───────┘
       ↓
┌──────────────────────┐
│ Check Cache (30 min) │
└──────┬───────────────┘
       ↓
┌──────────────────────┐     ┌─────────────────┐
│ Load DataModel       │────→│ Caffeine Cache  │
│ (User-Item Matrix)   │     └─────────────────┘
└──────┬───────────────┘
       ↓
┌──────────────────────────────────────────┐
│          Select Recommender              │
├──────────────┬───────────┬───────────────┤
│ User-Based   │Item-Based │  Slope One    │
└──────┬───────┴─────┬─────┴───────┬───────┘
       │             │             │
       └─────────────┼─────────────┘
                     ↓
      ┌──────────────────────────┐
      │ Mahout Recommendation    │
      └──────────┬───────────────┘
                 ↓
      ┌──────────────────────────┐
      │ Batch Query Movie Info   │
      └──────────┬───────────────┘
                 ↓
      ┌──────────────────────────┐
      │   Return Results         │
      └──────────────────────────┘
```

---

## 📋 环境要求

### 必需
- **Java**: JDK 8 或更高版本
- **Maven**: 3.8+
- **MySQL**: 5.7 或更高版本
- **内存**: 建议至少 2GB 可用内存（用于数据模型构建）

### 可选
- **TMDb API Key**: 用于获取电影海报（[申请地址](https://www.themoviedb.org/settings/api)）

---

## 🚀 快速开始

### 1. 克隆项目

```bash
git clone <repository-url>
cd MovieRecommender-later/backend
```

### 2. 配置数据库

#### 创建数据库
```sql
CREATE DATABASE movie DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### 导入数据
```bash
# 导入基础表结构
mysql -u root -p movie < ../database/schema.sql

# （可选）导入完整数据集
mysql -u root -p movie < ../database/movie_movies.sql
mysql -u root -p movie < ../database/movie_movie_preferences.sql
```

#### 添加海报字段（已完成则跳过）
```sql
USE movie;
ALTER TABLE movies ADD COLUMN poster_url VARCHAR(500) NULL COMMENT '电影海报URL';
```

### 3. 配置文件

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/movie?useSSL=false&useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: root          # 修改为你的数据库用户名
    password: 123456        # 修改为你的数据库密码
    driver-class-name: com.mysql.cj.jdbc.Driver

# TMDb API 配置（可选）
tmdb:
  api:
    key: your_api_key_here                    # 替换为你的 API Key
    read-token: your_read_access_token_here   # 替换为你的 Read Access Token
    base-url: https://api.themoviedb.org/3
    image-base-url: https://image.tmdb.org/t/p/w500
```

### 4. 运行项目

```bash
# 使用 Maven
mvn spring-boot:run

# 或者先打包后运行
mvn clean package -DskipTests
java -jar target/movie-recommender-backend-1.0.0.jar
```

### 5. 验证运行

访问以下地址验证服务启动：

- **API Base URL**: http://localhost:8080/api
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/api/health

---

## ⚙️ 配置说明

### 数据库配置

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/movie
    username: root
    password: 123456
```

### MyBatis 配置

```yaml
mybatis:
  configuration:
    map-underscore-to-camel-case: true  # 下划线转驼峰
    default-statement-timeout: 30        # 查询超时 30 秒
```

### 缓存配置

```yaml
spring:
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=30m  # 最大 1000 条，30 分钟过期
```

### 数据模型构建配置

```yaml
model:
  build:
    cron: "0 0 * * * ?"      # 每小时重建一次
    timeout: 300000           # 构建超时 5 分钟
    startup-delay: 30000      # 启动后延迟 30 秒预热
```

### TMDb API 配置

```yaml
tmdb:
  api:
    key: your_api_key                     # API Key
    read-token: your_read_access_token    # Read Access Token
    base-url: https://api.themoviedb.org/3
    image-base-url: https://image.tmdb.org/t/p/w500
```

---

## 📖 API 文档

### 完整 API 文档

访问 Swagger UI 查看完整 API 文档：
```
http://localhost:8080/swagger-ui.html
```

### 核心接口概览

#### 1. 电影检索

**GET** `/api/movies`

查询电影列表（支持分页和关键词搜索）

**请求参数**：
- `query` (可选): 电影名称关键字
- `page` (可选, 默认 0): 页码
- `size` (可选, 默认 20): 每页数量

**响应示例**：
```json
{
  "content": [
    {
      "id": 1,
      "name": "Toy Story",
      "publishedYear": "1995",
      "genres": "Animation, Children's, Comedy",
      "posterUrl": "https://image.tmdb.org/t/p/w500/xxx.jpg"
    }
  ],
  "totalElements": 3883,
  "totalPages": 195,
  "number": 0,
  "size": 20
}
```

---

**GET** `/api/movies/{id}`

获取电影详情

**响应示例**：
```json
{
  "id": 1,
  "name": "Toy Story",
  "publishedYear": "1995",
  "genres": "Animation, Children's, Comedy",
  "posterUrl": "https://image.tmdb.org/t/p/w500/xxx.jpg"
}
```

---

#### 2. 个性化推荐

**GET** `/api/recommendations/user/{userId}`

为用户生成个性化推荐

**路径参数**：
- `userId`: 用户 ID

**请求参数**：
- `size` (可选, 默认 10): 返回条数 (1-50)
- `strategy` (必填): 推荐策略
  - `USER_BASED`: 用户协同过滤
  - `ITEM_BASED`: 物品协同过滤
  - `SLOPE_ONE`: Slope One 算法

**响应示例**：
```json
[
  {
    "movieId": 357,
    "name": "Star Wars",
    "publishedYear": "1977",
    "genres": "Action, Sci-Fi",
    "posterUrl": "https://image.tmdb.org/t/p/w500/xxx.jpg",
    "score": 4.86
  }
]
```

---

#### 3. 评分管理

**POST** `/api/ratings`

提交或更新评分

**请求体**：
```json
{
  "userId": 100,
  "movieId": 1,
  "rating": 4.5
}
```

**响应**：
```json
{
  "success": true,
  "message": "评分已保存"
}
```

---

**GET** `/api/ratings/user/{userId}`

获取用户评分记录

**响应示例**：
```json
[
  {
    "userId": 100,
    "movieId": 1,
    "movieName": "Toy Story",
    "rating": 4.5,
    "timestamp": "2026-01-19T10:30:00"
  }
]
```

---

#### 4. 性能监控

**GET** `/api/health`

系统健康检查

**响应示例**：
```json
{
  "status": "ok",
  "timestamp": "2026-01-19T15:00:00",
  "version": "1.0.0"
}
```

---

**GET** `/api/metrics/cache`

缓存统计信息

**响应示例**：
```json
{
  "movies": {
    "hitRate": 0.85,
    "hitCount": 1200,
    "missCount": 200,
    "size": 150
  },
  "dataModel": {
    "hitRate": 1.0,
    "hitCount": 50,
    "missCount": 0,
    "size": 1
  },
  "recommendations": {
    "hitRate": 0.75,
    "hitCount": 300,
    "missCount": 100,
    "size": 45
  }
}
```

---

**GET** `/api/metrics/memory`

内存使用统计

**响应示例**：
```json
{
  "totalMemoryMB": 1024,
  "usedMemoryMB": 512,
  "freeMemoryMB": 512,
  "maxMemoryMB": 2048
}
```

---

#### 5. 海报管理

**POST** `/api/posters/update-all`

批量更新所有缺失的电影海报（后台异步执行）

**响应示例**：
```json
{
  "message": "海报批量更新任务已启动，正在后台执行",
  "status": "started"
}
```

---

**POST** `/api/posters/update-first?count=100`

快速更新前 N 部电影海报（用于快速预览）

**响应示例**：
```json
{
  "message": "正在更新前 100 部电影的海报",
  "count": 100,
  "status": "started"
}
```

---

**GET** `/api/model/status`

查询数据模型状态

**响应示例**：
```json
{
  "status": "ready",
  "numUsers": 6040,
  "numItems": 3883,
  "lastBuildTime": "2026-01-19T14:00:00",
  "buildDurationSeconds": 12.5
}
```

---

## 🧠 推荐算法

### 1. 用户协同过滤 (USER_BASED)

**原理**：找到与目标用户相似的其他用户，推荐这些相似用户喜欢的电影。

**相似度计算**：Pearson 相关系数

**优点**：
- 适合冷启动场景
- 能发现用户的潜在兴趣

**缺点**：
- 用户数量大时计算开销高
- 推荐结果变化较快

**适用场景**：用户数量较少，用户兴趣变化快

---

### 2. 物品协同过滤 (ITEM_BASED)

**原理**：基于用户历史评分，推荐与其喜欢的电影相似的其他电影。

**相似度计算**：Pearson 相关系数

**优点**：
- 推荐结果更稳定
- 可解释性强（"因为你喜欢 A，推荐 B"）
- 适合物品数量相对稳定的场景

**缺点**：
- 物品数量大时需要预计算相似度矩阵

**适用场景**：电影库相对稳定，用户评分数据充足

---

### 3. Slope One 算法 (SLOPE_ONE)

**原理**：基于物品之间的评分差值进行预测，计算简单高效。

**优点**：
- 计算速度快
- 实现简单
- 适合大规模数据集

**缺点**：
- 准确度可能略低于前两种

**适用场景**：需要快速响应，数据量大

---

## ⚡ 性能优化

### 1. 缓存策略

#### 三级缓存设计

```
Level 1: DataModel Cache (数据模型缓存)
└─ 存储: 用户-电影评分矩阵
└─ 过期: 手动更新或定时重建
└─ 大小: 1 个 (全局单例)

Level 2: Recommendations Cache (推荐结果缓存)
└─ 存储: userId_size_strategy → List<RecommendationDto>
└─ 过期: 30 分钟
└─ 大小: 最多 1000 条

Level 3: Movies Cache (电影信息缓存)
└─ 存储: 电影查询结果
└─ 过期: 写入后 30 分钟
└─ 大小: 最多 1000 条
```

### 2. 批量查询优化

**问题**：N+1 查询问题

**优化前**：
```java
for (RecommendedItem item : items) {
    MovieEntity movie = movieMapper.findById(item.getItemID()); // N 次查询
}
```

**优化后**：
```java
List<Long> movieIds = items.stream()
    .map(RecommendedItem::getItemID)
    .collect(Collectors.toList());
List<MovieEntity> movies = movieMapper.findByIds(movieIds); // 1 次查询
```

### 3. 流式数据加载

使用 MyBatis `ResultHandler` 流式处理大规模评分数据，避免一次性加载导致 OOM：

```java
ratingMapper.streamAllRatings(resultContext -> {
    RatingEntity rating = resultContext.getResultObject();
    // 逐条处理，内存占用恒定
});
```

### 4. 异步任务

- **数据模型预热**：启动后异步构建，不阻塞应用启动
- **海报更新**：后台异步更新，立即返回响应
- **定时重建**：定时任务自动重建数据模型

### 5. 性能指标

| 操作 | 优化前 | 优化后 | 提升 |
|------|--------|--------|------|
| 电影列表查询 (20 条) | ~20 秒 | ~100 ms | **200x** |
| 推荐结果查询 (首次) | ~5 秒 | ~5 秒 | - |
| 推荐结果查询 (缓存) | - | ~10 ms | **500x** |
| 数据模型构建 | N/A | ~12 秒 | - |

---

## 📁 项目结构

```
backend/
├── src/main/java/com/rcd/movierecommender/backend/
│   ├── config/               # 配置类
│   │   ├── AsyncConfig.java           # 异步任务配置
│   │   ├── CacheConfig.java           # 缓存配置
│   │   ├── OpenApiConfig.java         # Swagger 配置
│   │   └── TmdbConfig.java            # TMDb API 配置
│   │
│   ├── controller/           # 控制器层
│   │   ├── HealthController.java      # 健康检查
│   │   ├── MetricsController.java     # 性能监控
│   │   ├── ModelController.java       # 数据模型管理
│   │   ├── MovieController.java       # 电影检索
│   │   ├── PosterController.java      # 海报管理
│   │   ├── RatingController.java      # 评分管理
│   │   └── RecommendationController.java  # 推荐接口
│   │
│   ├── dto/                  # 数据传输对象
│   │   ├── MovieDto.java              # 电影 DTO
│   │   ├── RatingDto.java             # 评分 DTO
│   │   ├── RecommendationDto.java     # 推荐结果 DTO
│   │   └── RecommendationStrategy.java # 推荐策略枚举
│   │
│   ├── entity/               # 实体类
│   │   ├── MovieEntity.java           # 电影实体
│   │   └── RatingEntity.java          # 评分实体
│   │
│   ├── exception/            # 异常处理
│   │   ├── BusinessException.java     # 业务异常
│   │   ├── ErrorCode.java             # 错误码
│   │   └── GlobalExceptionHandler.java # 全局异常处理器
│   │
│   ├── mapper/               # MyBatis Mapper
│   │   ├── MovieMapper.java           # 电影数据访问
│   │   └── RatingMapper.java          # 评分数据访问
│   │
│   ├── service/              # 业务服务层
│   │   ├── MovieService.java          # 电影服务
│   │   ├── RatingService.java         # 评分服务
│   │   ├── RecommendationService.java # 推荐服务
│   │   ├── TmdbService.java           # TMDb API 服务
│   │   ├── PosterUpdateService.java   # 海报更新服务
│   │   ├── ModelWarmupService.java    # 数据模型预热
│   │   └── recommender/               # 推荐算法实现
│   │       └── slopeone/
│   │           └── CustomSlopeOneRecommender.java
│   │
│   └── MovieRecommenderApplication.java  # 主应用类
│
├── src/main/resources/
│   ├── application.yml       # 主配置文件
│   └── logback-spring.xml    # 日志配置
│
├── pom.xml                   # Maven 项目配置
└── README.md                 # 本文档
```

---

## ❓ FAQ

### Q1: 启动后推荐接口返回空列表？

**A**: 数据模型正在构建中，请稍等 30 秒后重试，或访问 `/api/model/status` 查看构建状态。

---

### Q2: 如何提高推荐性能？

**A**: 
1. 增加 JVM 堆内存：`java -Xmx2g -jar xxx.jar`
2. 启用缓存（已默认启用）
3. 使用 `ITEM_BASED` 或 `SLOPE_ONE` 策略（比 `USER_BASED` 快）

---

### Q3: 电影没有海报图片？

**A**: 
1. 确认已配置 TMDb API Key
2. 调用海报更新 API：
   ```bash
   curl -X POST http://localhost:8080/api/posters/update-first?count=100
   ```
3. 等待 30 秒后刷新

---

### Q4: 数据模型构建失败（OOM）？

**A**: 
1. 增加 JVM 堆内存：`-Xmx4g`
2. 减少数据量（删除部分评分记录）
3. 调整 `model.build.timeout` 配置

---

### Q5: 如何清除缓存？

**A**: 访问 `GET /api/metrics/cache/clear`

---

### Q6: 如何修改数据模型重建频率？

**A**: 修改 `application.yml` 中的 `model.build.cron` 配置：
```yaml
model:
  build:
    cron: "0 0 * * * ?"  # 每小时一次
    # cron: "0 0 0 * * ?"  # 每天午夜一次
```

---

## 📝 开发说明

### 添加新的推荐算法

1. 在 `RecommendationStrategy` 枚举中添加新策略
2. 在 `RecommendationService.buildRecommender()` 中添加对应实现
3. 更新 API 文档

### 自定义缓存策略

修改 `CacheConfig.java`：

```java
@Bean
public Caffeine<Object, Object> caffeineConfig() {
    return Caffeine.newBuilder()
        .maximumSize(2000)  // 增加缓存容量
        .expireAfterWrite(60, TimeUnit.MINUTES)  // 延长过期时间
        .recordStats();
}
```

---

## 📄 许可证



---

## 👥 贡献者



---



---

**最后更新**: 2026-01-19
