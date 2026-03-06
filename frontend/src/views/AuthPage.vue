<template>
  <div class="auth-page">
    <div class="auth-container" :class="{ 'is-register': mode === 'register' }">
      <div class="auth-left">
        <div class="brand">
          <i class="pi pi-video"></i>
          <span>智能电影推荐</span>
        </div>
        <div class="welcome-text">
          <h2>{{ mode === 'login' ? '探索你的影视宇宙' : '开启专属推荐之旅' }}</h2>
          <p>{{ mode === 'login' ? '基于深度学习的个性化引擎，越用越懂你的品味。' : '只需几步，构建你的专属观影画像。' }}</p>
        </div>
        <div class="auth-decor">
          <div class="decor-circle circle-1"></div>
          <div class="decor-circle circle-2"></div>
          <div class="glass-card"></div>
        </div>
      </div>
      
      <div class="auth-right">
        <div class="auth-right-content">
          <div class="auth-header">
            <h1>{{ mode === 'login' ? '欢迎回来' : '创建你的账号' }}</h1>
            <p>{{ mode === 'login' ? '登录后继续查看围绕当前用户生成的推荐和评分。' : '注册后即可建立自己的评分记录和推荐画像。' }}</p>
          </div>

          <div class="auth-tabs">
            <button
              type="button"
              class="tab-btn"
              :class="{ active: mode === 'login' }"
              @click="mode = 'login'"
            >
              登录
            </button>
            <button
              type="button"
              class="tab-btn"
              :class="{ active: mode === 'register' }"
              @click="mode = 'register'"
            >
              注册
            </button>
          </div>

          <form class="auth-form" @submit.prevent="handleSubmit">
            <TransitionGroup name="list" tag="div" class="form-fields-wrapper">
              <div v-if="mode === 'register'" key="display" class="form-field">
                <label>昵称</label>
                <span class="p-input-icon-left w-full relative">
                  <i class="pi pi-id-card input-icon"></i>
                  <input v-model.trim="form.displayName" type="text" class="input" placeholder="例如：星幕旅人" />
                </span>
              </div>

              <div key="username" class="form-field">
                <label>用户名</label>
                <span class="p-input-icon-left w-full relative">
                  <i class="pi pi-user input-icon"></i>
                  <input v-model.trim="form.username" type="text" class="input" placeholder="3-32 位，字母 / 数字 / 下划线" />
                </span>
              </div>

              <div key="password" class="form-field">
                <label>密码</label>
                <span class="p-input-icon-left w-full relative">
                   <i class="pi pi-lock input-icon"></i>
                   <input v-model="form.password" type="password" class="input" placeholder="至少 6 位" />
                </span>
              </div>
            </TransitionGroup>

            <Transition name="fade">
              <p v-if="error" class="feedback error">
                <i class="pi pi-exclamation-circle"></i> {{ error }}
              </p>
            </Transition>

            <button type="submit" class="submit-btn" :disabled="authStore.loading">
              <i :class="authStore.loading ? 'pi pi-spin pi-spinner' : mode === 'login' ? 'pi pi-sign-in' : 'pi pi-user-plus'"></i>
              <span>{{ submitText }}</span>
            </button>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/store/auth';

const authStore = useAuthStore();
const route = useRoute();
const router = useRouter();
const mode = ref('login');
const error = ref('');
const form = reactive({
  displayName: '',
  username: '',
  password: ''
});

const submitText = computed(() => {
  if (authStore.loading) {
    return mode.value === 'login' ? '登录中...' : '注册中...';
  }
  return mode.value === 'login' ? '登录并进入系统' : '注册并开始评分';
});

const navigateAfterAuth = () => {
  const redirect = route.query.redirect || '/recommendations';
  router.replace(String(redirect));
};

const handleSubmit = async () => {
  error.value = '';
  try {
    if (mode.value === 'login') {
      await authStore.login({
        username: form.username,
        password: form.password
      });
    } else {
      await authStore.register({
        displayName: form.displayName,
        username: form.username,
        password: form.password
      });
    }
    navigateAfterAuth();
  } catch (err) {
    error.value = err.response?.data?.message || err.message || '认证失败';
  }
};
</script>

