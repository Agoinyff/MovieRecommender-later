<template>
  <div class="auth-page">
    <div class="auth-card">
      <h1>注册</h1>
      <p>创建账号后即可建立你的评分画像并获得推荐。</p>

      <form @submit.prevent="handleSubmit" class="form">
        <label>
          用户名
          <input v-model.trim="form.username" type="text" minlength="3" required />
        </label>
        <label>
          密码
          <input v-model="form.password" type="password" minlength="6" required />
        </label>
        <p v-if="error" class="error">{{ error }}</p>
        <button class="submit-btn" :disabled="authStore.loading">{{ authStore.loading ? '注册中...' : '注册并登录' }}</button>
      </form>

      <router-link to="/login" class="switch-link">已经有账号？去登录</router-link>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/store/auth';

const router = useRouter();
const authStore = useAuthStore();
const error = ref('');
const form = reactive({ username: '', password: '' });

const handleSubmit = async () => {
  error.value = '';
  try {
    await authStore.register(form);
    router.push('/recommendations');
  } catch (err) {
    error.value = err.response?.data?.message || err.message || '注册失败';
  }
};
</script>

<style scoped>
.auth-page {
  min-height: calc(100vh - 68px);
  display: grid;
  place-items: center;
  background: linear-gradient(180deg, #fff7ed, #ffffff);
  padding: 24px;
}

.auth-card {
  width: min(100%, 420px);
  padding: 32px;
  border-radius: 24px;
  background: #fff;
  box-shadow: 0 22px 70px rgba(15, 23, 42, 0.1);
}

.form {
  display: grid;
  gap: 16px;
  margin-top: 20px;
}

label {
  display: grid;
  gap: 8px;
  font-weight: 600;
}

input {
  height: 48px;
  border-radius: 14px;
  border: 1px solid #cbd5e1;
  padding: 0 14px;
}

.submit-btn {
  height: 50px;
  border: none;
  border-radius: 14px;
  background: linear-gradient(135deg, #ea580c, #f59e0b);
  color: #fff;
  font-weight: 800;
}

.switch-link {
  display: inline-block;
  margin-top: 20px;
  color: #c2410c;
}

.error {
  color: #dc2626;
  margin: 0;
}
</style>
