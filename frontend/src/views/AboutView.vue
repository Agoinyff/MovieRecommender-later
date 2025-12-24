<template>
  <section class="page">
    <header>
      <p class="eyebrow">About</p>
      <h1>关于系统</h1>
      <p class="lede">
        本系统通过多策略协同过滤算法，为用户提供可解释的电影推荐，并提供完整电影库检索。
      </p>
    </header>

    <div class="cards">
      <article>
        <h3>后端能力</h3>
        <ul>
          <li>推荐列表：/api/recommendations</li>
          <li>电影检索：/api/movies?page&size&query</li>
          <li>电影详情：/api/movies/{id}</li>
        </ul>
      </article>
      <article>
        <h3>前端亮点</h3>
        <ul>
          <li>玻璃拟态卡片布局</li>
          <li>推荐结果渐入动画</li>
          <li>统一 API 与状态管理</li>
        </ul>
      </article>
    </div>
  </section>
</template>

<style scoped>
.page {
  max-width: 1100px;
  margin: 0 auto;
  color: #e2e8f0;
}

.eyebrow {
  color: rgba(129, 140, 248, 0.95);
  font-weight: 700;
  letter-spacing: 0.02em;
  margin: 0 0 4px;
}

h1 {
  margin: 0 0 8px;
}

.lede {
  margin: 0 0 24px;
  color: rgba(226, 232, 240, 0.7);
}

.cards {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
}

article {
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 18px;
  padding: 18px;
  backdrop-filter: blur(16px);
}

ul {
  margin: 0;
  padding-left: 18px;
  display: grid;
  gap: 8px;
}
</style>
