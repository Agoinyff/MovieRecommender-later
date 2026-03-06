import { computed, ref } from 'vue';
import { defineStore } from 'pinia';
import { getCurrentUser, login as loginApi, register as registerApi } from '@/api/auth';

const TOKEN_KEY = 'movie-recommender-token';
const USER_KEY = 'movie-recommender-user';

const readStoredUser = () => {
  const raw = localStorage.getItem(USER_KEY);
  if (!raw) {
    return null;
  }
  try {
    return JSON.parse(raw);
  } catch (error) {
    localStorage.removeItem(USER_KEY);
    return null;
  }
};

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '');
  const user = ref(readStoredUser());
  const loading = ref(false);

  const isAuthenticated = computed(() => Boolean(token.value && user.value));
  const isAdmin = computed(() => user.value?.role === 'ADMIN');

  const persistSession = (authPayload) => {
    token.value = authPayload.token;
    user.value = authPayload.user;
    localStorage.setItem(TOKEN_KEY, authPayload.token);
    localStorage.setItem(USER_KEY, JSON.stringify(authPayload.user));
  };

  const clearSession = () => {
    token.value = '';
    user.value = null;
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  };

  const login = async (payload) => {
    loading.value = true;
    try {
      const data = await loginApi(payload);
      persistSession(data);
      return data;
    } finally {
      loading.value = false;
    }
  };

  const register = async (payload) => {
    loading.value = true;
    try {
      const data = await registerApi(payload);
      persistSession(data);
      return data;
    } finally {
      loading.value = false;
    }
  };

  const fetchCurrentUser = async () => {
    if (!token.value) {
      clearSession();
      return null;
    }
    try {
      const profile = await getCurrentUser();
      user.value = profile;
      localStorage.setItem(USER_KEY, JSON.stringify(profile));
      return profile;
    } catch (error) {
      clearSession();
      throw error;
    }
  };

  const logout = () => {
    clearSession();
  };

  return {
    token,
    user,
    loading,
    isAuthenticated,
    isAdmin,
    login,
    register,
    fetchCurrentUser,
    logout,
    clearSession
  };
});
