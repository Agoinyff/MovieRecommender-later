<template>
  <form class="card" @submit.prevent="emitSubmit">
    <div class="field">
      <label>用户 ID</label>
      <input v-model.number="form.userId" type="number" min="1" required />
    </div>

    <div class="field">
      <label>推荐算法</label>
      <select v-model="form.strategy">
        <option value="USER_BASED">用户协同过滤 (User Based)</option>
        <option value="ITEM_BASED">物品协同过滤 (Item Based)</option>
        <option value="SLOPE_ONE">Slope One</option>
      </select>
    </div>

    <div class="field">
      <label>返回条数</label>
      <input v-model.number="form.size" type="number" min="1" max="50" />
    </div>

    <button type="submit" :disabled="loading">{{ loading ? '正在计算...' : '获取推荐结果' }}</button>
  </form>
</template>

<script setup>
import { reactive, watchEffect } from 'vue';

const props = defineProps({
  initialUser: { type: Number, default: 100 },
  loading: { type: Boolean, default: false }
});

const emit = defineEmits(['submit']);

const form = reactive({
  userId: props.initialUser,
  strategy: 'USER_BASED',
  size: 10
});

watchEffect(() => {
  form.userId = props.initialUser;
});

const emitSubmit = () => {
  emit('submit', { ...form });
};
</script>

<style scoped>
.card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.04);
  display: grid;
  gap: 12px;
}

.field {
  display: grid;
  gap: 6px;
}

label {
  font-weight: 600;
  color: #4b5563;
}

input,
select,
button {
  height: 42px;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  padding: 0 12px;
  font-size: 15px;
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
}

input:focus,
select:focus,
button:focus {
  outline: none;
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.15);
}

button {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  font-weight: 700;
  border: none;
}

button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
</style>
