# 电影推荐系统 - 前端

基于 Vue 3 + Vite + PrimeVue 构建的现代化电影推荐系统前端应用。

## ✨ 特性

- 🎯 **多种推荐算法**：支持用户协同过滤、物品协同过滤、Slope One 三种推荐算法
- 🔍 **智能搜索**：电影库搜索功能，支持关键词搜索和分页浏览
- 🎨 **现代化 UI**：毛玻璃效果、卡片式布局、流畅的过渡动画
- 📱 **响应式设计**：完美适配桌面端和移动端
- 🚀 **性能优化**：路由懒加载、组件按需加载

## 🏗️ 项目结构

```
frontend/
├── src/
│   ├── api/                 # API 请求层
│   │   ├── http.js          # Axios 实例配置
│   │   ├── movie.js         # 电影相关 API
│   │   ├── recommendation.js # 推荐相关 API
│   │   └── index.js         # API 统一导出
│   ├── assets/              # 静态资源
│   │   └── base.css         # 全局样式
│   ├── components/          # 公共组件
│   │   ├── AppNavbar.vue    # 导航栏
│   │   ├── MovieCard.vue    # 电影卡片
│   │   ├── MovieDetailDialog.vue # 电影详情弹窗
│   │   └── LoadingSpinner.vue    # 加载动画
│   ├── router/              # 路由配置
│   │   └── index.js         # Vue Router 配置
│   ├── store/               # 状态管理
│   │   ├── index.js         # Pinia 配置
│   │   └── movie.js         # 电影状态管理
│   ├── views/               # 页面级组件
│   │   ├── HomePage.vue     # 首页
│   │   ├── MoviesPage.vue   # 电影库页面
│   │   ├── RecommendationsPage.vue # 推荐页面
│   │   └── AboutPage.vue    # 关于页面
│   ├── App.vue              # 根组件
│   └── main.js              # 入口文件
├── index.html
├── vite.config.js           # Vite 配置
└── package.json
```

## 🛠️ 技术栈

- **框架**: Vue 3 (Composition API)
- **构建工具**: Vite
- **路由**: Vue Router
- **状态管理**: Pinia
- **UI 组件库**: PrimeVue
- **HTTP 客户端**: Axios
- **工具库**: @vueuse/core

## 📦 安装依赖

```bash
npm install
```

## 🚀 开发

```bash
npm run dev
```

应用将在 http://localhost:5173 启动

## 🏭 构建

```bash
npm run build
```

## 🌐 环境变量

创建 `.env` 文件配置后端 API 地址：

```
VITE_API_BASE=http://localhost:8080/api
```

## 📄 页面说明

### 首页 (HomePage)
- 系统介绍和特性展示
- 快速导航入口

### 电影库 (MoviesPage)
- 电影列表浏览
- 关键词搜索
- 分页加载
- 点击卡片查看详情

### 个性化推荐 (RecommendationsPage)
- 输入用户 ID
- 选择推荐算法
- 设置返回条数
- 查看推荐结果（卡片式展示）
- 评分可视化

### 关于系统 (AboutPage)
- 技术栈介绍
- 算法说明
- 使用指南

## 🎨 设计特色

1. **毛玻璃效果 (Glassmorphism)**
   - 半透明背景 + backdrop-filter
   - 现代化视觉体验

2. **流畅动画**
   - 页面切换过渡
   - 卡片渐入动画
   - Hover 交互效果

3. **浅色主题**
   - 清新的配色方案
   - 渐变背景
   - 柔和的阴影

## 📝 API 接口

### 获取电影列表
```
GET /api/movies?page=0&size=20
```

### 搜索电影
```
GET /api/movies/search?keyword=xxx&page=0&size=20
```

### 获取推荐
```
GET /api/recommendations?userId=100&strategy=USER_BASED&size=10
```

## 👨‍💻 开发说明

- 使用 Composition API 编写组件
- 遵循 Vue 3 最佳实践
- 组件化和模块化设计
- 响应式布局适配

## 📄 License

MIT