<style scoped>
.auth-page {
  min-height: calc(100vh - 64px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
  background: #f1f5f9;
}

.auth-container {
  display: flex;
  width: 100%;
  max-width: 1000px;
  min-height: 600px;
  background: #fff;
  border-radius: 32px;
  box-shadow: 0 24px 64px rgba(15, 23, 42, 0.08);
  overflow: hidden;
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.auth-left {
  flex: 1;
  background: linear-gradient(135deg, #4f46e5, #ec4899);
  padding: 48px;
  display: flex;
  flex-direction: column;
  position: relative;
  color: #fff;
  overflow: hidden;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 24px;
  font-weight: 800;
  z-index: 10;
}

.brand i {
  font-size: 28px;
}

.welcome-text {
  margin-top: 80px;
  z-index: 10;
}

.welcome-text h2 {
  font-size: 36px;
  font-weight: 800;
  line-height: 1.2;
  margin: 0 0 16px;
}

.welcome-text p {
  font-size: 16px;
  line-height: 1.6;
  opacity: 0.9;
  max-width: 320px;
}

.auth-decor {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.decor-circle {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
}

.circle-1 {
  width: 400px;
  height: 400px;
  background: rgba(139, 92, 246, 0.6);
  top: -100px;
  right: -100px;
}

.circle-2 {
  width: 350px;
  height: 350px;
  background: rgba(236, 72, 153, 0.5);
  bottom: -50px;
  left: -100px;
}

.glass-card {
  position: absolute;
  top: 60%;
  right: -50px;
  width: 300px;
  height: 200px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 24px;
  transform: rotate(-15deg);
}

.auth-right {
  flex: 1;
  padding: 48px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: #fff;
}

.auth-right-content {
  max-width: 380px;
  margin: 0 auto;
  width: 100%;
}

.auth-header {
  margin-bottom: 32px;
}

.auth-header h1 {
  margin: 0 0 12px;
  font-size: 32px;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.02em;
}

.auth-header p {
  margin: 0;
  color: #64748b;
  font-size: 14px;
  line-height: 1.6;
}

.auth-tabs {
  display: flex;
  background: #f1f5f9;
  border-radius: 16px;
  padding: 6px;
  margin-bottom: 32px;
}

.tab-btn {
  flex: 1;
  height: 44px;
  border: none;
  background: transparent;
  color: #64748b;
  font-size: 15px;
  font-weight: 600;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.tab-btn.active {
  background: #fff;
  color: #0f172a;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.form-fields-wrapper {
  display: flex;
  flex-direction: column;
  gap: 20px;
  position: relative;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}

.form-field label {
  font-size: 13px;
  font-weight: 600;
  color: #475569;
  margin-left: 4px;
}

.p-input-icon-left {
  position: relative;
  display: block;
}

.input-icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: #94a3b8;
  font-size: 18px;
  transition: color 0.3s ease;
  z-index: 2;
}

.input {
  width: 100%;
  height: 52px;
  padding: 0 16px 0 46px;
  border: 2px solid #e2e8f0;
  border-radius: 16px;
  background: #f8fafc;
  font-size: 15px;
  color: #0f172a;
  transition: all 0.3s ease;
  box-sizing: border-box;
}

.input:focus {
  outline: none;
  border-color: #6366f1;
  background: #fff;
  box-shadow: 0 0 0 4px rgba(99, 102, 241, 0.1);
}

.input:focus + .input-icon,
.input:not(:placeholder-shown) + .input-icon {
  color: #6366f1;
}

.submit-btn {
  width: 100%;
  height: 52px;
  margin-top: 32px;
  border: none;
  border-radius: 16px;
  background: linear-gradient(135deg, #4f46e5, #7c3aed);
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  box-shadow: 0 12px 24px rgba(79, 70, 229, 0.25);
  transition: all 0.3s ease;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 16px 32px rgba(79, 70, 229, 0.35);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

.feedback {
  margin: 20px 0 0;
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

.feedback.error {
  background: #fef2f2;
  color: #dc2626;
  border: 1px solid #fecaca;
}

/* Animations */
.list-enter-active,
.list-leave-active {
  transition: all 0.4s ease;
}

.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.list-leave-active {
  position: absolute;
  width: 100%;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 860px) {
  .auth-container {
    flex-direction: column;
    min-height: auto;
  }
  
  .auth-left {
    padding: 32px 24px;
    align-items: center;
    text-align: center;
  }
  
  .welcome-text {
    margin-top: 24px;
  }
  
  .glass-card {
    display: none;
  }
  
  .auth-right {
    padding: 32px 24px;
  }
}

@media (max-width: 640px) {
  .auth-page {
    padding: 16px;
  }
  
  .auth-header h1 {
    font-size: 26px;
  }
}
</style>
