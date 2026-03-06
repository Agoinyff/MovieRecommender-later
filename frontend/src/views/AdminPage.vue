<template>
  <div class="admin-page">
    <div class="page-container">
      <header class="page-header">
        <div>
          <h1>管理后台</h1>
          <p>查看最小后台统计、用户列表，并进入管理员能力页面。</p>
        </div>
        <div class="header-actions">
          <router-link to="/recommendations" class="pill-link">推荐页</router-link>
          <router-link to="/metrics" class="pill-link">监控页</router-link>
        </div>
      </header>

      <div class="stats-grid">
        <div class="stat-card">
          <span>用户数</span>
          <strong>{{ stats.userCount || 0 }}</strong>
        </div>
        <div class="stat-card">
          <span>评分数</span>
          <strong>{{ stats.ratingCount || 0 }}</strong>
        </div>
        <div class="stat-card">
          <span>电影数</span>
          <strong>{{ stats.movieCount || 0 }}</strong>
        </div>
        <div class="stat-card">
          <span>模型状态</span>
          <strong>{{ stats.modelStatus?.buildStatus || 'UNKNOWN' }}</strong>
        </div>
      </div>

      <section class="table-card">
        <div class="table-header">
          <h2>用户列表</h2>
          <button @click="loadData">刷新</button>
        </div>
        <table class="users-table" v-if="users.length">
          <thead>
            <tr>
              <th>ID</th>
              <th>用户名</th>
              <th>角色</th>
              <th>状态</th>
              <th>注册时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in users" :key="user.id">
              <td>{{ user.id }}</td>
              <td>{{ user.username }}</td>
              <td>{{ user.role }}</td>
              <td>{{ user.status }}</td>
              <td>{{ formatDate(user.createdAt) }}</td>
            </tr>
          </tbody>
        </table>
        <div v-else class="state">暂无数据</div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { getAdminStats, getAdminUsers } from '@/api';

const stats = ref({});
const users = ref([]);

const loadData = async () => {
  const [statsData, usersData] = await Promise.all([
    getAdminStats(),
    getAdminUsers({ page: 0, size: 20 })
  ]);
  stats.value = statsData;
  users.value = usersData || [];
};

const formatDate = (value) => (value ? new Date(value).toLocaleString('zh-CN') : '-');

onMounted(loadData);
</script>

<style scoped>
.admin-page {
  min-height: calc(100vh - 68px);
  background: linear-gradient(180deg, #fff7ed, #ffffff);
}

.page-container {
  max-width: 1240px;
  margin: 0 auto;
  padding: 40px 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
  margin-bottom: 24px;
}

.page-header h1 {
  margin: 0 0 8px;
  font-size: 42px;
}

.page-header p {
  margin: 0;
  color: #64748b;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.pill-link {
  height: 42px;
  display: inline-flex;
  align-items: center;
  padding: 0 16px;
  border-radius: 999px;
  text-decoration: none;
  color: #9a3412;
  background: #fff7ed;
  border: 1px solid #fdba74;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 18px;
  margin-bottom: 24px;
}

.stat-card,
.table-card {
  background: #fff;
  border-radius: 24px;
  padding: 22px;
  box-shadow: 0 18px 60px rgba(15, 23, 42, 0.06);
}

.stat-card span {
  color: #64748b;
}

.stat-card strong {
  display: block;
  margin-top: 10px;
  font-size: 28px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.table-header button {
  height: 40px;
  border: none;
  border-radius: 12px;
  background: #ea580c;
  color: #fff;
  padding: 0 14px;
}

.users-table {
  width: 100%;
  border-collapse: collapse;
}

.users-table th,
.users-table td {
  padding: 12px 8px;
  border-bottom: 1px solid #e2e8f0;
  text-align: left;
}

.state {
  color: #64748b;
}
</style>
