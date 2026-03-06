import { defineStore } from 'pinia';
import { computed, ref } from 'vue';
import { getCurrentUser, login as loginApi, logout as logoutApi, register as registerApi } from '@/api';

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null);
  const initialized = ref(false);
  const loading = ref(false);

  const isAuthenticated = computed(() => !!user.value);
  const isAdmin = computed(() => user.value?.role === 'ADMIN');

  const hydrate = async () => {
    if (initialized.value) {
      return user.value;
    }
    try {
      const data = await getCurrentUser();
      user.value = data.user;
    } catch (error) {
      user.value = null;
    } finally {
      initialized.value = true;
    }
    return user.value;
  };

  const login = async (payload) => {
    loading.value = true;
    try {
      const data = await loginApi(payload);
      user.value = data.user;
      initialized.value = true;
      return data;
    } finally {
      loading.value = false;
    }
  };

  const register = async (payload) => {
    loading.value = true;
    try {
      const data = await registerApi(payload);
      user.value = data.user;
      initialized.value = true;
      return data;
    } finally {
      loading.value = false;
    }
  };

  const logout = async () => {
    await logoutApi();
    user.value = null;
    initialized.value = true;
  };

  const clear = () => {
    user.value = null;
    initialized.value = true;
  };

  return {
    user,
    initialized,
    loading,
    isAuthenticated,
    isAdmin,
    hydrate,
    login,
    register,
    logout,
    clear
  };
});
